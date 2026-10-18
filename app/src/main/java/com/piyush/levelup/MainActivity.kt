package com.piyush.levelup

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("levelup", MODE_PRIVATE)
    }

    private val green = Color.rgb(177, 255, 73)
    private val background = Color.rgb(12, 14, 13)
    private val card = Color.rgb(28, 32, 29)

    private lateinit var content: LinearLayout
    private lateinit var dateText: TextView
    private lateinit var habitsLayout: LinearLayout

    private var selectedDate = today()

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun displayDate(): String =
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .parse(selectedDate)!!
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.getString("name", null) == null) {
            askName()
        }

        showScreen()
    }

    private fun askName() {
        val input = EditText(this)
        input.hint = "Enter your name"

        AlertDialog.Builder(this)
            .setTitle("Welcome to LevelUp")
            .setMessage("Your improvement starts here.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                prefs.edit()
                    .putString(
                        "name",
                        input.text.toString().trim()
                            .ifBlank { "Player" }
                    )
                    .apply()

                showScreen()
            }
            .show()
    }

    private fun label(
        value: String,
        size: Float,
        color: Int = Color.WHITE
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
    }

    private fun showScreen() {
        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 30, 22, 24)
            setBackgroundColor(background)
        }

        content.addView(
            label("LEVELUP  /  SELF IMPROVEMENT", 13f, green)
        )

        content.addView(
            label(
                "Build your next level.",
                28f
            ).apply {
                setPadding(0, 18, 0, 6)
            }
        )

        content.addView(
            label(
                "Welcome, ${prefs.getString("name", "Player")}",
                16f
            )
        )

        val dateRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 22, 0, 18)
        }

        dateRow.addView(makeButton("‹") {
            changeDate(-1)
        })

        dateText = label(displayDate(), 16f, green).apply {
            gravity = Gravity.CENTER
        }

        dateRow.addView(
            dateText,
            LinearLayout.LayoutParams(0, 55, 1f)
        )

        dateRow.addView(makeButton("›") {
            changeDate(1)
        })

        content.addView(dateRow)

        content.addView(
            label("YOUR DAILY PROGRESS", 16f, green)
        )

        habitsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 14, 0, 14)
        }

        content.addView(habitsLayout)

        content.addView(makeButton("+ Add Habit") {
            addHabit()
        })

        content.addView(
            label(
                "Small wins. Every day.",
                14f,
                Color.LTGRAY
            ).apply {
                setPadding(0, 24, 0, 0)
            }
        )

        scroll.addView(content)
        setContentView(scroll)

        refreshHabits()
    }

    private fun makeButton(
        title: String,
        action: () -> Unit
    ) = Button(this).apply {
        text = title
        setOnClickListener { action() }
    }

    private fun changeDate(days: Int) {
        val calendar = java.util.Calendar.getInstance()

        calendar.time = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).parse(selectedDate)!!

        calendar.add(
            java.util.Calendar.DAY_OF_MONTH,
            days
        )

        selectedDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(calendar.time)

        refreshHabits()
    }

    private fun habitsForDate(): Set<String> {
        val everyday = prefs.getStringSet(
            "everyday",
            emptySet()
        )!!.toSet()

        val todayOnly = prefs.getStringSet(
            "today_$selectedDate",
            emptySet()
        )!!.toSet()

        val removed = prefs.getStringSet(
            "removed_$selectedDate",
            emptySet()
        )!!.toSet()

        return (everyday + todayOnly) - removed
    }

    private fun addHabit() {
        val input = EditText(this)
        input.hint = "Habit name"

        val options = arrayOf(
            "Today only",
            "Everyday"
        )

        var choice = 0

        AlertDialog.Builder(this)
            .setTitle("Add Habit")
            .setView(input)
            .setSingleChoiceItems(options, 0) { _, which ->
                choice = which
            }
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    val key = if (choice == 0) {
                        "today_$selectedDate"
                    } else {
                        "everyday"
                    }

                    val existing = prefs.getStringSet(
                        key,
                        emptySet()
                    )!!.toSet()

                    prefs.edit()
                        .putStringSet(key, existing + name)
                        .apply()

                    refreshHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshHabits() {
        if (!::habitsLayout.isInitialized) return

        dateText.text = displayDate()
        habitsLayout.removeAllViews()

        val habits = habitsForDate()
        var completed = 0

        habits.sorted().forEach { habit ->
            val row = LinearLayout(this).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(10, 8, 6, 8)
                setBackgroundColor(card)
            }

            val check = CheckBox(this).apply {
                text = habit
                setTextColor(Color.WHITE)

                isChecked = prefs.getBoolean(
                    "done_${selectedDate}_$habit",
                    false
                )

                if (isChecked) completed++

                setOnCheckedChangeListener { _, checked ->
                    prefs.edit()
                        .putBoolean(
                            "done_${selectedDate}_$habit",
                            checked
                        )
                        .apply()

                    refreshHabits()
                }
            }

            row.addView(
                check,
                LinearLayout.LayoutParams(0, 55, 1f)
            )

            row.addView(makeButton("⋮") {
                deleteHabit(habit)
            })

            val params = LinearLayout.LayoutParams(
                -1,
                -2
            )

            params.setMargins(0, 0, 0, 8)

            habitsLayout.addView(row, params)
        }

        val xp = completed * 10

        content.findViewWithTag<TextView>("progress")?.let {
            it.text = "$completed/${habits.size} habits completed • $xp XP"
        }

        val progress = label(
            "$completed/${habits.size} habits completed • $xp XP",
            15f,
            green
        )

        progress.tag = "progress"

        if (content.childCount > 5) {
            content.addView(progress, 4)
        }
    }

    private fun deleteHabit(habit: String) {
        val options = arrayOf(
            "Delete today only",
            "Delete everyday"
        )

        AlertDialog.Builder(this)
            .setTitle("Remove $habit?")
            .setItems(options) { _, which ->

                if (which == 0) {
                    val key = "removed_$selectedDate"

                    val removed = prefs.getStringSet(
                        key,
                        emptySet()
                    )!!.toSet()

                    prefs.edit()
                        .putStringSet(key, removed + habit)
                        .apply()

                } else {
                    val existing = prefs.getStringSet(
                        "everyday",
                        emptySet()
                    )!!.toSet()

                    prefs.edit()
                        .putStringSet(
                            "everyday",
                            existing - habit
                        )
                        .apply()

                    val todayOnly = prefs.getStringSet(
                        "today_$selectedDate",
                        emptySet()
                    )!!.toSet()

                    prefs.edit()
                        .putStringSet(
                            "today_$selectedDate",
                            todayOnly - habit
                        )
                        .apply()
                }

                refreshHabits()
            }
            .show()
    }
}
