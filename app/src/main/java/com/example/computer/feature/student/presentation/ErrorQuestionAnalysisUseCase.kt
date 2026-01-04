package com.example.computer.feature.student.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

data class ErrorQuestionAnalysisResult(
    val errorAnalysis: String?,
    val correctAnswer: String
)


private const val APP_ID = ""
private const val API_KEY = ""
private const val API_SECRET = ""

private const val HOST = "spark-api.cn-huabei-1.xf-yun.com"
private const val ENDPOINT_PATH = "/v2.1/image"
private const val ENDPOINT_URL = "wss://$HOST$ENDPOINT_PATH"

private const val MAX_IMAGE_WIDTH = 1024
private const val MAX_IMAGE_HEIGHT = 1024
private const val JPEG_QUALITY = 85

private val okHttpClient by lazy { OkHttpClient() }

suspend fun analyzeQuestionOnly(
    context: Context,
    questionImageUri: Uri
): ErrorQuestionAnalysisResult = withContext(Dispatchers.IO) {

    val questionImageBytes = readAndCompressSingleImage(context, questionImageUri)

    val prompt = buildString {
        appendLine("你是一个专业的学科老师，请帮我解答这道题目。你的回答应当严格遵守我们给出的格式。")
        appendLine()
        appendLine("请你提供：")
        appendLine("### 题目分析（理解题意、明确已知条件和所求内容）")
        appendLine("### 详细的解题步骤")
        appendLine("### 最终答案")
        appendLine()
        appendLine("**格式要求**：")
        appendLine("- 数学表达以及公式请使用以下的格式")
        appendLine("  * 行内公式用单个美元符号：\$公式内容\$")
        appendLine("  * 独立公式用双美元符号：\$\$公式内容\$\$")
        appendLine("- 使用 Markdown 格式组织内容（标题、列表、粗体等）")
        appendLine("- 表格用标准 Markdown：| 列1 | 列2 |")
        appendLine()
        appendLine("请严格按照上述格式用中文回答。")
    }

    val content = callImageUnderstandingApi(prompt, questionImageBytes)

    ErrorQuestionAnalysisResult(
        errorAnalysis = null,
        correctAnswer = content
    )
}

suspend fun analyzeErrorQuestion(
    context: Context,
    questionImageUri: Uri,
    wrongAnswerImageUri: Uri
): ErrorQuestionAnalysisResult = withContext(Dispatchers.IO) {

    val mergedImageBytes = mergeAndCompressTwoImages(context, questionImageUri, wrongAnswerImageUri)

    val prompt = buildString {
        appendLine("你是一个专业的学科老师，我做错了一道题，需要你帮我分析。你的回答应当严格遵守我们给出的格式。")
        appendLine()
        appendLine("结合图片整体内容（上半部分为题目，下半部分为我的错误解答），请按照以下格式回答：")
        appendLine()
        appendLine("### 错误分析")
        appendLine("（指出我的解答中哪里错了、原因是什么）")
        appendLine()
        appendLine("### 正确解答")
        appendLine("（提供详细的正确解题步骤和最终答案）")
        appendLine()
        appendLine("**格式要求**：")
        appendLine("- 数学表达以及公式请使用以下的格式")
        appendLine("  * 行内公式用单个美元符号：\$公式内容\$")
        appendLine("  * 独立公式用双美元符号：\$\$公式内容\$\$")
        appendLine("- 使用 Markdown 格式组织内容（标题、列表、粗体等）")
        appendLine("- 计算步骤逐步列出")
        appendLine("- 表格用标准 Markdown：| 列1 | 列2 |")
        appendLine()
        appendLine("请严格按照上述格式用中文回答。")
    }

    val content = callImageUnderstandingApi(prompt, mergedImageBytes)

    parseAnalysisResult(content)
}

public suspend fun callImageUnderstandingApi(
    prompt: String,
    imageBytes: ByteArray
): String = suspendCancellableCoroutine { continuation ->

    val signedUrl = buildSignedUrl()
    Log.d("IMAGE_API", "准备连接 WebSocket")

    val request = Request.Builder()
        .url(signedUrl)
        .build()

    val requestJson = buildRequestJson(prompt, imageBytes).toString()
    val accumulatedContent = StringBuilder()

    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Log.d("IMAGE_API", "✓ WebSocket 已连接")
            webSocket.send(requestJson)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // ✅ 使用 Log.e 确保能看到
            Log.e("🌐API_MESSAGE", "收到消息: $text")

            try {
                val json = JSONObject(text)
                val header = json.optJSONObject("header") ?: JSONObject()
                val code = header.optInt("code", -1)

                if (code != 0) {
                    val message = header.optString("message", "调用失败")
                    Log.e("🌐API_MESSAGE", "✗ API 错误: code=$code, message=$message")
                    finishWithError(webSocket, RuntimeException("API错误($code): $message"))
                    return
                }

                val payload = json.optJSONObject("payload") ?: return
                val choices = payload.optJSONObject("choices") ?: return
                val texts = choices.optJSONArray("text") ?: JSONArray()
                for (i in 0 until texts.length()) {
                    val item = texts.optJSONObject(i) ?: continue
                    val content = item.optString("content", "")
                    if (content.isNotBlank()) {
                        accumulatedContent.append(content)
                    }
                }

                val status = choices.optInt("status", header.optInt("status", 0))
                if (status == 2) {
                    val finalContent = accumulatedContent.toString().trim()

                    // ✅ 使用 Log.e 打印完整内容
                    Log.e("🌐API_FULL", "========================================")
                    Log.e("🌐API_FULL", "完整 API 返回 (${finalContent.length} 字符):")
                    Log.e("🌐API_FULL", finalContent)
                    Log.e("🌐API_FULL", "========================================")

                    // ✅ 检查是否包含 $content$ 模式
                    val dollarMatches = Regex("""\$\w+\$""").findAll(finalContent).toList()
                    Log.e("🌐API_FULL", "包含 \$word\$ 模式: ${dollarMatches.size} 个")
                    dollarMatches.forEach { match ->
                        Log.e("🌐API_FULL", "  - ${match.value}")
                    }
                    Log.e("🌐API_FULL", "========================================")

                    finishSuccessfully(webSocket, finalContent)
                }
            } catch (e: Exception) {
                Log.e("🌐API_MESSAGE", "✗ 解析消息失败", e)
                finishWithError(webSocket, e)
            }
        }
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            onMessage(webSocket, bytes.string(Charset.forName("UTF-8")))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e("IMAGE_API", "✗ 连接失败: ${t.message}", t)
            if (response != null) {
                Log.e("IMAGE_API", "Response Code: ${response.code}")
                Log.e("IMAGE_API", "Response Message: ${response.message}")
            }
            finishWithError(webSocket, RuntimeException("网络异常: ${t.message}", t))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("IMAGE_API", "WebSocket 已关闭: code=$code, reason=$reason")
        }

        private fun finishSuccessfully(webSocket: WebSocket, content: String) {
            if (continuation.isActive) {
                continuation.resumeWith(Result.success(content))
            }
            webSocket.close(1000, "completed")
        }

        private fun finishWithError(webSocket: WebSocket, throwable: Throwable) {
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(throwable))
            }
            webSocket.close(1001, "error")
        }
    }

    val webSocket = okHttpClient.newWebSocket(request, listener)

    continuation.invokeOnCancellation {
        webSocket.cancel()
    }
}

private fun buildRequestJson(prompt: String, imageBytes: ByteArray): JSONObject {
    val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

    val textArray = JSONArray().apply {
        put(
            JSONObject().apply {
                put("role", "user")
                put("content", imageBase64)
                put("content_type", "image")
            }
        )
        put(
            JSONObject().apply {
                put("role", "user")
                put("content", prompt)
                put("content_type", "text")
            }
        )
    }

    return JSONObject().apply {
        put("header", JSONObject().apply {
            put("app_id", APP_ID)
            put("uid", System.currentTimeMillis().toString())
        })
        put("parameter", JSONObject().apply {
            put("chat", JSONObject().apply {
                put("domain", "imagev3")
                put("temperature", 0.7)
                put("top_k", 4)
                put("max_tokens", 2048)
            })
        })
        put("payload", JSONObject().apply {
            put("message", JSONObject().apply {
                put("text", textArray)
            })
        })
    }
}

private fun buildSignedUrl(): String {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    val date = dateFormat.format(Date())

    val requestLine = "GET $ENDPOINT_PATH HTTP/1.1"
    val signatureOrigin = "host: $HOST\ndate: $date\n$requestLine"

    Log.d("IMAGE_API", "===== 鉴权调试信息 =====")
    Log.d("IMAGE_API", "APP_ID: $APP_ID")
    Log.d("IMAGE_API", "API_KEY: $API_KEY")
    Log.d("IMAGE_API", "Date: $date")
    Log.d("IMAGE_API", "Signature Origin:\n$signatureOrigin")

    val mac = Mac.getInstance("HmacSHA256").apply {
        init(SecretKeySpec(API_SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256"))
    }
    val signatureBytes = mac.doFinal(signatureOrigin.toByteArray(Charsets.UTF_8))
    val signature = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)

    Log.d("IMAGE_API", "Signature: $signature")

    // ✅ 修复：正确的 authorization 格式
    val authorizationOrigin = "api_key=\"$API_KEY\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$signature\""

    Log.d("IMAGE_API", "Authorization Origin: $authorizationOrigin")

    val authorization = Base64.encodeToString(
        authorizationOrigin.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP
    )

    Log.d("IMAGE_API", "Authorization Base64: $authorization")

    val encodedAuthorization = URLEncoder.encode(authorization, "UTF-8")
    val encodedDate = URLEncoder.encode(date, "UTF-8")
    val encodedHost = URLEncoder.encode(HOST, "UTF-8")

    val finalUrl = "$ENDPOINT_URL?authorization=$encodedAuthorization&date=$encodedDate&host=$encodedHost"

    Log.d("IMAGE_API", "Final URL: $finalUrl")
    Log.d("IMAGE_API", "========================")

    return finalUrl
}

// ... 其余代码保持不变 ...

public fun readAndCompressSingleImage(
    context: Context,
    imageUri: Uri
): ByteArray {
    val bitmap = decodeScaledBitmap(context, imageUri)
    return bitmapToJpegBytes(bitmap, JPEG_QUALITY)
}

private fun mergeAndCompressTwoImages(
    context: Context,
    firstUri: Uri,
    secondUri: Uri
): ByteArray {
    val first = decodeScaledBitmap(context, firstUri)
    val second = decodeScaledBitmap(context, secondUri)
    val merged = combineBitmapsVertically(first, second)
    val finalBitmap = ensureMaxSize(merged, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
    return bitmapToJpegBytes(finalBitmap, JPEG_QUALITY)
}

private fun decodeScaledBitmap(
    context: Context,
    imageUri: Uri
): Bitmap {
    val original = context.contentResolver.openInputStream(imageUri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: throw IOException("无法读取图片数据: $imageUri")

    val scale = calculateScale(original.width, original.height, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
    if (scale >= 1f) return original

    val scaled = Bitmap.createScaledBitmap(
        original,
        (original.width * scale).toInt().coerceAtLeast(1),
        (original.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (!original.isRecycled) original.recycle()
    return scaled
}

public fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val bytes = outputStream.toByteArray()
    outputStream.close()
    if (!bitmap.isRecycled) bitmap.recycle()
    val sizeKB = bytes.size / 1024
    Log.d("IMAGE_COMPRESS", "压缩后图片大小: ${sizeKB}KB")
    return bytes
}

private fun combineBitmapsVertically(top: Bitmap, bottom: Bitmap): Bitmap {
    val width = maxOf(top.width, bottom.width)
    val totalHeight = top.height + bottom.height
    val combined = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(combined)
    canvas.drawColor(Color.WHITE)

    val topOffsetX = ((width - top.width) / 2f)
    val bottomOffsetX = ((width - bottom.width) / 2f)

    canvas.drawBitmap(top, topOffsetX, 0f, null)
    canvas.drawBitmap(bottom, bottomOffsetX, top.height.toFloat(), null)

    if (!top.isRecycled) top.recycle()
    if (!bottom.isRecycled) bottom.recycle()

    return combined
}

private fun ensureMaxSize(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val scale = calculateScale(bitmap.width, bitmap.height, maxWidth, maxHeight)
    if (scale >= 1f) return bitmap

    val resized = Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * scale).toInt().coerceAtLeast(1),
        (bitmap.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (!bitmap.isRecycled) bitmap.recycle()
    return resized
}

private fun calculateScale(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Float {
    val scaleWidth = maxWidth.toFloat() / width
    val scaleHeight = maxHeight.toFloat() / height
    return min(1.0f, min(scaleWidth, scaleHeight))
}

private fun parseAnalysisResult(content: String): ErrorQuestionAnalysisResult {
    val trimmedContent = content.trim()

    // ✅ 支持 Markdown 标题格式
    val errorAnalysisPart = extractSection(trimmedContent, "### 错误分析", "### 正确解答")
        ?: extractSection(trimmedContent, "【错误分析】", "【正确解答】")
        ?: extractSection(trimmedContent, "错误分析", "正确解答")

    val correctAnswerPart = extractSection(trimmedContent, "### 正确解答", null)
        ?: extractSection(trimmedContent, "【正确解答】", null)
        ?: extractSection(trimmedContent, "正确解答", null)

    if (errorAnalysisPart == null || correctAnswerPart == null) {
        // 备用方案：按比例分割
        val lines = trimmedContent.lines()
        val midPoint = lines.size / 2
        return ErrorQuestionAnalysisResult(
            errorAnalysis = lines.take(midPoint).joinToString("\n").trim(),
            correctAnswer = lines.drop(midPoint).joinToString("\n").trim()
        )
    }

    return ErrorQuestionAnalysisResult(
        errorAnalysis = errorAnalysisPart,
        correctAnswer = correctAnswerPart
    )
}

private fun extractSection(text: String, startMarker: String, endMarker: String?): String? {
    val startIndex = text.indexOf(startMarker)
    if (startIndex == -1) return null

    val contentStart = startIndex + startMarker.length
    val contentEnd = if (endMarker != null) {
        val endIndex = text.indexOf(endMarker, contentStart)
        if (endIndex == -1) text.length else endIndex
    } else {
        text.length
    }
    return text.substring(contentStart, contentEnd).trim()
}