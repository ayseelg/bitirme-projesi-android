package com.example.bitirmeiki

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        btnMenu = findViewById(R.id.btnMenu)

        val uid = auth.currentUser!!.uid

        // 🔹 Drawer Header → Kullanıcı Ad Soyad
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                tvUserName.text = name ?: "Kullanıcı"
            }

        // ☰ Menü butonu
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Drawer menü itemleri
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_detection -> {
                    startActivity(Intent(this, DetectionActivity::class.java))
                }

                R.id.nav_segmentation -> {
                    startActivity(Intent(this, SegmentationActivity::class.java))
                }

                R.id.nav_logout -> {
                    logoutAndSaveSession()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // 🔙 Back davranışı
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    // 🔐 ÇIKIŞ + logoutAt KAYDI
    private fun logoutAndSaveSession() {
        val uid = auth.currentUser!!.uid
        val sessionId = getSharedPreferences("session", MODE_PRIVATE)
            .getString("sessionId", null)

        sessionId?.let {
            db.collection("users")
                .document(uid)
                .collection("sessions")
                .document(it)
                .update("logoutAt", FieldValue.serverTimestamp())
        }

        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
