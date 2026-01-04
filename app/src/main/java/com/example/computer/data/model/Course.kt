package com.example.computer.data.model

data class Course(
    val id: String,
    val name: String,
    val code: String,
    val credits: Int,
    val semester: String,
    val description: String,
    val teacherId: String,
    val studentCount: Int = 0
)