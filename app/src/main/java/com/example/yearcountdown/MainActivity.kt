package com.example.yearcountdown

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var splashView: SplashView
    private lateinit var mainContent: RelativeLayout
    private lateinit var countdownView: YearCountdownView
    private lateinit var colorPicker: Button
    private lateinit var bgPicker: Button
    private lateinit var horizontalAlignSpinner: Spinner
    private lateinit var verticalAlignSpinner: Spinner
    private lateinit var yearText: TextView

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadBackgroundImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splashView = findViewById(R.id.splashView)
        mainContent = findViewById(R.id.mainContent)
        countdownView = findViewById(R.id.countdownView)
        colorPicker = findViewById(R.id.colorPicker)
        bgPicker = findViewById(R.id.bgPicker)
        horizontalAlignSpinner = findViewById(R.id.horizontalAlign)
        verticalAlignSpinner = findViewById(R.id.verticalAlign)
        yearText = findViewById(R.id.yearText)

        // Show splash screen
        splashView.startAnimation {
            // Hide splash and show main content after animation
            splashView.visibility = android.view.View.GONE
            mainContent.visibility = android.view.View.VISIBLE
        }

        setupAlignmentSpinners()
        setupColorPicker()
        setupBackgroundPicker()
        updateYearText()
    }

    private fun setupAlignmentSpinners() {
        val hAlignments = arrayOf("Left", "Center", "Right")
        val vAlignments = arrayOf("Top", "Center", "Bottom")

        horizontalAlignSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, hAlignments
        )
        verticalAlignSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, vAlignments
        )

        horizontalAlignSpinner.setSelection(1) // Default center
        verticalAlignSpinner.setSelection(1) // Default center

        horizontalAlignSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, p3: Long) {
                countdownView.horizontalAlign = when(pos) {
                    0 -> YearCountdownView.Align.START
                    2 -> YearCountdownView.Align.END
                    else -> YearCountdownView.Align.CENTER
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        verticalAlignSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, p3: Long) {
                countdownView.verticalAlign = when(pos) {
                    0 -> YearCountdownView.Align.START
                    2 -> YearCountdownView.Align.END
                    else -> YearCountdownView.Align.CENTER
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupColorPicker() {
        colorPicker.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun setupBackgroundPicker() {
        bgPicker.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun loadBackgroundImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            countdownView.setBackgroundBitmap(bitmap)
            inputStream?.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showColorPickerDialog() {
        val colors = arrayOf(
            "White" to 0xFFFFFFFF.toInt(),
            "Black" to 0xFF000000.toInt(),
            "Red" to 0xFFFF0000.toInt(),
            "Green" to 0xFF00FF00.toInt(),
            "Blue" to 0xFF0000FF.toInt(),
            "Yellow" to 0xFFFFFF00.toInt(),
            "Cyan" to 0xFF00FFFF.toInt(),
            "Magenta" to 0xFFFF00FF.toInt(),
            "Orange" to 0xFFFFA500.toInt(),
            "Purple" to 0xFF800080.toInt()
        )

        val colorNames = colors.map { it.first }.toTypedArray()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Choose Dot Color")
        builder.setItems(colorNames) { _, which ->
            countdownView.dotColor = colors[which].second
        }
        builder.show()
    }

    private fun updateYearText() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        yearText.text = year.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        }
    }
}