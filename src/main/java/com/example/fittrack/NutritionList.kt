package com.example.fittrack

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NutritionListActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var nutritionDao: NutritionDao
    private lateinit var username: String
    private lateinit var container: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_list)

        db = AppDatabase.getDatabase(this)
        nutritionDao = db.nutritionDao()
        username = intent.getStringExtra("username") ?: "User"
        container = findViewById(R.id.nutritionContainer)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        tvHeader.text = "Saved Meals"
        btnBack.setOnClickListener { finish() }

        loadFromFirestore()
    }

    private fun loadFromFirestore() {
        firestore.collection("users")
            .document(username)
            .collection("meals")
            .get()
            .addOnSuccessListener { result ->
                val meals = result.toObjects(Nutrition::class.java)
                if (meals.isEmpty()) {
                    loadFromRoom()
                } else {
                    displayMeals(meals)
                }
            }
            .addOnFailureListener {
                loadFromRoom()
            }
    }

    private fun loadFromRoom() {
        lifecycleScope.launch(Dispatchers.IO) {
            val meals = nutritionDao.getNutritionForUser(username)
            withContext(Dispatchers.Main) {
                displayMeals(meals)
            }
        }
    }

    private fun displayMeals(meals: List<Nutrition>) {
        container.removeAllViews()
        if (meals.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No meals logged yet."
                textSize = 16f
                setTextColor(android.graphics.Color.DKGRAY)
            }
            container.addView(emptyText)
            return
        }

        for (meal in meals) {
            val itemView = layoutInflater.inflate(R.layout.item_nutrition, container, false)

            val tvMealName = itemView.findViewById<TextView>(R.id.tvMealName)
            val layoutDetails = itemView.findViewById<LinearLayout>(R.id.layoutDetails)
            val tvCalories = itemView.findViewById<TextView>(R.id.tvCalories)
            val tvProtein = itemView.findViewById<TextView>(R.id.tvProtein)
            val tvCarbs = itemView.findViewById<TextView>(R.id.tvCarbs)
            val tvFats = itemView.findViewById<TextView>(R.id.tvFats)
            val tvDate = itemView.findViewById<TextView>(R.id.tvDate)

            tvMealName.text = meal.mealName
            tvCalories.text = "Calories: ${meal.calories}"
            tvProtein.text = "Protein: ${meal.protein}g"
            tvCarbs.text = "Carbs: ${meal.carbs}g"
            tvFats.text = "Fats: ${meal.fats}g"
            tvDate.text = "Date: ${meal.date}"

            layoutDetails.visibility = LinearLayout.GONE
            tvMealName.setOnClickListener {
                layoutDetails.visibility =
                    if (layoutDetails.visibility == LinearLayout.VISIBLE)
                        LinearLayout.GONE else LinearLayout.VISIBLE
            }

            container.addView(itemView)
        }
    }
}

