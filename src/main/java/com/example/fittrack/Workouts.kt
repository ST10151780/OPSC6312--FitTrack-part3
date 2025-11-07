package com.example.fittrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val name: String,
    val duration: String,
    val calories: String,
    val notes: String?,
    val date: String
)