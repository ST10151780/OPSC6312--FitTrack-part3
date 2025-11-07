package com.example.fittrack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NutritionDao {
    @Insert
    suspend fun insertNutrition(nutrition: Nutrition)

    @Query("SELECT * FROM nutrition WHERE username = :username ORDER BY date DESC")
    suspend fun getNutritionForUser(username: String): List<Nutrition>
}
