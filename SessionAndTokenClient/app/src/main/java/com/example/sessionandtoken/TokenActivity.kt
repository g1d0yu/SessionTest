package com.example.sessionandtoken

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class TokenActivity : AppCompatActivity() {
    private lateinit var client: OkHttpClient
    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var protectedButton: Button
    private lateinit var responseText: TextView
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token)

        // Initialize OkHttp
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)

        // UI elements
        usernameEdit = findViewById(R.id.usernameEdit2)
        passwordEdit = findViewById(R.id.passwordEdit2)
        loginButton = findViewById(R.id.loginButton2)
        protectedButton = findViewById(R.id.protectedButton2)
        responseText = findViewById(R.id.responseText2)

        // Login button
        loginButton.setOnClickListener {
            val username = usernameEdit.text.toString()
            val password = passwordEdit.text.toString()
            login(username, password)
        }

        // Protected endpoint button
        protectedButton.setOnClickListener {
            accessProtected()
        }
    }

    private fun login(username: String, password: String) {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString()
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://192.168.10.218:5001/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    e.message?.let { Log.d("error",it) }
                   // Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        val json = JSONObject(responseBody)
                        val token = json.getString("token")
                        sharedPreferences.edit().putString("token", token).apply()
                        responseText.text = "Token received"
                        Toast.makeText(this@TokenActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    } else {
                        responseText.text = responseBody
                        Toast.makeText(this@TokenActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun accessProtected() {
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            runOnUiThread {
                Toast.makeText(this@TokenActivity, "Please login first", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val request = Request.Builder()
            .url("http://192.168.10.218:5001/protected")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TokenActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    responseText.text = responseBody
                    if (!response.isSuccessful) {
                        Toast.makeText(this@TokenActivity, "Access failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}