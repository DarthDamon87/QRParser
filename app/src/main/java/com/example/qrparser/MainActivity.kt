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
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btnScan)
        tvSequence = findViewById(R.id.tvSequence)
        tvVariants = findViewById(R.id.tvVariants)

        btnScan.setOnClickListener {
            ensureCameraAndScan()
        }
    }

    private fun ensureCameraAndScan() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startScan()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Nakieruj aparat na kod QR")
            setBeepEnabled(true)
            setCameraId(0)
            setOrientationLocked(true)
        }
        barcodeLauncher.launch(options)
    }

    private fun handleScannedText(raw: String) {
        // Oczekiwany przykład: V111_0010000000000000000000_5_015800
        // Rozbijamy po "_" bez skomplikowanych regexów (prościej i odporniej na wklejki).
        val parts = raw.trim().split('_')

        if (parts.size < 4) {
            tvSequence.text = "-"
            tvVariants.text = "Nieprawidłowy format: $raw"
            return
        }

        val bitString = parts[1].trim()   // 22 znaki 0/1
        val seq6 = parts.last().trim()    // 6 cyfr

        // Sekwencja: ostatnie 4 cyfry, bez wiodących zer
        val seq4 = seq6.takeLast(4).trimStart('0').ifEmpty { "0" }
        tvSequence.text = seq4

        // Walidacja segmentu bitów (22 znaki 0/1)
        if (bitString.length != 22 || bitString.any { it != '0' && it != '1' }) {
            tvVariants.text = "Błędny segment bitów (oczekiwane 22 znaki 0/1): $bitString"
            return
        }

        val results = mutableListOf<String>()
        // Pozycje 8..25 (indeksy 7..24)
        for (pos in 8..25) {
            val idx = pos - 1
            if (idx in bitString.indices && bitString[idx] == '1') {
                val mapIdx = pos - 8
                if (mapIdx in mapping.indices) {
                    results.add("$pos: ${mapping[mapIdx]}")
                }
            }
        }

        tvVariants.text = if (results.isEmpty()) {
            "Brak dopasowań (w pozycjach 8-25 nie ma '1')."
        } else {
            results.joinToString(separator = "\n")
        }
    }
}
