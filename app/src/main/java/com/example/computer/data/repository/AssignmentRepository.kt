package com.example.computer.data.repository

import com.example.computer.data.model.Assignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AssignmentRepository {

    // 所有老师发布的作业（全局唯一）
    private val _assignments =
        MutableStateFlow<List<Assignment>>(emptyList())

    val assignments: StateFlow<List<Assignment>> = _assignments

    /** 老师发布作业 */
    fun publishAssignment(assignment: Assignment) {
        _assignments.value = _assignments.value + assignment
    }

    /** 清空（调试用） */
    fun clearAll() {
        _assignments.value = emptyList()
    }

    /** 根据 id 查找 */
    fun getAssignmentById(id: String): Assignment? {
        return _assignments.value.find { it.id == id }
    }
}