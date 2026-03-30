package com.example.bitirmeiki

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Kayitol : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kayitol)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<TextInputEditText>(R.id.etPasswordConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etPasswordConfirm.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Ad boş olamaz"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Geçerli e-posta gir"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Şifre en az 6 karakter"
                return@setOnClickListener
            }

            if (password != confirm) {
                etPasswordConfirm.error = "Şifreler uyuşmuyor"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid

                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kayıt başarılı 🎉", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        tvBackToLogin.setOnClickListener { finish() }
    }
}
