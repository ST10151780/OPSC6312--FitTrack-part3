package com.example.fittrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Button
import android.content.Intent

class WorkoutsListActivity : AppCompatActivity() {

    private lateinit var workoutsContainer: LinearLayout
    private lateinit var db: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts_list)

        workoutsContainer = findViewById(R.id.workoutsContainer)
        db = AppDatabase.getDatabase(this)
        workoutDao = db.workoutDao()

        username = intent.getStringExtra("username") ?: "User"

        loadWorkouts()

        val btnAddWorkout = findViewById<Button>(R.id.btnAddWorkout)
        btnAddWorkout.setOnClickListener {
            startActivity(Intent(this, LogWorkoutActivity::class.java).putExtra("username", username))
        }
    }

    private fun loadWorkouts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val workouts = workoutDao.getWorkoutsForUser(username)

            withContext(Dispatchers.Main) {
                workoutsContainer.removeAllViews()
                workouts.forEach { addWorkoutItem(it) }
            }
        }
    }

    private fun addWorkoutItem(workout: Workout) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.workout_item, workoutsContainer, false)

        val tvWorkoutName = itemView.findViewById<TextView>(R.id.tvWorkoutName)
        val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
        val tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)
        val tvCalories = itemView.findViewById<TextView>(R.id.tvCalories)
        val tvNotes = itemView.findViewById<TextView>(R.id.tvNotes)

        tvWorkoutName.text = workout.name
        tvDuration.text = "Duration: ${workout.duration}"
        tvCalories.text = "Calories: ${workout.calories}"
        tvNotes.text = "Notes: ${workout.notes}"

        tvWorkoutName.setOnClickListener {
            detailsLayout.visibility = if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        workoutsContainer.addView(itemView)
    }
}

