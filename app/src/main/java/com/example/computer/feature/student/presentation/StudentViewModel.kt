package com.example.computer.feature.student.presentation

import androidx.lifecycle.ViewModel
import com.example.computer.data.model.Assignment
import com.example.computer.data.model.Submission
import com.example.computer.data.model.SubmissionStatus
import com.example.computer.data.repository.AssignmentRepository
import com.example.computer.data.repository.SubmissionRepository
import java.util.UUID

class StudentViewModel : ViewModel() {

    val assignments =
        AssignmentRepository.assignments

    fun submitAssignment(
        assignment: Assignment,
        text: String,
        imageUris: List<String>
    ) {
        val submission = Submission(
            id = UUID.randomUUID().toString(),
            assignmentId = assignment.id,   // ⚠️ 关键
            studentId = "student_001",
            studentName = "张三",
            content = text,
            imageUris = imageUris,
            status = SubmissionStatus.SUBMITTED,
            score = null,
            comment = null
        )

        SubmissionRepository.submit(submission)
    }
}