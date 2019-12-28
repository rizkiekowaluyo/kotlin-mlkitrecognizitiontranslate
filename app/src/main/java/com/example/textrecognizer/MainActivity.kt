package com.example.textrecognizer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.example.textrecognizer.ui.MainFragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.pranavpandey.android.dynamic.toasts.DynamicToast


class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.hide()


        val sharedPref: SharedPreferences = getSharedPreferences("Pref",Context.MODE_PRIVATE)

        if (!sharedPref.getBoolean("first",false)){
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean("first",true)
            editor.apply()
            val intent = Intent(this,IntroActivity::class.java)
            startActivity(intent)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}
