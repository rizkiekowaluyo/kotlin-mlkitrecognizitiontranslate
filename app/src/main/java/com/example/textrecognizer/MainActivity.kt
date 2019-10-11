package com.example.textrecognizer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(){

    var PICK_IMAGE_REQUEST = 1
    lateinit var imageBitmap: Bitmap
    var mAuth : FirebaseAuth? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth!!.currentUser
        tvText.text = user?.email


        findViewById<ImageView>(R.id.imgPict)
        FirebaseApp.initializeApp(this)

        btn_camera.setOnClickListener { View -> takePictureIntent() }
        btn_detect.setOnClickListener { View -> detectImage() }

    }

    fun takePictureIntent(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null){
            imageBitmap = data.extras?.get("data") as Bitmap
            imgPict.setImageBitmap(imageBitmap)
        }
    }

    fun detectImage(){
        val image = FirebaseVisionImage.fromBitmap(imageBitmap) as FirebaseVisionImage
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText -> processTextRecognitionResult(firebaseVisionText) }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    private fun processTextRecognitionResult(firebaseVisionText: FirebaseVisionText) {
        val blocks = firebaseVisionText.textBlocks
        if (blocks.size == 0){
            Toast.makeText(this,"No Text",Toast.LENGTH_LONG).show()
            return
        }
        for (block in firebaseVisionText.textBlocks){
            val txt = block.text
            tvText.setText(txt)
        }
    }

}
