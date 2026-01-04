package com.example.computer.feature.common.domain

import com.example.computer.core.network.httpClient
import com.example.computer.data.model.LearningData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


//api 请用自己的
private const val AI_API_KEY =
    "sk-1NyoEXMJk2mqp3eO74C58c73A1964f339dD3BbD15bD1901d"
private const val AI_BASE_URL = "http://maas-api.cn-huabei-1.xf-yun.com/v1"
private const val AI_MODEL_ID = "xop3qwen1b7"

suspend fun requestLearningSuggestions(learningData: LearningData): String =
    withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("你是一个学生学习助手，请根据下面的学习数据，用中文给出 3 条简洁、具体的学习建议：")
            appendLine("总学习时间: ${learningData.totalLearningTime}")
            appendLine("技能等级: ${learningData.skillLevel}")
            appendLine("复习进度: ${(learningData.reviewProgress * 100).toInt()}%")
            appendLine("每日目标: ${learningData.dailyGoal}")
            appendLine("连续学习天数: ${learningData.streakDays}")
            appendLine("完成课程数: ${learningData.completedCourses}")
            appendLine("平均成绩: ${learningData.averageScore}")
        }

        val messagesJson = JSONArray().apply {
            put(
                JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            )
        }

        val requestJson = JSONObject().apply {
            put("model", AI_MODEL_ID)
            put("messages", messagesJson)
            put("temperature", 0.7)
            put("max_tokens", 512)
        }

        val mediaType = "application/json".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$AI_BASE_URL/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $AI_API_KEY")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code} ${response.message}")
            }

            val respText = response.body?.string() ?: throw RuntimeException("空响应")
            val root = JSONObject(respText)
            val choices = root.optJSONArray("choices")
                ?: throw RuntimeException("响应中没有 choices")
            if (choices.length() == 0) {
                throw RuntimeException("响应中没有 choices")
            }

            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            val content = message.optString("content", "")
            if (content.isNullOrBlank()) {
                throw RuntimeException("模型没有返回内容")
            }
            content.trim()
        }
    }