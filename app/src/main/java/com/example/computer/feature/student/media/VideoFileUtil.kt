package com.example.computer.feature.student.media

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object VideoFileUtil {

    fun copyUriToFile(
        context: Context,
        uri: Uri
    ): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开视频 Uri")

        val outFile = File(
            context.filesDir,
            "video_${System.currentTimeMillis()}.mp4"
        )

        FileOutputStream(outFile).use { output ->
            inputStream.copyTo(output)
        }

        return outFile
    }
}