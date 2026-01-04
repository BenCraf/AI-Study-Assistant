package com.example.computer.feature.teacher.presentation

import androidx.lifecycle.ViewModel
import com.example.computer.data.model.Assignment
import com.example.computer.data.model.Submission
import com.example.computer.data.model.SubmissionStatus
import com.example.computer.data.repository.AssignmentRepository
import com.example.computer.data.repository.SubmissionRepository
//import com.example.computer.data.repository.InMemoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TeacherViewModel : ViewModel() {


    val assignments = AssignmentRepository.assignments


    val submissions = SubmissionRepository.submissions


    fun gradeSubmission(
        submissionId: String,
        score: Int,
        comment: String
    ) {
        val current = submissions.value.find { it.id == submissionId } ?: return

        SubmissionRepository.update(
            current.copy(
                score = score,
                comment = comment,
                status = SubmissionStatus.GRADED
            )
        )
    }
}