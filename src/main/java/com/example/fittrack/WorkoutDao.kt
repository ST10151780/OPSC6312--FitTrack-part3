package com.example.fittrack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Query("SELECT * FROM workouts WHERE username = :username ORDER BY id DESC")
    suspend fun getWorkoutsForUser(username: String): List<Workout>
}