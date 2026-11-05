package com.aichat.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed interface TestResult {
    data class Ok(val code: Int) : TestResult
    data class HttpError(val code: Int) : TestResult
    data class NetError(val message: String) : TestResult
    data object NoUrl : TestResult
    data object NoKey : TestResult
}

/**
 * 用一个 GET {baseUrl}/models 请求验证地址与密钥是否可用（兼容 OpenAI 接口格式）。
 */
object ConnectionTester {

    suspend fun test(baseUrl: String, apiKey: String): TestResult = withContext(Dispatchers.IO) {
        val base = baseUrl.trim().trimEnd('/')
        if (base.isEmpty()) return@withContext TestResult.NoUrl
        if (apiKey.isBlank()) return@withContext TestResult.NoKey

        var conn: HttpURLConnection? = null
        try {
            val url = URL("$base/models")
            // Anthropic 原生接口用 x-api-key，其余兼容 OpenAI 的服务用 Bearer
            val isAnthropic = base.contains("anthropic.com")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                if (isAnthropic) {
                    setRequestProperty("x-api-key", apiKey.trim())
                    setRequestProperty("anthropic-version", "2023-06-01")
                } else {
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            if (code in 200..299) TestResult.Ok(code) else TestResult.HttpError(code)
        } catch (e: Exception) {
            TestResult.NetError(e.message ?: e.javaClass.simpleName)
        } finally {
            conn?.disconnect()
        }
    }
}
