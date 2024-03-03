package com.furthergrow.android_download_manager.myProject

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.furthergrow.android_download_manager.R

class SplashScreen :AppCompatActivity() {

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_splash)
    }

    fun btnClick(view: View){
        startActivity(Intent(this@SplashScreen,PdfDownloadList::class.java))

    }

}