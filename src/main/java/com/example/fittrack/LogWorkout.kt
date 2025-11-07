package com.example.fittrack

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LogWorkoutActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_workout)

        db = AppDatabase.getDatabase(this)
        workoutDao = db.workoutDao()

        username = intent.getStringExtra("username") ?: "User"

        val etWorkoutName = findViewById<EditText>(R.id.etWorkoutName)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val etDate = findViewById<EditText>(R.id.etDate)
        val btnSaveWorkout = findViewById<Button>(R.id.btnSaveWorkout)
        val btnBackDashboard = findViewById<Button>(R.id.btnBackDashboard)

        // 🗓 Date picker
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = android.app.DatePickerDialog(
                this,
                { _, y, m, d ->
                    etDate.setText("$d/${m + 1}/$y")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // 💾 Save workout button
        btnSaveWorkout.setOnClickListener {
            val name = etWorkoutName.text.toString().trim()
            val durationText = etDuration.text.toString().trim()
            val caloriesText = etCalories.text.toString().trim()
            val date = etDate.text.toString().trim()

            // Validation
            if (name.isEmpty() || durationText.isEmpty() || caloriesText.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Safe parsing
            val durationInt = durationText.toIntOrNull() ?: 0
            val caloriesInt = caloriesText.toIntOrNull() ?: 0

            // Optional: block invalid (zero) input
            if (durationInt <= 0 || caloriesInt <= 0) {
                Toast.makeText(this, "Enter valid numbers for duration and calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val workout = Workout(
                username = username,
                name = name,
                duration = durationInt.toString(),
                calories = caloriesInt.toString(),
                notes = "", // optional
                date = date
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 🧩 Save locally
                    workoutDao.insertWorkout(workout)

                    // 🌐 Save to API
                    ApiClient.api.createWorkout(
                        WorkoutDTO(
                            username = username,
                            workoutName = name,
                            duration = durationInt,
                            calories = caloriesInt,
                            notes = ""
                        )
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogWorkoutActivity, "Workout saved successfully!", Toast.LENGTH_SHORT).show()
                        etWorkoutName.text.clear()
                        etDuration.text.clear()
                        etCalories.text.clear()
                        etDate.text.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogWorkoutActivity, "Error saving workout: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // ⏪ Back to list
        btnBackDashboard.setOnClickListener {
            startActivity(Intent(this, WorkoutsListActivity::class.java).putExtra("username", username))
            finish()
        }
    }
}


