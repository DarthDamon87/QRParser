package com.example.qrparser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var tvSequence: TextView
    private lateinit var tvVariants: TextView

    // Mapa pozycji 8..25 -> symbol (18 pozycji)
    private val mapping: List<String> = listOf(
        "7C0.008.081",   // 8
        "7C0.008.082",   // 9
        "7C0.008.083",   // 10
        "7C0.008.083.A", // 11
        "7C0.008.084",   // 12
        "7C0.008.084.A", // 13
        "7C0.008.103",   // 14
        "7C0.008.103.A", // 15
        "7C0.008.103.B", // 16
        "7C0.008.103.C", // 17
        "7CA.008.088",   // 18
        "7CA.008.008.A", // 19
        "7LE.008.084",   // 20
        "7CA.008.088.C", // 21
        "7C0.008.106",   // 22
        "7C0.008.106.A", // 23
        "7C4.008.085",   // 24
        "7C5.008.085"    // 25
    )

    // Launcher ZXing
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            tvSequence.text = "-"
            tvVariants.text = "Anulowano lub brak danych."
        } else {
            handleScannedText(result.contents)
        }
    }

    // Uprawnienie do kamery
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startScan()
        } else {
            tvVariants.text = "Brak uprawnienia do kamery."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blokadę orientacji mamy w AndroidManifest.xml:
        // <activity ... android:screenOrientation="portrait" />
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btnScan)
        tvSequence = findViewById(R.id.tvSequence)
        tvVariants = findViewById(R.id.tvVariants)

        btnScan.setOnClickListener {
            ensureCameraAndScan()
        }
    }

