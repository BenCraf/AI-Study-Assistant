package com.example.computer.data.model

data class Student(
    val id: String,
    val studentId: String,        // 学号
    val name: String,
    val className: String,        // 班级
    val email: String = "",
    val phone: String = "",
    val gender: String = "男",
    val major: String = ""        // 专业
)