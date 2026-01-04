package com.example.computer.data.model

data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val publishTime: Long = System.currentTimeMillis()
)