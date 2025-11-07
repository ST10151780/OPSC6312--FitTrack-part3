package com.example.fittrack

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NutritionActivity : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var db: AppDatabase
    private lateinit var nutritionDao: NutritionDao
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutritions)

        username = intent.getStringExtra("username") ?: "User"
        db = AppDatabase.getDatabase(this)
        nutritionDao = db.nutritionDao()

        val etMealName = findViewById<EditText>(R.id.etMealName)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val etProtein = findViewById<EditText>(R.id.etProtein)
        val etCarbs = findViewById<EditText>(R.id.etCarbs)
        val etFats = findViewById<EditText>(R.id.etFats)
        val etDate = findViewById<EditText>(R.id.etDate)
        val btnSave = findViewById<Button>(R.id.btnSaveNutrition)
        val btnViewMeals = findViewById<Button>(R.id.btnViewMeals)

        // Date picker
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                this,
                { _, y, m, d ->
                    etDate.setText("$d/${m + 1}/$y")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        btnSave.setOnClickListener {
            val mealName = etMealName.text.toString().trim()
            val calories = etCalories.text.toString().trim()
            val protein = etProtein.text.toString().trim()
            val carbs = etCarbs.text.toString().trim()
            val fats = etFats.text.toString().trim()
            val date = etDate.text.toString().trim()

            if (mealName.isEmpty() || calories.isEmpty() || protein.isEmpty() ||
                carbs.isEmpty() || fats.isEmpty() || date.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nutrition = Nutrition(
                username = username,
                mealName = mealName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                date = date
            )

            // Save locally and in Firebase
            lifecycleScope.launch(Dispatchers.IO) {
                nutritionDao.insertNutrition(nutrition)
                firestore.collection("users")
                    .document(username)
                    .collection("meals")
                    .add(nutrition)
                    .addOnSuccessListener {
                        Toast.makeText(this@NutritionActivity, "Meal saved online!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@NutritionActivity, "Saved offline only (no internet)", Toast.LENGTH_SHORT).show()
                    }

                runOnUiThread {
                    Toast.makeText(this@NutritionActivity, "Meal saved locally!", Toast.LENGTH_SHORT).show()
                    etMealName.text.clear()
                    etCalories.text.clear()
                    etProtein.text.clear()
                    etCarbs.text.clear()
                    etFats.text.clear()
                    etDate.text.clear()
                }
            }
        }

        btnViewMeals.setOnClickListener {
            startActivity(android.content.Intent(this, NutritionListActivity::class.java).putExtra("username", username))
        }
    }
}

