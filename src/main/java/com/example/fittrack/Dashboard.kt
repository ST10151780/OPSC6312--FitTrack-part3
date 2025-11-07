package com.example.fittrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Get username from Intent
        username = intent.getStringExtra("username") ?: "User"

        // Reference UI elements
        val greetingTextView = findViewById<TextView>(R.id.tvGreeting)
        val caloriesTextView = findViewById<TextView>(R.id.tvCalories)
        val stepsTextView = findViewById<TextView>(R.id.tvSteps)
        val workoutsTextView = findViewById<TextView>(R.id.tvWorkouts)
        val settingsButton = findViewById<ImageButton>(R.id.btnSettings)

        val btnWorkout = findViewById<Button>(R.id.btnWorkout)
        val btnNutrition = findViewById<Button>(R.id.btnNutrition)
        val btnProgress = findViewById<Button>(R.id.btnProgress)

        // Set greeting dynamically
        greetingTextView.text = "Hey, $username!"

        // Placeholder summary
        caloriesTextView.text = "Calories Burned: 500 kcal"
        stepsTextView.text = "Steps Taken: 8000"
        workoutsTextView.text = "Workouts Completed: 2"

        // Settings Button → SettingsActivity
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        // Quick action buttons
        btnWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutsListActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnNutrition.setOnClickListener {
            val intent = Intent(this, NutritionActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnProgress.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        // Optional: Fetch users from API
        fetchUsersFromApi()

        // Schedule daily reminder notification
        scheduleDailyReminder()
    }

    private fun fetchUsersFromApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val users = ApiClient.api.getUsers()
                withContext(Dispatchers.Main) {
                    if (users.isNotEmpty()) {
                        Toast.makeText(
                            this@DashboardActivity,
                            "Fetched ${users.size} users from API!",
                            Toast.LENGTH_SHORT
                        ).show()
                        users.forEach {
                            Log.d("API_USER", "Name: ${it.name}, Email: ${it.email}")
                        }
                    } else {
                        Toast.makeText(
                            this@DashboardActivity,
                            "No users found on API",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Error fetching users: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ================== Daily Reminder Scheduling ==================
    private fun scheduleDailyReminder() {
        val workManager = WorkManager.getInstance(applicationContext)

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        workManager.enqueueUniqueWork(
            "daily_reminder_work",
            ExistingWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()

        // Set reminder time (e.g., 8 AM)
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Schedule for next day if time passed
        }

        return calendar.timeInMillis - now.timeInMillis
    }
}





