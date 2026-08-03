package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ApiErrorSanitizer
import com.example.data.AppEnvironment
import com.example.data.BaseUrlPresetEntry
import com.example.data.BaseUrlResolver
import com.example.data.UrlClassification
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


sealed class UrlHintSeverity {
    data object OK : UrlHintSeverity()
    data object INFO : UrlHintSeverity()
    data object WARNING : UrlHintSeverity()
    data object ERROR : UrlHintSeverity()
}

data class UrlHintState(
    val showHint: Boolean = false,
    val severity: UrlHintSeverity = UrlHintSeverity.INFO,
    val title: String? = null,
    val body: String? = null,
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val actionEnv: AppEnvironment? = null
)

@Composable
fun BaseUrlConfigScreen(
    currentBaseUrl: String,
    activeEnvironment: AppEnvironment = AppEnvironment.DEV,
    onSaveBaseUrl: (String) -> Unit,
    onSelectEnvironment: (AppEnvironment) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    var inputUrl by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    var pingStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var urlHint by remember { mutableStateOf(UrlHintState()) }
    val scope = rememberCoroutineScope()

    val smartPresets = remember { BaseUrlResolver.buildPresetList() }

    LaunchedEffect(inputUrl) {
        val trimmed = inputUrl.trim()
        if (trimmed.isBlank()) {
            urlHint = UrlHintState()
            return@LaunchedEffect
        }

        val resolution = BaseUrlResolver.resolve(trimmed)
        urlHint = when (resolution.classification) {
            UrlClassification.MARKETING_WEBSITE -> UrlHintState(
                showHint = true,
                severity = UrlHintSeverity.ERROR,
                title = "⚠️ This is a marketing website — no JSON API here",
                body = resolution.suggestion,
                actionLabel = "Switch to ${resolution.suggestedEnvironment?.keyName ?: "PROD"} Preset",
                actionUrl = resolution.correctedUrl,
                actionEnv = resolution.suggestedEnvironment
            )
            UrlClassification.LOCALHOST_NEEDS_EMULATOR_FIX -> UrlHintState(
                showHint = true,
                severity = UrlHintSeverity.WARNING,
                title = "💡 localhost won't work on Android",
                body = resolution.suggestion,
                actionLabel = "Use 10.0.2.2 instead",
                actionUrl = resolution.correctedUrl,
                actionEnv = resolution.suggestedEnvironment
            )
            UrlClassification.HTML_WEBSITE_NOT_API -> UrlHintState(
                showHint = true,
                severity = UrlHintSeverity.ERROR,
                title = "⚠️ URL returned a web page, not JSON",
                body = resolution.suggestion,
                actionLabel = "Switch to JobTraq PROD API",
                actionUrl = resolution.correctedUrl,
                actionEnv = resolution.suggestedEnvironment
            )
            UrlClassification.LOCALHOST_VALID -> UrlHintState(
                showHint = true,
                severity = UrlHintSeverity.OK,
                title = "✅ Android emulator loopback configured",
                body = "10.0.2.2 correctly routes to your PC's localhost."
            )
            UrlClassification.VALID_API_SERVER -> {
                if (resolution.suggestedEnvironment == AppEnvironment.PROD) {
                    UrlHintState(
                        showHint = true,
                        severity = UrlHintSeverity.INFO,
                        title = "✅ Valid JobTraq Production API",
                        body = resolution.suggestion ?: "Valid API host detected. Tap Test Endpoint to verify reachability."
                    )
                } else {
                    UrlHintState(
                        showHint = true,
                        severity = UrlHintSeverity.OK,
                        title = "✅ Valid API server URL format",
                        body = resolution.suggestion ?: "Tap Test Endpoint to verify reachability."
                    )
                }
            }
            else -> UrlHintState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("base_url_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Auth",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "API Base URL Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "API Server",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Server Configuration 🌐",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Configure custom backend API endpoint before signing in. Stored securely in local preferences.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ENVIRONMENT PROFILE SELECTOR
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE ENVIRONMENT PROFILE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "ACTIVE: ${activeEnvironment.keyName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppEnvironment.values().forEach { env ->
                        val isSelected = activeEnvironment == env
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onSelectEnvironment(env)
                                    inputUrl = env.defaultBaseUrl
                                }
                                .testTag("env_config_card_${env.keyName.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = env.keyName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (env == AppEnvironment.TEST) "Dummy Data" else if (env == AppEnvironment.DEV) "Real API (Active)" else "Live Cluster",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "CUSTOM API BASE URL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = {
                            inputUrl = it
                            pingStatus = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("base_url_input_field"),
                        placeholder = { Text("e.g. https://www.jobtraq.in or http://10.0.2.2:9002") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "URL Link",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        supportingText = {
                            Text(
                                text = "Use JobTraq API URL or your local backend server.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (urlHint.severity is UrlHintSeverity.ERROR) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (urlHint.severity is UrlHintSeverity.ERROR) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = urlHint.showHint) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            val (hintBg, hintFg, hintIcon) = when (urlHint.severity) {
                                is UrlHintSeverity.OK -> Triple(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    MaterialTheme.colorScheme.primary,
                                    Icons.Default.CheckCircle
                                )
                                is UrlHintSeverity.INFO -> Triple(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                    MaterialTheme.colorScheme.secondary,
                                    Icons.Default.Dns
                                )
                                is UrlHintSeverity.WARNING -> Triple(
                                    androidx.compose.ui.graphics.Color(0xFFFFF4E5),
                                    androidx.compose.ui.graphics.Color(0xFFB25B00),
                                    Icons.Default.Warning
                                )
                                is UrlHintSeverity.ERROR -> Triple(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.error,
                                    Icons.Default.Error
                                )
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = hintBg),
                                border = if (urlHint.severity is UrlHintSeverity.ERROR || urlHint.severity is UrlHintSeverity.WARNING)
                                    BorderStroke(1.dp, hintFg.copy(alpha = 0.35f)) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = hintIcon,
                                            contentDescription = null,
                                            tint = hintFg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            urlHint.title?.let { title ->
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = hintFg
                                                )
                                            }
                                            urlHint.body?.let { body ->
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = body,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = hintFg.copy(alpha = 0.85f),
                                                    lineHeight = 14.sp
                                                )
                                            }
                                            urlHint.actionLabel?.let { label ->
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            urlHint.actionUrl?.let { url ->
                                                                inputUrl = url
                                                                pingStatus = null
                                                            }
                                                            urlHint.actionEnv?.let { env ->
                                                                onSelectEnvironment(env)
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = hintFg,
                                                            contentColor = Color.White
                                                        ),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Settings,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAutoDetecting = true
                                pingStatus = null
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        BaseUrlResolver.probeAndSuggest(inputUrl.trim())
                                    }
                                    isAutoDetecting = false
                                    if (result.wasCorrected) {
                                        inputUrl = result.correctedUrl
                                        result.suggestedEnvironment?.let { env ->
                                            onSelectEnvironment(env)
                                        }
                                        pingStatus = result.suggestion
                                    } else {
                                        val hint = result.suggestion
                                        val probe = BaseUrlResolver.probeUrl(inputUrl.trim())
                                        pingStatus = when {
                                            probe.isSuccess && !probe.isHtml ->
                                                "✅ Auto-detect OK — ${if (hint.isNullOrBlank()) "Valid API endpoint" else hint} (${probe.httpCode}, ${probe.durationMs}ms)"
                                            probe.isSuccess && probe.isHtml ->
                                                "⚠️ Auto-detect WARNING — URL returned HTML (web page not JSON). Pick JobTraq PROD preset or your local backend server."
                                            else ->
                                                (hint ?: "❌ Could not reach server — select a preset below or enter your API server URL.")
                                        }
                                    }
                                }
                            },
                            enabled = !isAutoDetecting && inputUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("auto_detect_button")
                        ) {
                            if (isAutoDetecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Detecting...", fontSize = 10.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = "Auto-Detect",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Detect", fontSize = 10.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                pingStatus = null
                                var formattedUrl = inputUrl.trim()
                                if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                                    formattedUrl = "http://$formattedUrl"
                                }
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            val client = OkHttpClient.Builder()
                                                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                                                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                                                .build()
                                            val request = Request.Builder()
                                                .url(formattedUrl)
                                                .build()
                                            client.newCall(request).execute().use { response ->
                                                val code = response.code
                                                val message = response.message
                                                val contentType = response.header("Content-Type").orEmpty()
                                                val isHtml = contentType.contains("text/html", ignoreCase = true)
                                                val bodyBytes = runCatching { response.body?.bytes() ?: ByteArray(0) }.getOrDefault(ByteArray(0))
                                                val rawBody = if (bodyBytes.isNotEmpty()) String(bodyBytes.copyOfRange(0, minOf(2048, bodyBytes.size))) else ""
                                                val looksLikeHtml = isHtml ||
                                                    rawBody.trimStart().uppercase().let { up ->
                                                        up.startsWith("<!DOCTYPE") || up.startsWith("<HTML")
                                                    }

                                                if (response.isSuccessful && !looksLikeHtml) {
                                                    "✅ Connection successful! HTTP $code $message. Server responded (non-HTML)."
                                                } else if (response.isSuccessful && looksLikeHtml) {
                                                    "⚠️ Server responded HTTP $code, but returned a WEB PAGE (HTML / ${contentType.ifBlank { "text/html" }}). $formattedUrl appears to be a frontend website, not a JSON API. The app will fail here. In Settings, pick JobTraq PROD API, or your local backend (http://10.0.2.2:PORT for emulator)."
                                                } else {
                                                    val fullBody = if (bodyBytes.isNotEmpty()) String(bodyBytes) else ""
                                                    val cleaned = ApiErrorSanitizer.sanitizeApiError(
                                                        rawError = fullBody,
                                                        responseCode = code,
                                                        baseUrl = formattedUrl,
                                                        fallbackContext = "ping server"
                                                    )
                                                    "❌ $cleaned"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            val cleaned = ApiErrorSanitizer.sanitizeExceptionError(e, formattedUrl)
                                            "❌ $cleaned"
                                        }
                                    }
                                    isTestingConnection = false
                                    pingStatus = result
                                }
                            },
                            enabled = !isTestingConnection && inputUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("ping_connection_button")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 10.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Test",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val finalUrl = if (urlHint.actionUrl != null && urlHint.severity is UrlHintSeverity.ERROR) {
                                    urlHint.actionUrl ?: inputUrl.trim()
                                } else inputUrl.trim()
                                onSaveBaseUrl(finalUrl)
                                val inferred = AppEnvironment.inferFromBaseUrl(finalUrl)
                                onSelectEnvironment(inferred)
                                onNavigateBack()
                            },
                            enabled = inputUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .testTag("save_base_url_button")
                                .weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    pingStatus?.let { status ->
                        val isSuccess = status.startsWith("Connection successful")
                        val containerColor = if (isSuccess) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        }
                        val tintColor = if (isSuccess) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        val icon = if (isSuccess) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Error
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = containerColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Status",
                                    tint = tintColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RECOMMENDED PRESET SHORTCUTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    smartPresets.take(3).forEach { preset ->
                        FilterChip(
                            selected = inputUrl == preset.url,
                            onClick = {
                                inputUrl = preset.url
                                pingStatus = null
                                onSelectEnvironment(preset.env)
                            },
                            label = {
                                Text(preset.tag, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            leadingIcon = if (preset.isRecommended) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_preset_${preset.tag.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Smart Presets Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FULL BASE URL PRESETS (TAP TO SELECT)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                smartPresets.forEach { preset ->
                    val isSelected = inputUrl == preset.url && !preset.isPlaceHolder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !preset.isPlaceHolder) {
                                if (!preset.isPlaceHolder) {
                                    inputUrl = preset.url
                                    pingStatus = null
                                    onSelectEnvironment(preset.env)
                                }
                            }
                            .testTag("preset_${preset.tag.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                preset.isPlaceHolder -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        else if (preset.isRecommended) androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (preset.isRecommended) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = preset.label,
                                        tint = if (preset.isRecommended) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (preset.isRecommended) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "RECOMMENDED",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (preset.isPlaceHolder) preset.subtitle else preset.url,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (preset.isPlaceHolder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            when {
                                isSelected -> Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                preset.isPlaceHolder -> Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "EDIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
