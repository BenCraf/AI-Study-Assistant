package com.example.computer.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)    // 连接超时 30 秒
        .readTimeout(120, TimeUnit.SECONDS)      // 读取超时 120 秒（AI 响应需要时间）
        .writeTimeout(30, TimeUnit.SECONDS)      // 写入超时 30 秒
        .build()
}