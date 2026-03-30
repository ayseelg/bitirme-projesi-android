package com.example.bitirmeiki

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Alanlar boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser!!.uid

                    // 🔐 GİRİŞ OTURUMU OLUŞTUR
                    val sessionData = hashMapOf(
                        "loginAt" to FieldValue.serverTimestamp(),
                        "logoutAt" to null
                    )

                    val sessionRef = db.collection("users")
                        .document(uid)
                        .collection("sessions")
                        .document()

                    sessionRef.set(sessionData)

                    // sessionId sakla (logout için)
                    getSharedPreferences("session", MODE_PRIVATE)
                        .edit()
                        .putString("sessionId", sessionRef.id)
                        .apply()

                    // Kullanıcı adını çek (isteğe bağlı)
                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            val name = doc.getString("name") ?: ""
                            Toast.makeText(this, "Hoş geldin $name 👋", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, Kayitol::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, Sifremiunuttum::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
