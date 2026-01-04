package com.example.computer.data.model

data class LearningData(
    val totalLearningTime: String,
    val skillLevel: String,
    val reviewProgress: Float, // 0.0 到 1.0
    val dailyGoal: String,
    val streakDays: Int,
    val completedCourses: Int,
    val averageScore: String
)
