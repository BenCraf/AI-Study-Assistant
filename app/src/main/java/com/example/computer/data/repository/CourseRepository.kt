package com.example.computer.data.repository

import com.example.computer.data.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object CourseRepository {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses = _courses.asStateFlow()

    fun addCourse(course: Course) {
        _courses.value = _courses.value + course
    }

    fun updateCourse(course: Course) {
        _courses.value = _courses.value.map {
            if (it.id == course.id) course else it
        }
    }

    fun deleteCourse(courseId: String) {
        _courses.value = _courses.value.filter { it.id != courseId }
    }

    fun getCourseById(courseId: String): Course? {
        return _courses.value.find { it.id == courseId }
    }
}