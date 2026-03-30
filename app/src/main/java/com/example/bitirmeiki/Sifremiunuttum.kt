package com.example.bitirmeiki

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Sifremiunuttum : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sifremiunuttum)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etResetEmail)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        // 🔁 Şifre sıfırlama maili gönder
        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "E-posta adresi giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Şifre sıfırlama bağlantısı e-posta adresine gönderildi",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        it.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // ⬅️ Giriş ekranına dön
        tvBack.setOnClickListener {
            finish()
        }
    }
}
