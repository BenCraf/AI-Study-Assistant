package com.example.computer.data.repository

import com.example.computer.data.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StudentRepository {
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    init {
        // 初始化一些示例数据
        _students.value = listOf(
            Student(
                id = "1",
                studentId = "2021001",
                name = "张三",
                className = "计算机21-1班",
                email = "zhangsan@example.com",
                phone = "13800138001",
                gender = "男",
                major = "计算机科学与技术"
            ),
            Student(
                id = "2",
                studentId = "2021002",
                name = "李四",
                className = "计算机21-1班",
                email = "lisi@example.com",
                phone = "13800138002",
                gender = "女",
                major = "计算机科学与技术"
            ),
            Student(
                id = "3",
                studentId = "2021003",
                name = "王五",
                className = "计算机21-2班",
                email = "wangwu@example.com",
                phone = "13800138003",
                gender = "男",
                major = "软件工程"
            ),
            Student(
                id = "4",
                studentId = "2021004",
                name = "赵六",
                className = "计算机21-2班",
                email = "zhaoliu@example.com",
                phone = "13800138004",
                gender = "女",
                major = "软件工程"
            ),
            Student(
                id = "5",
                studentId = "2021005",
                name = "孙七",
                className = "计算机21-3班",
                email = "sunqi@example.com",
                phone = "13800138005",
                gender = "男",
                major = "网络工程"
            )
        )
    }

    fun addStudent(student: Student) {
        _students.value = _students.value + student
    }

    fun updateStudent(student: Student) {
        _students.value = _students.value.map {
            if (it.id == student.id) student else it
        }
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }
    }

    fun getStudentById(id: String): Student? {
        return _students.value.find { it.id == id }
    }

    fun getStudentsByClass(className: String): List<Student> {
        return _students.value.filter { it.className == className }
    }

    fun searchStudents(query: String): List<Student> {
        return _students.value.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.studentId.contains(query, ignoreCase = true) ||
                    it.className.contains(query, ignoreCase = true)
        }
    }
}