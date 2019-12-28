package com.example.textrecognizer.ui

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.example.textrecognizer.R
import com.example.textrecognizer.util.Language
import com.example.textrecognizer.util.ResultOrError
import com.example.textrecognizer.util.SmoothedMutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseLanguageIdentification =
        FirebaseNaturalLanguage.getInstance().languageIdentification
    private val modelManager: FirebaseTranslateModelManager = FirebaseTranslateModelManager.getInstance()
    val targetLang = MutableLiveData<Language>()
    val sourceText = SmoothedMutableLiveData<String>(SMOOTHING_DURATION)
    val translatedText = MediatorLiveData<ResultOrError>()
    val translating = MutableLiveData<Boolean>()
    val modelDownloading = SmoothedMutableLiveData<Boolean>(SMOOTHING_DURATION)

    var modelDownloadTask: Task<Void> = Tasks.forCanceled<Void>()

    var sourceLang = Transformations.switchMap(sourceText) { text ->
        val result = MutableLiveData<Language>()
        firebaseLanguageIdentification.identifyLanguage(text)
            .addOnSuccessListener {
                if (it != "und")
                    result.value = Language(it)
            }
        result
    }

    fun translate(): Task<String> {
        val text = sourceText.value
        val source = sourceLang.value
        val target = targetLang.value
        if (modelDownloading.value != false || translating.value != false) {
            return Tasks.forCanceled()
        }
        if (source == null || target == null || text == null || text.isEmpty()) {
            return Tasks.forResult("")
        }
        val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(source.code)
        val targetLangCode = FirebaseTranslateLanguage.languageForLanguageCode(target.code)
        if (sourceLangCode == null || targetLangCode == null) {
            return Tasks.forCanceled()
        }
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        modelDownloading.setValue(true)

        // Register watchdog to unblock long running downloads
        Handler().postDelayed({ modelDownloading.setValue(false) }, 15000)
        modelDownloadTask = translator.downloadModelIfNeeded().addOnCompleteListener {
            modelDownloading.setValue(false)
        }
        translating.value = true
        return modelDownloadTask.continueWithTask { task ->
            if (task.isSuccessful) {
                translator.translate(text)
            } else {
                Tasks.forException<String>(
                    task.exception
                        ?: Exception(getApplication<Application>().getString(R.string.unknown_error))
                )
            }
        }.addOnCompleteListener {
            translating.value = false
        }
    }

    // Gets a list of all available translation languages.
    val availableLanguages: List<Language> = FirebaseTranslateLanguage.getAllLanguages()
        .map { Language(FirebaseTranslateLanguage.languageCodeForLanguage(it)) }

    init {
        modelDownloading.setValue(false)
        translating.value = false
        // Create a translation result or error object.
        val processTranslation =
            OnCompleteListener<String> { task ->
                if (task.isSuccessful) {
                    translatedText.value = ResultOrError(task.result, null)
                } else {
                    if (task.isCanceled) {
                        // Tasks are cancelled for reasons such as gating; ignore.
                        return@OnCompleteListener
                    }
                    translatedText.value = ResultOrError(null, task.exception)
                }
            }
        // Start translation if any of the following change: detected text, source lang, target lang.
        translatedText.addSource(sourceText) { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(sourceLang) { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(targetLang) { translate().addOnCompleteListener(processTranslation) }
    }

    companion object {
        // Amount of time (in milliseconds) to wait for detected text to settle
        private const val SMOOTHING_DURATION = 50L
    }
}
