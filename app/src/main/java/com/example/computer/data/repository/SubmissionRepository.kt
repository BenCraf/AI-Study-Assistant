package com.example.computer.data.repository

import com.example.computer.data.model.Submission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SubmissionRepository {

    private val _submissions = MutableStateFlow<List<Submission>>(emptyList())
    val submissions: StateFlow<List<Submission>> = _submissions.asStateFlow()

    fun submit(submission: Submission) {
        _submissions.value = _submissions.value + submission
    }

    fun update(submission: Submission) {
        _submissions.value = _submissions.value.map {
            if (it.id == submission.id) submission else it
        }
    }
}