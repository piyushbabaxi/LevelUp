package com.piyush.levelup

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("levelup", MODE_PRIVATE)
    }

    private val bg = Color.rgb(12, 14, 13)
    private val card = Color.rgb(28, 32, 29)
    private val green = Color.rgb(177, 255, 73)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 165, 160)

    private lateinit var root: LinearLayout
    private lateinit var dateText: TextView
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var habitsLayout: LinearLayout

    private var selectedDate = today()

    private fun formatter(pattern: String) =
        SimpleDateFormat(pattern, Locale.US)

    private fun today(): String =
        formatter("yyyy-MM-dd").format(Date())

    private fun displayDate(): String =
        formatter("dd MMM yyyy").format(
            formatter("yyyy-MM-dd").parse(selectedDate)!!
        )

    private fun savedSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen()

        if (prefs.getString("name", null).isNullOrBlank()) {
            askName()
        }
    }

    private fun askName() {
        val input = EditText(this).apply {
            hint = "Your name"
            setSingleLine(true)
            setTextColor(white)
        }

        AlertDialog.Builder(this)
            .setTitle("Welcome to LevelUp")
            .setMessage("Start building better habits.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                prefs.edit().putString(
                    "name",
                    input.text.toString().trim().ifBlank { "Player" }
                ).apply()
                showScreen()
            }
            .show()
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = white
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
    }

    private fun button(
        value: String,
        action: () -> Unit
    ) = Button(this).apply {
        text = value
        isAllCaps = false
        setTextColor(green)
        setOnClickListener { action() }
    }

    private fun addSpace(height: Int) {
        root.addView(View(this), LinearLayout.LayoutParams(1, height))
    }

    private fun showScreen() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 26, 22, 30)
            setBackgroundColor(bg)
        }

        root.addView(text("LEVELUP  /  SELF IMPROVEMENT", 13f, green))

        addSpace(24)

        root.addView(text("Build your next level.", 30f).apply {
            setTypeface(null, Typeface.BOLD)
        })

        addSpace(8)

        root.addView(
            text("Welcome, ${prefs.getString("name", "Player")}", 17f, muted)
        )

        addSpace(24)

        val dateRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        dateRow.addView(button("‹") { changeDate(-1) })

        dateText = text(displayDate(), 17f, green).apply {
            gravity = Gravity.CENTER
        }

        dateRow.addView(
            dateText,
            LinearLayout.LayoutParams(0, 55, 1f)
        )

        dateRow.addView(button("›") {
            if (selectedDate < today()) changeDate(1)
        })

        root.addView(dateRow)

        addSpace(20)

        val progressCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(card)
        }

        progressCard
