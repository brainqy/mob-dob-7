package com.example.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ResolvedUrl(
    val originalUrl: String,
    val correctedUrl: String,
    val wasCorrected: Boolean,
    val classification: UrlClassification,
    val suggestion: String? = null,
    val suggestedEnvironment: AppEnvironment? = null
)

enum class UrlClassification {
    VALID_API_SERVER,
    MARKETING_WEBSITE,
    LOCALHOST_VALID,
    LOCALHOST_NEEDS_EMULATOR_FIX,
    UNREACHABLE_OR_INVALID,
    HTML_WEBSITE_NOT_API
}

data class ProbeResult(
    val url: String,
    val isSuccess: Boolean,
    val isHtml: Boolean,
    val httpCode: Int,
    val durationMs: Long
)

object BaseUrlResolver {

    private val marketingDomains = setOf(
        "jobtraq.com",
        "www.jobtraq.com",
        "jobtraq.io",
        "www.jobtraq.io"
    )

    private val acceptedProductionDomains = setOf(
        "jobtraq.in",
        "www.jobtraq.in"
    )

    private val domainRedirects: Map<String, AppEnvironment> = mapOf(
        "jobtraq.com" to AppEnvironment.PROD,
        "www.jobtraq.com" to AppEnvironment.PROD
    )

    private val domainEnvironmentHints: Map<String, AppEnvironment> = mapOf(
        "jobtraq.in" to AppEnvironment.PROD,
        "www.jobtraq.in" to AppEnvironment.PROD
    )

    private val localhostAliases = setOf(
        "localhost",
        "127.0.0.1",
        "0.0.0.0"
    )

    fun classifyUrl(rawUrl: String): UrlClassification {
        val url = rawUrl.trim().lowercase()
        val clean = url.removePrefix("http://").removePrefix("https://").substringBefore("/")

        if (clean in acceptedProductionDomains) {
            return UrlClassification.VALID_API_SERVER
        }

        if (clean in marketingDomains) {
            return UrlClassification.MARKETING_WEBSITE
        }

        val hostPart = clean.substringBefore(":")
        if (hostPart in localhostAliases) {
            return if (hostPart == "10.0.2.2") {
                UrlClassification.LOCALHOST_VALID
            } else {
                UrlClassification.LOCALHOST_NEEDS_EMULATOR_FIX
            }
        }

        if (clean == "10.0.2.2") {
            return UrlClassification.LOCALHOST_VALID
        }

        if (clean.endsWith(".run.app")) {
            return UrlClassification.VALID_API_SERVER
        }

        return UrlClassification.UNREACHABLE_OR_INVALID
    }

    fun isMarketingWebsite(rawUrl: String): Boolean {
        val clean = rawUrl.trim().lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
        return clean in marketingDomains
    }

    fun resolve(rawUrl: String): ResolvedUrl {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return ResolvedUrl(
                originalUrl = rawUrl,
                correctedUrl = AppEnvironment.DEV.defaultBaseUrl,
                wasCorrected = true,
                classification = UrlClassification.UNREACHABLE_OR_INVALID,
                suggestion = "URL was blank. Defaulted to DEV environment.",
                suggestedEnvironment = AppEnvironment.DEV
            )
        }

        val withScheme = if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)) {
            "https://$trimmed"
        } else trimmed

        val classification = classifyUrl(withScheme)
        val host = withScheme.lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
            .substringBefore(":")

        val matchedAcceptable = domainEnvironmentHints.entries.firstOrNull { (domain, _) ->
            host == domain || host.endsWith(".$domain")
        }

        if (matchedAcceptable != null || classification == UrlClassification.VALID_API_SERVER &&
            (host in acceptedProductionDomains || acceptedProductionDomains.any { host == it || host.endsWith(".$it") })) {
            val env = matchedAcceptable?.value ?: AppEnvironment.PROD
            return ResolvedUrl(
                originalUrl = rawUrl,
                correctedUrl = withScheme,
                wasCorrected = false,
                classification = UrlClassification.VALID_API_SERVER,
                suggestion = "✅ $host is a valid production API host. Using PROD environment.",
                suggestedEnvironment = env
            )
        }

        val matchedEnv = domainRedirects.entries.firstOrNull { (domain, _) ->
            host == domain || host.endsWith(".$domain")
        }

        if (matchedEnv != null || classification == UrlClassification.MARKETING_WEBSITE) {
            val targetEnv = matchedEnv?.value ?: AppEnvironment.PROD
            return ResolvedUrl(
                originalUrl = rawUrl,
                correctedUrl = targetEnv.defaultBaseUrl,
                wasCorrected = true,
                classification = UrlClassification.MARKETING_WEBSITE,
                suggestion = "⚠️ $host is a marketing website with no JSON API. Auto-switched to ${targetEnv.keyName} JobTraq API URL.",
                suggestedEnvironment = targetEnv
            )
        }

        if (classification == UrlClassification.LOCALHOST_NEEDS_EMULATOR_FIX) {
            val portMatch = Regex(""":(\d{2,5})""").find(withScheme)
            val port = portMatch?.groupValues?.get(1) ?: "9002"
            return ResolvedUrl(
                originalUrl = rawUrl,
                correctedUrl = "http://10.0.2.2:$port",
                wasCorrected = true,
                classification = UrlClassification.LOCALHOST_NEEDS_EMULATOR_FIX,
                suggestion = "💡 localhost/$host is unreachable on Android emulators. Use 10.0.2.2 (Android's host loopback) instead.",
                suggestedEnvironment = AppEnvironment.TEST
            )
        }

        return ResolvedUrl(
            originalUrl = rawUrl,
            correctedUrl = withScheme,
            wasCorrected = false,
            classification = classification
        )
    }

    suspend fun probeUrl(rawUrl: String, timeoutSec: Int = 5): ProbeResult {
        val url = rawUrl.trim()
        val withScheme = if (!url.startsWith("http://", ignoreCase = true) &&
            !url.startsWith("https://", ignoreCase = true)) {
            "http://$url"
        } else url

        val start = System.currentTimeMillis()
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                .readTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(withScheme)
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                val contentType = response.header("Content-Type").orEmpty()
                val bodyBytes = runCatching { response.body?.bytes() ?: ByteArray(0) }.getOrDefault(ByteArray(0))
                val rawBody = if (bodyBytes.isNotEmpty()) {
                    String(bodyBytes.copyOfRange(0, minOf(1024, bodyBytes.size)))
                } else ""
                val isHtml = contentType.contains("text/html", ignoreCase = true) ||
                    rawBody.trimStart().uppercase().let { up ->
                        up.startsWith("<!DOCTYPE") || up.startsWith("<HTML")
                    }
                ProbeResult(
                    url = withScheme,
                    isSuccess = response.isSuccessful,
                    isHtml = isHtml,
                    httpCode = response.code,
                    durationMs = System.currentTimeMillis() - start
                )
            }
        } catch (e: Exception) {
            ProbeResult(
                url = withScheme,
                isSuccess = false,
                isHtml = false,
                httpCode = 0,
                durationMs = System.currentTimeMillis() - start
            )
        }
    }

    suspend fun probeAndSuggest(rawUrl: String): ResolvedUrl {
        val baseResolution = resolve(rawUrl)
        if (baseResolution.wasCorrected) return baseResolution

        val probe = probeUrl(rawUrl)
        if (probe.isSuccess && !probe.isHtml) {
            return baseResolution.copy(classification = UrlClassification.VALID_API_SERVER)
        }

        if (probe.isSuccess && probe.isHtml) {
            val matchedEnv = domainRedirects.entries.firstOrNull { (domain, _) ->
                val host = rawUrl.lowercase()
                    .removePrefix("http://")
                    .removePrefix("https://")
                    .substringBefore("/")
                host == domain || host.endsWith(".$domain")
            }
            val targetEnv = matchedEnv?.value ?: AppEnvironment.PROD
            return ResolvedUrl(
                originalUrl = rawUrl,
                correctedUrl = targetEnv.defaultBaseUrl,
                wasCorrected = true,
                classification = UrlClassification.HTML_WEBSITE_NOT_API,
                suggestion = "⚠️ Got HTTP ${probe.httpCode} but response was HTML (web page, not JSON). Switched to ${targetEnv.keyName} JobTraq API URL.",
                suggestedEnvironment = targetEnv
            )
        }

        return baseResolution.copy(
            suggestion = if (probe.httpCode == 0) {
                "❌ Could not connect to $rawUrl (server unreachable, timed out, or DNS failed)."
            } else {
                "⚠️ Server responded HTTP ${probe.httpCode}. Verify this URL points to a JSON API server."
            }
        )
    }

    fun buildPresetList(): List<BaseUrlPresetEntry> = listOf(
        BaseUrlPresetEntry(
            label = "JobTraq Production API",
            subtitle = "www.jobtraq.in — official Next.js /api routes",
            url = "https://www.jobtraq.in",
            tag = "PROD",
            env = AppEnvironment.PROD,
            isRecommended = true
        ),
        BaseUrlPresetEntry(
            label = "Android Emulator Local",
            subtitle = "10.0.2.2:9002 (Android loopback → PC localhost:9002)",
            url = "http://10.0.2.2:9002",
            tag = "EMULATOR",
            env = AppEnvironment.TEST
        ),
        BaseUrlPresetEntry(
            label = "Physical Device LAN",
            subtitle = "Use your PC's LAN IP e.g. 192.168.1.50:9002",
            url = "http://192.168.1.50:9002",
            tag = "LAN",
            env = AppEnvironment.TEST,
            isPlaceHolder = true
        )
    )

    fun alternateJobtraqHostForDnsFallback(baseUrl: String): String? {
        val trimmed = baseUrl.trim()
        if (trimmed.isBlank()) return null
        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
        val scheme = withScheme.substring(0, withScheme.indexOf("://") + 3)
        val rest = withScheme.substring(scheme.length)
        val host = rest.substringBefore("/").substringBefore(":")
        val port = rest.substringBefore("/").let { h ->
            if (h.contains(":")) h.substringAfter(":") else null
        }
        val suffix = rest.substringAfter('/', "")
        val newHost = when (host.lowercase()) {
            "www.jobtraq.in" -> "jobtraq.in"
            "jobtraq.in" -> "www.jobtraq.in"
            else -> return null
        }
        val portPart = if (port != null) ":$port" else ""
        val suffixPart = if (suffix.isNotEmpty() || rest.contains("/")) "/$suffix" else ""
        return "$scheme$newHost$portPart$suffixPart".removeSuffix("/")
    }

    fun isDnsFailure(e: Exception): Boolean {
        return e is java.net.UnknownHostException ||
            (e.message?.contains("unable to resolve host", ignoreCase = true) == true) ||
            (e.message?.contains("no address associated with hostname", ignoreCase = true) == true) ||
            (e.cause?.let { isDnsFailure(it as Exception) } == true)
    }
}

data class BaseUrlPresetEntry(
    val label: String,
    val subtitle: String,
    val url: String,
    val tag: String,
    val env: AppEnvironment,
    val isRecommended: Boolean = false,
    val isPlaceHolder: Boolean = false
)
