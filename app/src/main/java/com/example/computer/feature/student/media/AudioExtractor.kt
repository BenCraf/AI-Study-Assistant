package com.example.computer.feature.student.media


import android.content.Context
import android.media.*
import android.net.Uri
import java.io.*

object AudioExtractor {

    fun extractAudioToWav(
        context: Context,
        videoUri: Uri,
        outputWav: File
    ): File {

        val extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)

        // 1️⃣ 找到音频轨道
        var audioTrackIndex = -1
        var format: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                format = f
                break
            }
        }

        if (audioTrackIndex == -1 || format == null) {
            extractor.release()
            throw IllegalStateException("No audio track found")
        }

        extractor.selectTrack(audioTrackIndex)

        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val pcmOutput = ByteArrayOutputStream()

        val bufferInfo = MediaCodec.BufferInfo()

        var sawEOS = false

        while (!sawEOS) {

            // 输入
            val inputIndex = decoder.dequeueInputBuffer(10_000)
            if (inputIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)

                if (sampleSize < 0) {
                    decoder.queueInputBuffer(
                        inputIndex,
                        0,
                        0,
                        0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    sawEOS = true
                } else {
                    decoder.queueInputBuffer(
                        inputIndex,
                        0,
                        sampleSize,
                        extractor.sampleTime,
                        0
                    )
                    extractor.advance()
                }
            }

            // 输出
            val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (outputIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputIndex)!!
                val chunk = ByteArray(bufferInfo.size)
                outputBuffer.get(chunk)
                outputBuffer.clear()
                pcmOutput.write(chunk)
                decoder.releaseOutputBuffer(outputIndex, false)
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()

        // 2️⃣ 写 WAV 文件
        writeWavFile(
            outputWav,
            pcmOutput.toByteArray(),
            sampleRate,
            channelCount
        )

        return outputWav
    }

    private fun writeWavFile(
        file: File,
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int
    ) {
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)

        // RIFF
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        val totalDataLen = pcmData.size + 36
        writeInt(header, 4, totalDataLen)

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(header, 16, 16)
        writeShort(header, 20, 1)
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * 2).toShort())
        writeShort(header, 34, 16)

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeInt(header, 40, pcmData.size)

        FileOutputStream(file).use {
            it.write(header)
            it.write(pcmData)
        }
    }

    private fun writeInt(b: ByteArray, offset: Int, value: Int) {
        b[offset] = value.toByte()
        b[offset + 1] = (value shr 8).toByte()
        b[offset + 2] = (value shr 16).toByte()
        b[offset + 3] = (value shr 24).toByte()
    }

    private fun writeShort(b: ByteArray, offset: Int, value: Short) {
        b[offset] = value.toByte()
        b[offset + 1] = (value.toInt() shr 8).toByte()
    }
}