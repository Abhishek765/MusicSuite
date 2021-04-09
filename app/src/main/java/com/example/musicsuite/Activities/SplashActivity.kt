package com.example.musicsuite.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.musicsuite.R

class SplashActivity : AppCompatActivity() {

    //    required permissions
    var permissionsString = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        if (!hasPermissions(this@SplashActivity, *permissionsString)) {
            //we have to grant permissions
            ActivityCompat.requestPermissions(this@SplashActivity, permissionsString, 131)
        } else {
            Handler().postDelayed({
                val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                startAct.putExtra("fromStart", "FromStart")
                startActivity(startAct)
//                Log.e("TAG", "onCreate:  $startAct" )
                this.finish()
            }, 1000)
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            131 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    Handler().postDelayed({
                        val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                        startAct.putExtra("fromStart", "FromStart")
                        startActivity(startAct)
                        this.finish()
                    }, 1000)
                } else {
                    Toast.makeText(this@SplashActivity, "Please grant all permission to continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                return

            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        var hasAllPermissions = true
        for (permission in permissions) {
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }

}