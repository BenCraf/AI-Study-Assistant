package com.example.computer.data.repository

import com.example.computer.data.model.GradeEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GradeRepository {
    private val _grades = MutableStateFlow<List<GradeEntry>>(emptyList())
    val grades: StateFlow<List<GradeEntry>> = _grades.asStateFlow()

    init {
        // 初始化一些示例数据
        _grades.value = listOf(
            GradeEntry(
                id = "1",
                studentId = "1",
                courseId = "1",
                score = 85,
                remarks = "表现良好"
            ),
            GradeEntry(
                id = "2",
                studentId = "2",
                courseId = "1",
                score = 92,
                remarks = "优秀"
            )
        )
    }

    fun saveGrade(grade: GradeEntry) {
        val currentGrades = _grades.value.toMutableList()
        val existingIndex = currentGrades.indexOfFirst {
            it.studentId == grade.studentId && it.courseId == grade.courseId
        }

        if (existingIndex != -1) {
            currentGrades[existingIndex] = grade
        } else {
            currentGrades.add(grade)
        }

        _grades.value = currentGrades
    }

    fun getGradesByStudent(studentId: String): List<GradeEntry> {
        return _grades.value.filter { it.studentId == studentId }
    }

    fun getGradesByCourse(courseId: String): List<GradeEntry> {
        return _grades.value.filter { it.courseId == courseId }
    }

    fun deleteGrade(gradeId: String) {
        _grades.value = _grades.value.filter { it.id != gradeId }
    }
}
