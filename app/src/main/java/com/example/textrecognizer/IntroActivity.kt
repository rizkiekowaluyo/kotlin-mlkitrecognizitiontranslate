package com.example.textrecognizer

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment

class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        addSlide(AppIntroFragment.newInstance("Hello","Welcome to my app",R.drawable.greeting,
            ContextCompat.getColor(applicationContext,R.color.DarkAqua)))

        addSlide(AppIntroFragment.newInstance("What my app can do?","You can translate any word to another language",R.drawable.confused,
            ContextCompat.getColor(applicationContext,R.color.Aqua)))

        addSlide(AppIntroFragment.newInstance("How it works?","make sure your words enter the box on the camera",R.drawable.camera,
            ContextCompat.getColor(applicationContext,R.color.DarkAqua)))

    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}