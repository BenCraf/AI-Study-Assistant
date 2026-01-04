package com.example.computer.data.model

data class GradeEntry(
    val id: String,
    val studentId: String,
    val courseId: String,
    val score: Int?,
    val remarks: String = ""
)
