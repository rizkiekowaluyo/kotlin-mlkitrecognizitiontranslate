package com.example.textrecognizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity: AppCompatActivity() {

    var mAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val actionBar = supportActionBar
        actionBar!!.title = "Profile"
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)


        mAuth = FirebaseAuth.getInstance()
        val user = mAuth!!.currentUser
        tvUsername.text = user?.displayName
        tvEmail.text = user?.email
        Glide.with(this)
            .load(user?.photoUrl)
            .into(imageview_account_profile)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
