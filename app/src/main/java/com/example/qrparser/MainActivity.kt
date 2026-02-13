package com.example.qrparser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var imgContainer: LinearLayout

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
        "7CA.008.008.A", // 19 (jeśli powinno być 7CA.008.088.A — daj znać)
        "7LE.008.084",   // 20
        "7CA.008.088.C", // 21
        "7C0.008.106",   // 22
        "7C0.008.106.A", // 23
        "7C4.008.085",   // 24
        "7C5.008.085"    // 25
    )

    // Mapowanie kodów na zasoby rysunków (pliki w res/drawable/)
    private val imageByCode: Map<String, Int> = mapOf(
        "7C0.008.103"   to R.drawable.img_7c0_008_103,
        "7C0.008.103.A" to R.drawable.img_7c0_008_103_a,
        "7C0.008.103.B" to R.drawable.img_7c0_008_103_b,
        "7C0.008.103.C" to R.drawable.img_7c0_008_103_c,
        "7CA.008.088"   to R.drawable.img_7ca_008_088,
        "7CA.008.088.C" to R.drawable.img_7ca_008_088_c,
        "7C4.008.085"   to R.drawable.img_7c4_008_085,
        "7C5.008.085"   to R.drawable.img_7c5_008_085
    )

    // ZXing launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            tvSequence.text = "-"
            tvVariants.text = "Anulowano lub brak danych."
            imgContainer.visibility = View.GONE
            imgContainer.removeAllViews()
        } else {
            handleScannedText(result.contents)
        }
    }

    // Runtime permission do kamery
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startScan()
        } else {
            tvVariants.text = "Brak uprawnienia do kamery."
            imgContainer.visibility = View.GONE
            imgContainer.removeAllViews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blokada orientacji jest w AndroidManifest.xml (android:screenOrientation="portrait")
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btnScan)
        tvSequence = findViewById(R.id.tvSequence)
        tvVariants = findViewById(R.id.tvVariants)
        imgContainer = findViewById(R.id.imgContainer)

        btnScan.setOnClickListener { ensureCameraAndScan() }
    }

    private fun ensureCameraAndScan() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) startScan() else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Nakieruj aparat na kod QR")
            setBeepEnabled(true)
            setCameraId(0)
            setOrientationLocked(true) // dodatkowo blokuje obrót widoku skanera
        }
        barcodeLauncher.launch(options)
    }

    private fun handleScannedText(raw: String) {
        // Przykład formatu: V111_0010000000000000000000_5_015800
        val parts = raw.trim().split('_')

        if (parts.size < 4) {
            tvSequence.text = "-"
            tvVariants.text = "Nieprawidłowy format: $raw"
            imgContainer.visibility = View.GONE
            imgContainer.removeAllViews()
            return
        }

        val bitString = parts[1].trim()   // 22 znaki 0/1 (pozycje 1..22 liczone OD LEWEJ)
        val seq6 = parts.last().trim()    // 6 cyfr sekwencji

        // Sekwencja: ostatnie 4 cyfry bez wiodących zer
        val seq4 = seq6.takeLast(4).trimStart('0').ifEmpty { "0" }
        tvSequence.text = seq4

        // Walidacja segmentu bitów
        if (bitString.length != 22 || bitString.any { it != '0' && it != '1' }) {
            tvVariants.text = "Błędny segment bitów (oczekiwane 22 znaki 0/1): $bitString"
            imgContainer.visibility = View.GONE
            imgContainer.removeAllViews()
            return
        }

        // Pozycje 8..25 liczone OD LEWEJ (1‑indeksowane) z offsetem:
        val OFFSET_FROM_LEFT = 5

        val results = mutableListOf<String>()
        for (i in 8..25) {
            val idx = i - OFFSET_FROM_LEFT  // i (1‑indeksowane) -> indeks w stringu (0‑indeksowany)
            if (idx in bitString.indices && bitString[idx] == '1') {
                val mapIdx = i - 7 // 8->0 ... 25->17
                if (mapIdx in mapping.indices) {
                    results.add("$i: ${mapping[mapIdx]}")
                }
            }
        }

        // Tekst wyników (diagnostyka)
        tvVariants.text = if (results.isEmpty()) {
            "Brak dopasowań (w pozycjach 8-25 nie ma '1')."
        } else {
            results.joinToString(separator = "\n")
        }

        // ====== KARTY: ETYKIETA + OBRAZ (wiele, jedno pod drugim) ======
        // Z linii "i: KOD" wyciągamy pary (label, resId) i renderujemy karty.
        data class Card(val label: String, val resId: Int)

        val cards: List<Card> = results.mapNotNull { line ->
            val code = line.substringAfter(": ").trim()
            val resId = imageByCode[code] ?: return@mapNotNull null
            Card(label = line, resId = resId)
        }

        imgContainer.removeAllViews()
        if (cards.isNotEmpty()) {
            imgContainer.visibility = View.VISIBLE

            for (card in cards) {
                // Etykieta (nazwa wycięcia nad obrazem)
                val labelView = TextView(this).apply {
                    text = card.label
                    textSize = 16f
                    setPadding(0, dp(4), 0, dp(4))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
                imgContainer.addView(labelView)

                // Obraz
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { lp ->
                        if (lp is LinearLayout.LayoutParams) {
                            lp.setMargins(0, 0, 0, dp(12)) // odstęp po obrazku
                        }
                    }
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageResource(card.resId)
                }
                imgContainer.addView(iv)
            }
        } else {
            imgContainer.visibility = View.GONE
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
