package com.example.fittrack

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 🔹 Existing user endpoints
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserDTO>

    @POST("users")
    suspend fun createUser(@Body user: UserDTO): UserDTO

    // 🔹 New workout endpoints
    @GET("workouts")
    suspend fun getWorkouts(): List<WorkoutDTO>

    @POST("workouts")
    suspend fun createWorkout(@Body workout: WorkoutDTO): WorkoutDTO
}
