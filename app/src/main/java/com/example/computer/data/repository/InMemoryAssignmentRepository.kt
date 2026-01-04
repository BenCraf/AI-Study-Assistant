//package com.example.computer.data.repository
//
//import com.example.computer.data.model.Assignment
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.update
//
//object InMemoryAssignmentRepository : AssignmentRepository {
//
//    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
//    private val _submissions = MutableStateFlow<Map<String, String>>(emptyMap())
//
//    override val assignments: StateFlow<List<Assignment>> = _assignments
//    override val submissions: StateFlow<Map<String, String>> = _submissions
//
//    override fun addAssignment(assignment: Assignment) {
//        _assignments.update { current ->
//            if (current.none { it.title == assignment.title }) {
//                current + assignment
//            } else {
//                current.map { if (it.title == assignment.title) assignment else it }
//            }
//        }
//    }
//
//    override fun submitAssignment(assignmentTitle: String, content: String) {
//        _submissions.update { current ->
//            current + (assignmentTitle to content)
//        }
//    }
//
//    override fun clear() {
//        _assignments.value = emptyList()
//        _submissions.value = emptyMap()
//    }
//}
