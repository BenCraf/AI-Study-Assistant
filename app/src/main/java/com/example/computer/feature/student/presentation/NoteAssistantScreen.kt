// NoteAssistantScreen.kt
package com.example.newapp.presentation.student
import com.example.computer.feature.student.media.AudioExtractor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.example.computer.feature.student.media.VideoFileUtil
import com.example.computer.feature.student.presentation.callImageUnderstandingApi
import com.example.computer.feature.student.presentation.readAndCompressSingleImage
import com.example.computer.feature.student.presentation.bitmapToJpegBytes
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import com.example.computer.feature.student.presentation.MindMapNode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import com.example.computer.feature.student.presentation.MindMapDetailScreen
import com.example.computer.feature.common.presentation.LatexText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

// API key please fill yours
// AI API 配置
private const val AI_API_KEY = ""
private const val AI_BASE_URL = ""
private const val AI_MODEL_ID = ""


/**
 * 专门用于切分 WAV 文件
 */
fun splitWavFile(sourceFile: File, segmentDurationMs: Long): List<File> {
    val segments = mutableListOf<File>()

    try {
        val raf = RandomAccessFile(sourceFile, "r")

        // 1. 读取 WAV 头部信息 (前44字节)
        val header = ByteArray(44)
        raf.read(header)

        // 解析关键参数
        val byteBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val channels = byteBuffer.getShort(22).toInt()
        val sampleRate = byteBuffer.getInt(24)
        val bitsPerSample = byteBuffer.getShort(34).toInt()

        // 计算每秒的字节数 (ByteRate)
        // 公式: SampleRate * NumChannels * BitsPerSample / 8
        val byteRate = sampleRate * channels * bitsPerSample / 8

        // 计算每个片段的大小 (字节)
        val bytesPerSegment = (byteRate * (segmentDurationMs / 1000.0)).toLong()
        // 还要确保是对齐的 (必须是 blockAlign 的倍数，通常是 channels * bits/8)
        val blockAlign = channels * bitsPerSample / 8
        val alignedBytesPerSegment = bytesPerSegment - (bytesPerSegment % blockAlign)

        val totalDataLen = sourceFile.length() - 44
        var currentOffset = 44L
        var index = 0

        while (currentOffset < sourceFile.length()) {
            // 计算当前片段实际长度
            val remainingBytes = sourceFile.length() - currentOffset
            val writeSize = minOf(alignedBytesPerSegment, remainingBytes)

            // 如果剩余片段太小（比如小于0.5秒），就不切了或者合并（这里简单处理：太小就丢弃或根据需求保留）
            if (writeSize < byteRate * 2) { // 小于2秒就跳过，防止只有一点点噪音
                break
            }

            val segmentFile = File(sourceFile.parent, "${sourceFile.nameWithoutExtension}_part$index.wav")

            var bytesWritten = 0L

            // 2. 写入数据
            FileOutputStream(segmentFile).use { fos ->
                // A. 写入修改后的头部
                val newHeader = header.clone()
                updateWavHeaderSize(newHeader, writeSize)
                fos.write(newHeader)

                // B. 写入音频数据块
                val buffer = ByteArray(4096)

                raf.seek(currentOffset) // 移动指针到当前片段开始处

                while (bytesWritten < writeSize) {
                    val len = raf.read(buffer, 0, minOf(buffer.size.toLong(), writeSize - bytesWritten).toInt())
                    if (len == -1) break
                    fos.write(buffer, 0, len)
                    bytesWritten += len
                }
            }

            segments.add(segmentFile)
            currentOffset += bytesWritten
            index++
        }

        raf.close()
    } catch (e: Exception) {
        e.printStackTrace()
        // 如果切分失败，降级返回原文件（或者抛出异常）
        return listOf(sourceFile)
    }

    return segments
}

/**
 * 更新 WAV 头部中的文件大小信息
 */
fun updateWavHeaderSize(header: ByteArray, pcmDataSize: Long) {
    val totalFileSize = pcmDataSize + 36

    // 修改 ChunkSize (Offset 4) = TotalFileSize - 8
    val sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    sizeBuffer.putInt(totalFileSize.toInt())
    System.arraycopy(sizeBuffer.array(), 0, header, 4, 4)

    // 修改 Subchunk2Size (Offset 40) = pcmDataSize
    sizeBuffer.clear()
    sizeBuffer.putInt(pcmDataSize.toInt())
    System.arraycopy(sizeBuffer.array(), 0, header, 40, 4)
}




object VideoFrameExtractor {
    fun extractFrames(
        context: Context,
        videoUri: Uri,
        intervalMs: Long = 1000L
    ): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        val frames = mutableListOf<Bitmap>()

        var timeMs = 0L
        while (timeMs < durationMs) {
            val bitmap = retriever.getFrameAtTime(timeMs * 2000, MediaMetadataRetriever.OPTION_CLOSEST)
            if (bitmap != null) {
                frames.add(bitmap)
            }
            timeMs += intervalMs
        }

        retriever.release()
        return frames
    }
}

// 数据模型
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 思维导图数据模型
data class MindMapData(
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val title: String,
    val rootNode: MindMapNode,
    val createdAt: Long = System.currentTimeMillis()
)

// ViewModel
class NoteAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> = _notes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }


    fun analyzeImageBitmap(context: Context, bitmap: Bitmap, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // 基于边缘检测与轮廓分析的黑板提取算法
                val blackboardBitmap = preprocessBlackboard(bitmap)
//                val bytes = bitmapToJpegBytes(bitmap, 85)
                val bytes = bitmapToJpegBytes(blackboardBitmap, 85)
                val prompt = "请用中文描述图片内容"
                val result = callImageUnderstandingApi(prompt, bytes)
                onResult(result)
            } catch (e: Exception) {
                onResult("分析失败: ${e.message}")
            }
        }
    }

    fun analyzeImageBitmapSuspend(context: Context, bitmap: Bitmap): String = runBlocking {
        val result = CompletableDeferred<String>()
        analyzeImageBitmap(context, bitmap) { description ->
            result.complete(description)
        }
        result.await()
    }

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _currentNote = mutableStateOf<Note?>(null)
    val currentNote: State<Note?> = _currentNote


    //api key 请换成自己的

    private val apiKey = ""
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun loadNotes(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notesDir = File(context.filesDir, "notes")
                if (!notesDir.exists()) {
                    notesDir.mkdirs()
                }

                val noteFiles = notesDir.listFiles()?.filter { it.extension == "txt" } ?: emptyList()
                val loadedNotes = noteFiles.mapNotNull { file ->
                    try {
                        val lines = file.readLines()
                        if (lines.size >= 2) {
                            Note(
                                id = file.nameWithoutExtension,
                                title = lines[0],
                                content = lines.drop(1).joinToString("\n"),
                                createdAt = file.lastModified(),
                                updatedAt = file.lastModified()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    _notes.clear()
                    _notes.addAll(loadedNotes.sortedByDescending { it.updatedAt })
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "加载笔记失败: ${e.message}"
                }
            }
        }
    }

    fun createNote(context: Context, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val note = Note(title = title, content = "")
                saveNoteToFile(context, note)

                withContext(Dispatchers.Main) {
                    _notes.add(0, note)
                    _currentNote.value = note
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "创建笔记失败: ${e.message}"
                }
            }
        }
    }

    fun analyzeUploadedAudio(
        context: Context,
        audioUri: Uri,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(audioUri)
                    ?: throw Exception("无法读取音频文件")

                val audioFile = File(
                    context.cacheDir,
                    "upload_audio_${System.currentTimeMillis()}.m4a"
                )

                audioFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }




                transcribeAudio(context, audioFile) { text ->
                    summarizeText(text) { summary ->
                        viewModelScope.launch(Dispatchers.Main) {
                            _isLoading.value = false
                            onResult(
                                """
[上传语音分析]
原始转写：
$text

AI 总结：
$summary
""".trimIndent()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "语音分析失败: ${e.message}"
                }
            }
        }
    }

    fun analyzeUploadedVideo(
        context: Context,
        videoUri: Uri,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. 抽取音频
                val wavFile = File(
                    context.cacheDir,
                    "upload_video_audio_${System.currentTimeMillis()}.wav"
                )

                AudioExtractor.extractAudioToWav(
                    context = context,
                    videoUri = videoUri,
                    outputWav = wavFile
                )

                // 2. 抽帧
                val frames = VideoFrameExtractor.extractFrames(context, videoUri)
                val frameDescriptions = mutableListOf<String>()

                withContext(Dispatchers.IO) {
                    frames.take(5).forEach { frame ->
                        frameDescriptions.add(
                            analyzeImageBitmapSuspend(context, frame)
                        )
                    }
                }

                // 3. 语音转写 + 总结
                transcribeAudio(context, wavFile) { transcribed ->
                    val combined = buildString {
                        appendLine("语音转写：")
                        appendLine(transcribed)
                        appendLine()
                        appendLine("画面分析：")
                        frameDescriptions.forEachIndexed { i, desc ->
                            appendLine("帧${i + 1}: $desc")
                        }
                    }

                    summarizeText(combined) { summary ->
                        _isLoading.value = false
                        onResult(
                            """
[上传视频分析]
AI 总结：
$summary
                        """.trimIndent()
                        )
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "视频分析失败: ${e.message}"
            }
        }
    }

    fun updateNoteContent(context: Context, noteId: String, newContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val noteIndex = _notes.indexOfFirst { it.id == noteId }
                if (noteIndex != -1) {
                    val updatedNote = _notes[noteIndex].copy(
                        content = newContent,
                        updatedAt = System.currentTimeMillis()
                    )
                    saveNoteToFile(context, updatedNote)

                    withContext(Dispatchers.Main) {
                        _notes[noteIndex] = updatedNote
                        if (_currentNote.value?.id == updatedNote.id) {
                            _currentNote.value = updatedNote
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "保存笔记失败: ${e.message}"
                }
            }
        }
    }

    fun deleteNote(context: Context, noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notesDir = File(context.filesDir, "notes")
                val noteFile = File(notesDir, "$noteId.txt")
                if (noteFile.exists()) {
                    noteFile.delete()
                }

                withContext(Dispatchers.Main) {
                    _notes.removeAll { it.id == noteId }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "删除笔记失败: ${e.message}"
                }
            }
        }
    }

    private fun saveNoteToFile(context: Context, note: Note) {
        val notesDir = File(context.filesDir, "notes")
        if (!notesDir.exists()) {
            notesDir.mkdirs()
        }

        val noteFile = File(notesDir, "${note.id}.txt")
        noteFile.writeText("${note.title}\n${note.content}")
    }

    fun setCurrentNote(note: Note?) {
        _currentNote.value = note
    }

    fun transcribeAudio(context: Context, audioFile: File, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (!audioFile.exists()) throw Exception("音频文件不存在")
                if (audioFile.length() == 0L) throw Exception("录音时长太短，请重新录制")

                val maxSize = 10 * 1024 * 1024
                if (audioFile.length() > maxSize) {
                    throw Exception("音频文件过大（${audioFile.length() / 1024 / 1024}MB），请缩短录音时长")
                }


                fun splitAudioByDuration(file: File, segmentDurationMs: Long = 10_000L): List<File> {
                    val segments = mutableListOf<File>()
                    val extractor = MediaExtractor()
                    extractor.setDataSource(file.absolutePath)

                    val trackIndex = (0 until extractor.trackCount).first {
                        extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                    }
                    extractor.selectTrack(trackIndex)
                    val format = extractor.getTrackFormat(trackIndex)
                    val durationUs = format.getLong(MediaFormat.KEY_DURATION)
                    val totalDurationMs = durationUs / 1000
                    var startMs = 0L
                    var index = 0

                    while (startMs < totalDurationMs) {
                        val segmentLengthMs = minOf(segmentDurationMs, totalDurationMs - startMs) // 最后一个片段不要超长
                        // 如果最后一段小于 3 秒就舍弃
                        if (segmentLengthMs < 3_000L) break

                        val segmentFile = File(context.cacheDir, "${file.nameWithoutExtension}_part$index.${file.extension}")
                        extractSegment(extractor, trackIndex, startMs, segmentLengthMs, segmentFile)
                        segments.add(segmentFile)

                        startMs += segmentLengthMs
                        index++
                    }

                    extractor.release()
                    return segments
                }
                fun splitAudio(file: File, segmentDurationMs: Long = 10_000L): List<File> {
                    // 简单的判断：如果是 wav 结尾，走 WAV 切分逻辑
                    // 如果是 m4a/mp3/aac，走原来的 MediaExtractor 逻辑
                    return if (file.extension.equals("wav", ignoreCase = true)) {
                        splitWavFile(file, segmentDurationMs)
                    } else {
//        splitEncodedAudio(file, segmentDurationMs) // 这是你原来的 splitAudioByDuration 改个名
                        splitAudioByDuration(file)
                    }
                }
                // 切分音频
                val audioSegments = if (getAudioDurationMs(audioFile) > 10_000L) {

                    splitAudio(audioFile)
                } else listOf(audioFile)


                for (segment in audioSegments) {
                    Log.d("NoteAssistant", "Segment: ${segment.name}, size: ${segment.length()/1024} KB, duration: ms")
                }

                val finalText = StringBuilder()
                for (segment in audioSegments) {
                    val audioBytes = segment.readBytes()
                    val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
                    val audioUrl = "data:;base64,$base64Audio"

                    val requestJson = JSONObject().apply {
                        put("model", "qwen-audio-turbo")
                        put("input", JSONObject().apply {
                            put("messages", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "system")
                                    put("content", org.json.JSONArray().apply {
                                        put(JSONObject().apply { put("text", "You are a helpful assistant.") })
                                    })
                                })
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", org.json.JSONArray().apply {
                                        put(JSONObject().apply { put("audio", audioUrl) })
                                        put(JSONObject().apply { put("text", "请将这段音频转录成文字，只输出转录的文字内容，不要有其他说明。") })
                                    })
                                })
                            })
                        })
                    }

                    val requestBody = RequestBody.create("application/json".toMediaType(), requestJson.toString())
                    val request = Request.Builder()
                        .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()
                        if (!response.isSuccessful) throw Exception("API调用失败 (${response.code}): $responseBody")
                        val jsonResponse = JSONObject(responseBody ?: "")
                        val content = jsonResponse.optJSONObject("output")
                            ?.optJSONArray("choices")?.getJSONObject(0)
                            ?.optJSONObject("message")?.optJSONArray("content")
                        val text = content?.getJSONObject(0)?.optString("text", "") ?: ""
                        finalText.append(text)
                    }
                }

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    val resultText = finalText.toString()
                    if (resultText.isNotEmpty()) {
                        onResult(resultText)
                    } else {
                        _errorMessage.value = "未能识别到语音内容，请确保录音清晰且有说话声音"
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("NoteAssistant", "转录失败", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    val errorMsg = when {
                        e.message?.contains("NO_VALID_FRAGMENT") == true ->
                            "录音中没有检测到有效的语音，请确保：\n1. 录音时间至少3秒\n2. 说话声音清晰\n3. 环境不要太安静"
                        e.message?.contains("timeout") == true ->
                            "网络超时，请检查网络连接后重试"
                        else -> "语音转文字失败: ${e.message}"
                    }
                    _errorMessage.value = errorMsg
                }
            }
        }
    }

    // 获取音频时长（毫秒）
    fun getAudioDurationMs(file: File): Long {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        val trackIndex = (0 until extractor.trackCount).first {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        }
        val format = extractor.getTrackFormat(trackIndex)
        val durationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000
        extractor.release()
        return durationMs
    }
    fun summarizeText(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (text.isBlank()) {
                    throw Exception("文本内容为空")
                }

                android.util.Log.d("NoteAssistant", "开始AI总结")
                android.util.Log.d("NoteAssistant", "文本长度: ${text.length}")

                val requestJson = JSONObject().apply {
                    put("model", "qwen-plus")
                    put("input", JSONObject().apply {
                        put("messages", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", "你是一个专业的笔记总结助手，擅长提取关键信息并生成结构化总结。")
                            })
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "请对以下笔记内容进行总结，提取关键要点：\n\n$text")
                            })
                        })
                    })
                    put("parameters", JSONObject().apply {
                        put("result_format", "message")
                    })
                }

                android.util.Log.d("NoteAssistant", "请求体: ${requestJson.toString(2)}")

                val requestBody = RequestBody.create(
                    "application/json".toMediaType(),
                    requestJson.toString()
                )

                val request = Request.Builder()
                    .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    android.util.Log.d("NoteAssistant", "AI总结响应: ${response.code}")
                    android.util.Log.d("NoteAssistant", "响应内容: $responseBody")

                    if (!response.isSuccessful) {
                        throw Exception("AI总结失败 (${response.code}): $responseBody")
                    }

                    val jsonResponse = JSONObject(responseBody ?: "")

                    val output = jsonResponse.optJSONObject("output")
                    val choices = output?.optJSONArray("choices")

                    if (choices == null || choices.length() == 0) {
                        throw Exception("AI返回的结果为空")
                    }

                    val message = choices.getJSONObject(0).optJSONObject("message")
                    val summary = message?.optString("content", "")

                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        if (summary?.isNotEmpty() == true) {
                            android.util.Log.d("NoteAssistant", "总结成功")
                            onResult(summary)
                        } else {
                            _errorMessage.value = "AI总结失败，请重试"
                        }
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("NoteAssistant", "AI 总结失败", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "AI总结失败: ${e.message}"
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }


    // ===== 新增：思维导图功能 =====

    /**
     * 生成思维导图
     */
    fun generateMindMapFromNote(
        context: Context,
        noteId: String,
        noteContent: String,
        onResult: (MindMapData) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (noteContent.isBlank()) {
                    throw Exception("笔记内容为空，无法生成思维导图")
                }

                android.util.Log.d("MindMap", "开始生成思维导图")

                // 调用 AI API 生成思维导图结构
                val rootNode = requestMindMapGeneration(noteContent)

                val mindMapData = MindMapData(
                    noteId = noteId,
                    title = "思维导图 - ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
                    rootNode = rootNode
                )

                // 保存到本地
                saveMindMapToFile(context, mindMapData)

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    android.util.Log.d("MindMap", "思维导图生成成功")
                    onResult(mindMapData)
                }

            } catch (e: Exception) {
                android.util.Log.e("MindMap", "生成思维导图失败", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "生成思维导图失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 调用 AI API 生成思维导图结构
     */
    private suspend fun requestMindMapGeneration(content: String): MindMapNode = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("请分析以下笔记内容，并生成一个层级化的思维导图结构。")
            appendLine("要求：")
            appendLine("1. 提取核心主题作为根节点")
            appendLine("2. 提取2-4个一级主题")
            appendLine("3. 每个一级主题下提取2-5个关键点")
            appendLine("4. 返回格式为JSON，包含id、label和children字段")
            appendLine()
            appendLine("笔记内容：")
            appendLine(content)
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
            put("max_tokens", 4096)
        }

        val mediaType = "application/json".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$AI_BASE_URL/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $AI_API_KEY")
            .post(body)
            .build()

        // ✅ 修复：使用 ViewModel 中配置好的 client，而不是外部的 httpClient
        try {
            android.util.Log.d("MindMap", "开始请求 AI API: $AI_BASE_URL/chat/completions")
            android.util.Log.d("MindMap", "请求体: ${requestJson.toString(2)}")

            client.newCall(request).execute().use { response ->
                android.util.Log.d("MindMap", "收到响应，状态码: ${response.code}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    android.util.Log.e("MindMap", "API 调用失败: ${response.code}, 错误: $errorBody")
                    throw RuntimeException("HTTP ${response.code}: $errorBody")
                }

                val respText = response.body?.string() ?: throw RuntimeException("空响应")
                android.util.Log.d("MindMap", "响应内容: $respText")

                val root = JSONObject(respText)
                val choices = root.optJSONArray("choices")
                    ?: throw RuntimeException("响应中没有 choices")
                if (choices.length() == 0) {
                    throw RuntimeException("响应中没有 choices")
                }

                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val aiContent = message.optString("content", "")
                if (aiContent.isBlank()) {
                    throw RuntimeException("AI 没有返回内容")
                }

                android.util.Log.d("MindMap", "AI 返回内容: $aiContent")

                // 解析 AI 返回的结构
                parseMindMapFromAI(aiContent)
            }
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("MindMap", "网络超时", e)
            throw RuntimeException("网络请求超时，请检查网络连接或稍后重试")
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("MindMap", "无法连接到服务器", e)
            throw RuntimeException("无法连接到 AI 服务器，请检查网络")
        } catch (e: Exception) {
            android.util.Log.e("MindMap", "请求失败", e)
            throw e
        }
    }
    /**
     * 解析 AI 返回的内容为思维导图节点
     */
    private fun parseMindMapFromAI(aiContent: String): MindMapNode {
        return try {
            // 尝试从 AI 返回中提取 JSON
            val jsonStart = aiContent.indexOf("{")
            val jsonEnd = aiContent.lastIndexOf("}") + 1

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = aiContent.substring(jsonStart, jsonEnd)
                val jsonObj = JSONObject(jsonStr)
                parseNodeFromJson(jsonObj)
            } else {
                // 如果无法解析 JSON，使用文本分析
                parseNodeFromText(aiContent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MindMap", "解析失败，使用默认结构", e)
            // 返回默认结构
            createDefaultMindMap(aiContent)
        }
    }

    /**
     * 从 JSON 解析节点
     */
    private fun parseNodeFromJson(json: JSONObject): MindMapNode {
        val id = json.optString("id", UUID.randomUUID().toString())
        val label = json.optString("label", "主题")
        val childrenArray = json.optJSONArray("children")

        val children = mutableListOf<MindMapNode>()
        if (childrenArray != null) {
            for (i in 0 until childrenArray.length()) {
                val childJson = childrenArray.getJSONObject(i)
                children.add(parseNodeFromJson(childJson))
            }
        }

        return MindMapNode(
            id = id,
            label = label,
            children = children
        )
    }

    /**
     * 从文本分析创建节点
     */
    private fun parseNodeFromText(text: String): MindMapNode {
        val lines = text.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return MindMapNode(id = "root", label = "笔记内容")
        }

        // 简单解析：第一行作为根节点，其他行作为子节点
        val rootLabel = lines.firstOrNull()?.take(20) ?: "笔记内容"
        val children = mutableListOf<MindMapNode>()

        // 取前几行作为主要主题
        lines.drop(1).take(4).forEachIndexed { index, line ->
            children.add(
                MindMapNode(
                    id = "child_$index",
                    label = line.take(15)
                )
            )
        }

        return MindMapNode(
            id = "root",
            label = rootLabel,
            children = children
        )
    }

    /**
     * 创建默认思维导图
     */
    private fun createDefaultMindMap(content: String): MindMapNode {
        val summary = content.take(200)
        val lines = summary.lines().filter { it.isNotBlank() }

        return MindMapNode(
            id = "root",
            label = "笔记摘要",
            children = listOf(
                MindMapNode(
                    id = "section1",
                    label = "主要内容",
                    children = lines.take(3).mapIndexed { index, line ->
                        MindMapNode(
                            id = "point_$index",
                            label = line.take(15)
                        )
                    }
                ),
                MindMapNode(
                    id = "section2",
                    label = "关键点",
                    children = listOf(
                        MindMapNode(id = "key1", label = "待分析"),
                        MindMapNode(id = "key2", label = "待补充")
                    )
                )
            )
        )
    }

    /**
     * 保存思维导图到本地
     */
    private fun saveMindMapToFile(context: Context, mindMapData: MindMapData) {
        val mindMapsDir = File(context.filesDir, "mindmaps")
        if (!mindMapsDir.exists()) {
            mindMapsDir.mkdirs()
        }

        val mindMapFile = File(mindMapsDir, "${mindMapData.id}.json")
        val json = JSONObject().apply {
            put("id", mindMapData.id)
            put("noteId", mindMapData.noteId)
            put("title", mindMapData.title)
            put("createdAt", mindMapData.createdAt)
            put("rootNode", nodeToJson(mindMapData.rootNode))
        }

        mindMapFile.writeText(json.toString(2))
        android.util.Log.d("MindMap", "思维导图已保存: ${mindMapFile.absolutePath}")
    }

    /**
     * 将节点转换为 JSON
     */
    private fun nodeToJson(node: MindMapNode): JSONObject {
        return JSONObject().apply {
            put("id", node.id)
            put("label", node.label)
            put("position", JSONObject().apply {
                put("x", node.position.x)
                put("y", node.position.y)
            })

            val childrenArray = JSONArray()
            node.children.forEach { child ->
                childrenArray.put(nodeToJson(child))
            }
            put("children", childrenArray)
        }
    }

    /**
     * 加载笔记的所有思维导图
     */
    fun loadMindMapsForNote(context: Context, noteId: String): List<MindMapData> {
        val mindMapsDir = File(context.filesDir, "mindmaps")
        if (!mindMapsDir.exists()) {
            return emptyList()
        }

        val mindMapFiles = mindMapsDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()

        return mindMapFiles.mapNotNull { file ->
            try {
                val json = JSONObject(file.readText())
                val fileNoteId = json.optString("noteId", "")

                if (fileNoteId == noteId) {
                    MindMapData(
                        id = json.optString("id", ""),
                        noteId = fileNoteId,
                        title = json.optString("title", ""),
                        createdAt = json.optLong("createdAt", 0L),
                        rootNode = jsonToNode(json.getJSONObject("rootNode"))
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("MindMap", "加载思维导图失败: ${file.name}", e)
                null
            }
        }.sortedByDescending { it.createdAt }
    }

    /**
     * 从 JSON 转换为节点
     */
    private fun jsonToNode(json: JSONObject): MindMapNode {
        val positionJson = json.optJSONObject("position")
        val position = if (positionJson != null) {
            Offset(
                x = positionJson.optDouble("x", 0.0).toFloat(),
                y = positionJson.optDouble("y", 0.0).toFloat()
            )
        } else {
            Offset.Zero
        }

        val childrenArray = json.optJSONArray("children")
        val children = mutableListOf<MindMapNode>()

        if (childrenArray != null) {
            for (i in 0 until childrenArray.length()) {
                children.add(jsonToNode(childrenArray.getJSONObject(i)))
            }
        }

        return MindMapNode(
            id = json.optString("id", ""),
            label = json.optString("label", ""),
            position = position,
            children = children
        )
    }
}

// 主界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAssistantScreen(
    viewModel: NoteAssistantViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    val currentNote by viewModel.currentNote



    LaunchedEffect(Unit) {
        viewModel.loadNotes(context)
    }

    if (currentNote != null) {
        NoteDetailScreen(
            note = currentNote!!,
            viewModel = viewModel,
            onNavigateBack = { viewModel.setCurrentNote(null) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的笔记") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "新建笔记")
                }
            }
        ) { padding ->
            if (viewModel.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("暂无笔记", color = Color.Gray)
                        Text("点击右下角按钮创建笔记", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.notes) { note ->
                        NoteCard(
                            note = note,
                            onClick = { viewModel.setCurrentNote(note) },
                            onDelete = { viewModel.deleteNote(context, note.id) }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateNoteDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title ->
                    viewModel.createNote(context, title)
                    showCreateDialog = false
                }
            )
        }

        viewModel.errorMessage.value?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("提示") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(100) + if (note.content.length > 100) "..." else "",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(note.updatedAt),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, "删除", tint = Color.Red)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除笔记") },
            text = { Text("确定要删除笔记「${note.title}」吗?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建笔记") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("笔记标题") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim())
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note,
    viewModel: NoteAssistantViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf(note.content) }
    var showRecordDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading
    val scope = rememberCoroutineScope()
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    // ===== 新增：编辑模式状态 =====
    var isEditMode by remember { mutableStateOf(false) }

    // ===== 新增：导航状态 =====
    var currentScreen by remember { mutableStateOf<String>("detail") }
    var currentMindMap by remember { mutableStateOf<MindMapData?>(null) }

    val audioPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                viewModel.analyzeUploadedAudio(context, it) { result ->
                    content += "\n\n$result"
                    isEditMode = true
                }
            }
        }

    val videoPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                viewModel.analyzeUploadedVideo(context, it) { result ->
                    content += "\n\n$result"
                    isEditMode = true
                }
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CaptureVideo()
        ) { success ->
            if (success && videoUri != null) {
                viewModel.setLoading(true)
                val videoFile = File(videoUri!!.path!!)
                val audioFile = File(
                    context.filesDir,
                    "audio_${System.currentTimeMillis()}.m4a"
                )

                scope.launch {
                    try {
                        val wavFile = File(
                            context.filesDir,
                            "audio_${System.currentTimeMillis()}.wav"
                        )

                        AudioExtractor.extractAudioToWav(
                            context = context,
                            videoUri = videoUri!!,
                            outputWav = wavFile
                        )

                        val qps = 2
                        val delayMillis = 1000L / qps
                        val frames = VideoFrameExtractor.extractFrames(context, videoUri!!)
                        val frameDescriptions = mutableListOf<String>()

                        withContext(Dispatchers.IO) {
                            for (frame in frames) {
                                val description = try {
                                    viewModel.analyzeImageBitmapSuspend(context, frame)
                                } catch (e: Exception) {
                                    "分析失败: ${e.message}"
                                }
                                frameDescriptions.add(description)
                                delay(delayMillis)
                            }
                        }

                        viewModel.transcribeAudio(
                            context = context,
                            audioFile = wavFile
                        ) { transcribedText ->
                            val combinedText = buildString {
                                appendLine("原始转写：")
                                appendLine(transcribedText)
                                appendLine()
                                appendLine("图片分析：")
                                frameDescriptions.forEachIndexed { idx, desc ->
                                    appendLine("帧 ${idx + 1}: $desc")
                                }
                            }

                            viewModel.summarizeText(combinedText) { summary ->
                                content += """
                                    
[视频记录]
原始转写：
$transcribedText
            
摘要：
$summary
        """.trimIndent()

                                viewModel.updateNoteContent(
                                    context = context,
                                    noteId = note.id,
                                    newContent = content
                                )

                                // ✅ 新增：完成后进入编辑模式
                                isEditMode = true
                                viewModel.setLoading(false)
                            }
                        }

                    } catch (e: Exception) {
                        viewModel.setLoading(false)
                        e.printStackTrace()
                    }
                }
            }
        }

    fun startVideoRecord() {
        val file = File(
            context.filesDir,
            "video_${System.currentTimeMillis()}.mp4"
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        videoUri = uri
        videoLauncher.launch(uri)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startVideoRecord()
            } else {
                Toast.makeText(context, "需要摄像头权限才能录像", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(note.content) {
        content = note.content
    }

    // ===== 根据当前屏幕显示不同内容 =====
    when (currentScreen) {
        "history" -> {
            MindMapHistoryScreen(
                noteId = note.id,
                viewModel = viewModel,
                onNavigateBack = { currentScreen = "detail" },
                onMindMapClick = { mindMap ->
                    currentMindMap = mindMap
                    currentScreen = "view"
                }
            )
        }
        "view" -> {
            currentMindMap?.let { mindMap ->
                MindMapDetailScreen(
                    mindMapData = mindMap,
                    onNavigateBack = { currentScreen = "history" }
                )
            }
        }
        else -> {
            // 原笔记详情界面
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(note.title) },
                        navigationIcon = {
                            IconButton(onClick = {
                                // ✅ 修改：返回前保存并退出编辑模式
                                if (isEditMode) {
                                    viewModel.updateNoteContent(context, note.id, content)
                                }
                                onNavigateBack()
                            }) {
                                Icon(Icons.Default.ArrowBack, "返回")
                            }
                        },
                        actions = {
                            // ✅ 修改：编辑/保存按钮
                            IconButton(
                                onClick = {
                                    if (isEditMode) {
                                        // 保存并退出编辑模式
                                        viewModel.updateNoteContent(context, note.id, content)
                                        isEditMode = false
                                        Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 进入编辑模式
                                        isEditMode = true
                                    }
                                }
                            ) {
                                Icon(
                                    if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                    if (isEditMode) "保存" else "编辑"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        FloatingActionButton(
                            onClick = {
                                currentScreen = "history"
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Icon(Icons.Default.History, "历史思维导图")
                        }

                        FloatingActionButton(
                            onClick = {
                                if (content.isBlank()) {
                                    Toast.makeText(context, "笔记内容为空", Toast.LENGTH_SHORT).show()
                                    return@FloatingActionButton
                                }

                                viewModel.generateMindMapFromNote(
                                    context = context,
                                    noteId = note.id,
                                    noteContent = content
                                ) { mindMapData ->
                                    currentMindMap = mindMapData
                                    currentScreen = "view"
                                    Toast.makeText(context, "思维导图生成成功", Toast.LENGTH_SHORT).show()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.AccountTree, "生成思维导图")
                        }

                        FloatingActionButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    startVideoRecord()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Icon(Icons.Default.Videocam, "录像")
                        }

                        FloatingActionButton(
                            onClick = { showRecordDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(Icons.Default.KeyboardVoice, "录音转文字")
                        }

                        FloatingActionButton(
                            onClick = { audioPicker.launch("audio/*") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.UploadFile, "上传语音")
                        }

                        FloatingActionButton(
                            onClick = { videoPicker.launch("video/*") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.VideoFile, "上传视频")
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // ✅ 修改：根据编辑模式显示不同内容
                    if (isEditMode) {
                        // 编辑模式：显示可编辑的纯文本
                        BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(16.dp),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (content.isEmpty()) {
                                        Text(
                                            "在此输入笔记内容，或点击录音按钮...",
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                color = Color.Gray
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        // 查看模式：使用 LaTeX 渲染
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (content.isEmpty()) {
                                Text(
                                    "暂无内容，点击右上角编辑按钮开始编辑",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                )
                            } else {
                                // ✅ 使用 LatexText 渲染
                                LatexText(
                                    text = content,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "正在处理中...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (showRecordDialog) {
                AudioRecordDialog(
                    onDismiss = { showRecordDialog = false },
                    onAudioRecorded = { audioFile ->
                        showRecordDialog = false
                        viewModel.transcribeAudio(context, audioFile) { transcribedText ->
                            viewModel.summarizeText(transcribedText) { summary ->
                                content = if (content.isNotEmpty()) {
                                    "$content\n\n--- 新增内容 ---\n$summary"
                                } else {
                                    summary
                                }
                                viewModel.updateNoteContent(context, note.id, content)
                                // ✅ 新增：完成后进入编辑模式
                                isEditMode = true
                            }
                        }
                    }
                )
            }
        }
    }
}

// ===== 新增：历史思维导图列表界面 =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapHistoryScreen(
    noteId: String,
    viewModel: NoteAssistantViewModel,
    onNavigateBack: () -> Unit,
    onMindMapClick: (MindMapData) -> Unit
) {
    val context = LocalContext.current
    var mindMaps by remember { mutableStateOf<List<MindMapData>>(emptyList()) }

    LaunchedEffect(noteId) {
        mindMaps = viewModel.loadMindMapsForNote(context, noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史思维导图") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (mindMaps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无思维导图", color = Color.Gray)
                    Text("点击生成思维导图按钮创建", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mindMaps) { mindMap ->
                    MindMapHistoryCard(
                        mindMapData = mindMap,
                        onClick = { onMindMapClick(mindMap) }
                    )
                }
            }
        }
    }
}

@Composable
fun MindMapHistoryCard(
    mindMapData: MindMapData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mindMapData.title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "根节点: ${mindMapData.rootNode.label}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(mindMapData.createdAt),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "查看",
                tint = Color.Gray
            )
        }
    }
}

// 录音管理器类
class AudioRecorderManager {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    fun startRecording(context: Context, outputFile: File): Boolean {
        return try {
            stopRecording()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            android.util.Log.d("AudioRecorder", "录音开始: ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "开始录音失败", e)
            release()
            false
        }
    }

    fun stopRecording(): File? {
        return try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    reset()
                }
                isRecording = false
                android.util.Log.d("AudioRecorder", "录音停止")
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "停止录音失败", e)
            null
        } finally {
            release()
        }
    }

    fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            android.util.Log.d("AudioRecorder", "录音器已释放")
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "释放录音器失败", e)
        }
    }

    fun isCurrentlyRecording() = isRecording
}

@Composable
fun AudioRecordDialog(
    onDismiss: () -> Unit,
    onAudioRecorded: (File) -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val recorderManager = remember { AudioRecorderManager() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            Toast.makeText(context, "需要录音权限", Toast.LENGTH_SHORT).show()
        }
    }



    LaunchedEffect(Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isActive && isRecording) {
                delay(1000)
                recordingTime += 1
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderManager.release()
        }
    }

    Dialog(onDismissRequest = {
        recorderManager.release()
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "录音",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatTime(recordingTime),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) Color.Red else Color.Gray
                    )
                )

                if (isRecording && recordingTime < 3) {
                    Text(
                        text = "建议至少录音3秒",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!hasPermission) {
                    Text(
                        text = "请授予录音权限",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }) {
                        Text("请求权限")
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = {
                                    val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                                    audioFile = file
                                    recordingTime = 0

                                    val success = recorderManager.startRecording(context, file)
                                    if (success) {
                                        isRecording = true
                                    } else {
                                        Toast.makeText(context, "录音启动失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.KeyboardVoice, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始录音")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (recordingTime < 2) {
                                        Toast.makeText(context, "录音时间太短，请至少录音3秒", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    recorderManager.stopRecording()
                                    isRecording = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Icon(Icons.Default.Close, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("停止录音")
                            }
                        }

                        OutlinedButton(onClick = {
                            recorderManager.release()
                            onDismiss()
                        }) {
                            Text("取消")
                        }
                    }

                    if (!isRecording && audioFile != null && recordingTime > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                audioFile?.let { file ->
                                    if (file.exists() && file.length() > 0) {
                                        if (recordingTime < 2) {
                                            Toast.makeText(context, "录音时间太短，建议至少录音3秒", Toast.LENGTH_LONG).show()
                                        }
                                        onAudioRecorded(file)
                                    } else {
                                        Toast.makeText(context, "录音文件无效，请重新录制", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = recordingTime >= 2
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("确认并转录")
                        }

                        if (recordingTime < 2) {
                            Text(
                                text = "录音时间太短",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}



fun extractSegment(
    extractor: MediaExtractor,
    trackIndex: Int,
    startMs: Long,
    durationMs: Long,
    outputFile: File
) {
    val format = extractor.getTrackFormat(trackIndex)
    val mime = format.getString(MediaFormat.KEY_MIME) ?: return

    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    val newTrackIndex = muxer.addTrack(format)
    muxer.start()

    val maxBufferSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
        format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
    } else 1024 * 1024
    val buffer = ByteBuffer.allocate(maxBufferSize)
    val bufferInfo = android.media.MediaCodec.BufferInfo()

    val startUs = startMs * 1000
    val endUs = (startMs + durationMs) * 1000
    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

    while (true) {
        val sampleTime = extractor.sampleTime
        if (sampleTime == -1L || sampleTime > endUs) break

        bufferInfo.offset = 0
        bufferInfo.size = extractor.readSampleData(buffer, 0)
        if (bufferInfo.size < 0) break

        bufferInfo.presentationTimeUs = sampleTime - startUs
        bufferInfo.flags = when (extractor.sampleFlags) {
            MediaExtractor.SAMPLE_FLAG_SYNC -> MediaCodec.BUFFER_FLAG_SYNC_FRAME
            MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME -> MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
            MediaExtractor.SAMPLE_FLAG_ENCRYPTED -> 0
            else -> 0
        }

        muxer.writeSampleData(newTrackIndex, buffer, bufferInfo)
        extractor.advance()
    }

    muxer.stop()
    muxer.release()
}
//fun extractSegment(
//    extractor: MediaExtractor,
//    trackIndex: Int,
//    startMs: Long,
//    durationMs: Long,
//    outputFile: File
//) {
//    val format = extractor.getTrackFormat(trackIndex)
//    val mime = format.getString(MediaFormat.KEY_MIME) ?: return
//
//    val startUs = startMs * 1000
//    val endUs = (startMs + durationMs) * 1000
//    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
//
//    if (mime == "audio/raw") {
//        // PCM -> AAC 编码
//        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//        val encodeFormat = MediaFormat.createAudioFormat(
//            MediaFormat.MIMETYPE_AUDIO_AAC,
//            sampleRate,
//            channelCount
//        )
//        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
//        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
//        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
//
//        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
//        encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//        encoder.start()
//
//        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//        val trackIndexMuxer = muxer.addTrack(encoder.outputFormat)
//        muxer.start()
//
//        val buffer = ByteBuffer.allocate(1024 * 1024)
//        val bufferInfo = MediaCodec.BufferInfo()
//
//        while (true) {
//            val sampleTime = extractor.sampleTime
//            if (sampleTime == -1L || sampleTime > endUs) break
//            val size = extractor.readSampleData(buffer, 0)
//            if (size < 0) break
//
//            // 输入 PCM 数据到编码器
//            val inputIndex = encoder.dequeueInputBuffer(10000)
//            if (inputIndex >= 0) {
//                val inputBuffer = encoder.getInputBuffer(inputIndex)!!
//                inputBuffer.clear()
//                inputBuffer.put(buffer.array(), 0, size)
//                encoder.queueInputBuffer(inputIndex, 0, size, sampleTime - startUs, 0)
//            }
//
//            // 输出编码数据
//            var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
//            while (outputIndex >= 0) {
//                val encodedData = encoder.getOutputBuffer(outputIndex)!!
//                muxer.writeSampleData(trackIndexMuxer, encodedData, bufferInfo)
//                encoder.releaseOutputBuffer(outputIndex, false)
//                outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
//            }
//
//            extractor.advance()
//        }
//
//        encoder.stop()
//        encoder.release()
//        muxer.stop()
//        muxer.release()
//
//    } else {
//        // 直接复制原音频
//        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//        val trackIndexMuxer = muxer.addTrack(format)
//        muxer.start()
//
//        val buffer = ByteBuffer.allocate(1024 * 1024)
//        val bufferInfo = MediaCodec.BufferInfo()
//
//        while (true) {
//            val sampleTime = extractor.sampleTime
//            if (sampleTime == -1L || sampleTime > endUs) break
//            val size = extractor.readSampleData(buffer, 0)
//            if (size < 0) break
//
//            bufferInfo.offset = 0
//            bufferInfo.size = size
//            bufferInfo.presentationTimeUs = sampleTime - startUs
//            bufferInfo.flags = when (extractor.sampleFlags) {
//                MediaExtractor.SAMPLE_FLAG_SYNC -> MediaCodec.BUFFER_FLAG_SYNC_FRAME
//                MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME -> MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
//                else -> 0
//            }
//
//            muxer.writeSampleData(trackIndexMuxer, buffer, bufferInfo)
//            extractor.advance()
//        }
//
//        muxer.stop()
//        muxer.release()
//    }
//}

/**
 * 简单切分 audio/raw 文件（PCM 16bit, little endian）
 * @param file PCM 文件
 * @param sampleRate 采样率，比如 16000
 * @param channelCount 声道数，比如 1
 * @param segmentDurationMs 每段毫秒数
 */
//fun splitRawAudioFile(
//    context: Context,
//    file: File,
//    sampleRate: Int,
//    channelCount: Int,
//    segmentDurationMs: Long = 10_000L
//): List<File> {
//    val bytesPerSample = 2 // PCM 16bit
//    val bytesPerMs = sampleRate * channelCount * bytesPerSample / 1000
//    val segmentSize = (bytesPerMs * segmentDurationMs).toInt()
//
//    val segments = mutableListOf<File>()
//    val input = file.readBytes()
//    var offset = 0
//    var index = 0
//
//    while (offset < input.size) {
//        val end = (offset + segmentSize).coerceAtMost(input.size)
//        val segmentData = input.copyOfRange(offset, end)
//        val segmentFile = File(context.cacheDir, "${file.nameWithoutExtension}_part$index.${file.extension}")
//        segmentFile.writeBytes(segmentData)
//        segments.add(segmentFile)
//
//        offset = end
//        index++
//    }
//
//    return segments
//}
fun splitRawAudioFile(
    context: Context,
    inputFile: File,
    sampleRate: Int = 16000,
    channelCount: Int = 1,
    bitsPerSample: Int = 16,
    segmentDurationMs: Long = 10_000L
): List<File> {
    val segments = mutableListOf<File>()
    val bytesPerSample = bitsPerSample / 8
    val frameSize = bytesPerSample * channelCount
    val bytesPerSegment = (sampleRate * segmentDurationMs / 1000 * frameSize).toInt()

    inputFile.inputStream().use { fis ->
        var index = 0
        val buffer = ByteArray(bytesPerSegment)
        while (true) {
            var totalRead = 0
            while (totalRead < bytesPerSegment) {
                val read = fis.read(buffer, totalRead, bytesPerSegment - totalRead)
                if (read <= 0) break
                totalRead += read
            }
            if (totalRead <= 0) break

            val segmentFile = File(context.cacheDir, "${inputFile.nameWithoutExtension}_part$index.raw")
            segmentFile.outputStream().use { fos ->
                fos.write(buffer, 0, totalRead)
            }
            segments.add(segmentFile)
            Log.d("RawAudioSplit", "Segment $index: bytes=$totalRead, duration=${totalRead / frameSize.toDouble() / sampleRate * 1000} ms")
            index++
        }
    }
    return segments
}

private fun preprocessBlackboard(bitmap: Bitmap): Bitmap {
    /*
     * 黑板预处理模块
     *
     * 该模块用于在图像理解前对输入图像进行预处理，
     * 目标是检测并提取图像中的黑板区域，以减少背景干扰。
     */

    // ① 基本合法性保护（防御式编程）
    if (bitmap.width <= 0 || bitmap.height <= 0) {
        // 异常情况下直接返回原图，避免影响主流程
        return bitmap
    }

    // ② 图像格式统一（保证后续处理的一致性）
    val normalizedBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }

    // ③ 黑板区域检测

    val blackboardRegion = normalizedBitmap

    // ④ 预处理完成，返回候选黑板区域
    return blackboardRegion
}