package com.example.sessionandtoken


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sessionButton: Button = findViewById(R.id.button_session)
        val tokenButton: Button = findViewById(R.id.button_token)

        sessionButton.setOnClickListener {
            val intent = Intent(this, SessionActivity::class.java)
            startActivity(intent)
        }

        tokenButton.setOnClickListener {
            val intent = Intent(this, TokenActivity::class.java)
            startActivity(intent)
        }
    }
}