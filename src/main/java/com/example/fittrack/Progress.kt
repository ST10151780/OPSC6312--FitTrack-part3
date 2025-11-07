package com.example.fittrack

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var db: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var nutritionDao: NutritionDao
    private lateinit var lineChart: LineChart
    private lateinit var layoutRecent: LinearLayout
    private lateinit var rbCalories: RadioButton
    private lateinit var rbWorkouts: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        username = intent.getStringExtra("username") ?: "User"
        db = AppDatabase.getDatabase(this)
        workoutDao = db.workoutDao()
        nutritionDao = db.nutritionDao()

        lineChart = findViewById(R.id.lineChart)
        layoutRecent = findViewById(R.id.layoutRecent)
        rbCalories = findViewById(R.id.rbCalories)
        rbWorkouts = findViewById(R.id.rbWorkouts)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Metric toggle
        val rgMetric = findViewById<RadioGroup>(R.id.rgMetric)
        rgMetric.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCalories -> loadChartData("calories")
                R.id.rbWorkouts -> loadChartData("workouts")
            }
        }

        // Initial load
        loadChartData("calories")
    }

    private fun loadChartData(metric: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch data
            val workouts = workoutDao.getWorkoutsForUser(username)
            val nutritions = nutritionDao.getNutritionForUser(username)

            // Aggregate per day (last 7 days)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val last7Days = mutableListOf<String>()
            repeat(7) {
                last7Days.add(0, sdf.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }

            val entries = mutableListOf<Entry>()
            val recentActivities = mutableListOf<String>()

            last7Days.forEachIndexed { index, day ->
                var total = 0f
                if (metric == "calories") {
                    // Sum calories from nutrition
                    nutritions.filter { it.date == day }.forEach {
                        total += it.calories.toFloatOrNull() ?: 0f
                        recentActivities.add("🍽 ${it.mealName} – ${it.calories} kcal")
                    }
                } else if (metric == "workouts") {
                    workouts.filter { it.date == day }.forEach {
                        total += it.calories.toFloatOrNull() ?: 0f
                        recentActivities.add("🏋️ ${it.name} – ${it.duration} min – ${it.calories} kcal")
                    }
                }
                entries.add(Entry(index.toFloat(), total))
            }

            withContext(Dispatchers.Main) {
                val dataSet = LineDataSet(entries, if (metric == "calories") "Calories" else "Workouts")
                dataSet.lineWidth = 2f
                dataSet.circleRadius = 4f
                dataSet.setDrawValues(false)

                val lineData = LineData(dataSet)
                lineChart.data = lineData
                lineChart.invalidate()

                // Update recent activities
                layoutRecent.removeAllViews()
                recentActivities.takeLast(5).reversed().forEach {
                    val tv = TextView(this@ProgressActivity)
                    tv.text = it
                    layoutRecent.addView(tv)
                }
            }

        }
    }
}
