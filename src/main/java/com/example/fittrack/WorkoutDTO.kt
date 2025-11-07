package com.example.fittrack

data class WorkoutDTO(
    val username: String,
    val workoutName: String,
    val duration: Int,
    val calories: Int,
    val notes: String? = null
)