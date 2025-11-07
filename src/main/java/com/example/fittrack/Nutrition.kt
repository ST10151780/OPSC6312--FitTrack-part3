package com.example.fittrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition")
data class Nutrition(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val mealName: String,
    val calories: String,
    val protein: String,
    val carbs: String,
    val fats: String,
    val date: String
)
