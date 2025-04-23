package com.example.sessionandtoken
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
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

class SessionActivity : AppCompatActivity() {
    private lateinit var client: OkHttpClient
    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var protectedButton: Button
    private lateinit var responseText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        // Initialize OkHttp with CookieJar
        client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        // UI elements
        usernameEdit = findViewById(R.id.usernameEdit1)
        passwordEdit = findViewById(R.id.passwordEdit1)
        loginButton = findViewById(R.id.loginButton1)
        protectedButton = findViewById(R.id.protectedButton1)
        responseText = findViewById(R.id.responseText1)

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
            .url("http://192.168.10.218:5000/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    e.message?.let { Log.d("1111", it) };
                    Toast.makeText(this@SessionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        responseText.text = responseBody
                        Toast.makeText(this@SessionActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    } else {
                        responseText.text = responseBody
                        Toast.makeText(this@SessionActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun accessProtected() {
        val request = Request.Builder()
            .url("http://192.168.10.218:5000/protected")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    e.message?.let { Log.d("error", it) };
                    //Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    responseText.text = responseBody
                    if (!response.isSuccessful) {
                        Log.d("error", "Access failed") ;
                        //Toast.makeText(this@SessionActivity, "Access failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}