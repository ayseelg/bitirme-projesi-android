package com.example.bitirmeiki

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class DetectionActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI
    private lateinit var btnYolov11: Button
    private lateinit var btnYolov12: Button
    private lateinit var btnYolov8: Button
    private lateinit var btnUpload: Button
    private lateinit var btnAnalyze: Button
    private lateinit var previewImage: ImageView
    private lateinit var resultText: TextView
    private lateinit var resultImage: ImageView
    private lateinit var resultCard: CardView
    private lateinit var historyRecycler: RecyclerView

    private var imageUri: Uri? = null
    private var selectedModel = "YOLOv11"

    private val SERVER_URL = "http://100.104.97.123:5000/predict"
    private val TAG = "DetectionActivity"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val tumorTypes = mapOf(
        0 to "Glioma",
        1 to "Meningioma",
        2 to "Tümör Yok",
        3 to "Pituitary"
    )

    private val historyList = mutableListOf<AnalysisHistory>()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews()
        setupRecyclerView()
        setupModelButtons()
        loadHistoryFromFirestore()

        resultCard.isVisible = true
        resultText.text = "Henüz analiz yapılmadı."
        resultImage.isVisible = false

        val pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    imageUri = result.data?.data
                    previewImage.setImageURI(imageUri)
                    previewImage.isVisible = true
                    btnAnalyze.isEnabled = true
                }
            }

        btnUpload.setOnClickListener {
            pickImage.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }

        btnAnalyze.setOnClickListener {
            imageUri?.let { uploadImageToServer(it) }
        }

        btnAnalyze.isEnabled = false
    }

    private fun initViews() {
        btnYolov11 = findViewById(R.id.btnYolov11)
        btnYolov12 = findViewById(R.id.btnYolov12)
        btnYolov8 = findViewById(R.id.btnYolov8)
        btnUpload = findViewById(R.id.btnUpload)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        previewImage = findViewById(R.id.previewImage)
        resultText = findViewById(R.id.resultText)
        resultImage = findViewById(R.id.resultImage)
        resultCard = findViewById(R.id.resultCard)
        historyRecycler = findViewById(R.id.historyRecycler)
    }

    private fun setupModelButtons() {
        btnYolov11.setOnClickListener { selectModel("YOLOv11") }
        btnYolov12.setOnClickListener { selectModel("YOLOv12") }
        btnYolov8.setOnClickListener { selectModel("YOLOv8") }
    }

    private fun selectModel(model: String) {
        selectedModel = model
        btnYolov11.alpha = if (model == "YOLOv11") 1f else 0.5f
        btnYolov12.alpha = if (model == "YOLOv12") 1f else 0.5f
        btnYolov8.alpha = if (model == "YOLOv8") 1f else 0.5f
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList)
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter
    }

    private fun uploadImageToServer(uri: Uri) {
        Thread {
            try {
                val file = File(cacheDir, "img.jpg")
                contentResolver.openInputStream(uri)!!.use { input ->
                    file.outputStream().use { input.copyTo(it) }
                }

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", selectedModel)
                    .addFormDataPart(
                        "image",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val request = Request.Builder().url(SERVER_URL).post(body).build()
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body!!.string())

                val predictedClass = json.getInt("predicted_class")
                val confidence = (json.getDouble("confidence") * 100).toInt()

                runOnUiThread {
                    showResult(predictedClass, confidence)
                    saveAnalysisToFirestore(predictedClass, confidence)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun showResult(predictedClass: Int, confidence: Int) {
        val tumor = tumorTypes[predictedClass] ?: "Bilinmeyen"
        resultText.text =
            "Sonuç:\nTür: $tumor\nGüven: %$confidence\nModel: $selectedModel"
    }

    // 🔥 KULLANICIYA ÖZEL KAYIT
    private fun saveAnalysisToFirestore(predictedClass: Int, confidence: Int) {
        val user = auth.currentUser ?: return

        val data = hashMapOf(
            "model" to selectedModel,
            "predictedClass" to predictedClass,
            "confidence" to confidence,
            "timestamp" to SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            ).format(Date())
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("analyses")
            .add(data)

        historyList.add(
            0,
            AnalysisHistory(
                selectedModel,
                predictedClass,
                confidence,
                data["timestamp"] as String
            )
        )
        historyAdapter.notifyItemInserted(0)
    }

    // 🔥 SADECE KENDİ GEÇMİŞİNİ ÇEK
    private fun loadHistoryFromFirestore() {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("analyses")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { docs ->
                historyList.clear()
                for (d in docs) {
                    historyList.add(
                        AnalysisHistory(
                            d.getString("model") ?: "",
                            d.getLong("predictedClass")!!.toInt(),
                            d.getLong("confidence")!!.toInt(),
                            d.getString("timestamp") ?: ""
                        )
                    )
                }
                historyAdapter.notifyDataSetChanged()
            }
    }

    data class AnalysisHistory(
        val model: String,
        val predictedClass: Int,
        val confidence: Int,
        val timestamp: String
    )

    inner class HistoryAdapter(private val list: List<AnalysisHistory>) :
        RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val t1: TextView = v.findViewById(R.id.historyModel)
            val t2: TextView = v.findViewById(R.id.historyClass)
            val t3: TextView = v.findViewById(R.id.historyConfidence)
            val t4: TextView = v.findViewById(R.id.historyTime)
        }

        override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
            return VH(
                LayoutInflater.from(p.context)
                    .inflate(R.layout.history_item, p, false)
            )
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val item = list[i]
            h.t1.text = item.model
            h.t2.text = tumorTypes[item.predictedClass]
            h.t3.text = "%${item.confidence}"
            h.t4.text = item.timestamp
        }

        override fun getItemCount() = list.size
    }
}
