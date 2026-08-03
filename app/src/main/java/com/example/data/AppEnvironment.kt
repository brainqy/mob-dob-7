package com.example.data

enum class AppEnvironment(
    val keyName: String,
    val displayName: String,
    val badgeTag: String,
    val description: String,
    val defaultBaseUrl: String,
    val isDummyDataAllowed: Boolean
) {
    TEST(
        keyName = "TEST",
        displayName = "Test Environment",
        badgeTag = "TEST (Dummy Data)",
        description = "Uses offline mock and dummy test data for sandbox testing.",
        defaultBaseUrl = "http://localhost:9002",
        isDummyDataAllowed = true
    ),
    DEV(
        keyName = "DEV",
        displayName = "Dev Environment",
        badgeTag = "DEV (Real API - Active)",
        description = "Connected to real JobTraq backend API. Fetches real data with no dummy fallbacks.",
        defaultBaseUrl = "https://www.jobtraq.in",
        isDummyDataAllowed = false
    ),
    PROD(
        keyName = "PROD",
        displayName = "Production Environment",
        badgeTag = "PROD (Live)",
        description = "Connected to JobTraq production live API server.",
        defaultBaseUrl = "https://www.jobtraq.in",
        isDummyDataAllowed = false
    );

    companion object {
        fun fromKey(key: String?): AppEnvironment {
            return values().firstOrNull { it.keyName.equals(key, ignoreCase = true) } ?: DEV
        }

        fun inferFromBaseUrl(url: String?): AppEnvironment {
            val clean = (url ?: "").trim().lowercase()
            if (clean.isBlank()) return DEV

            val stripped = clean
                .removePrefix("http://")
                .removePrefix("https://")
                .substringBefore("/")

            val devHost = stripped.substringBefore(":")

            return when {
                stripped.endsWith("jobtraq.in") -> PROD
                stripped.contains("jobtraq.in") || clean.contains(PROD.defaultBaseUrl.lowercase()) -> PROD
                devHost == "localhost" || devHost == "127.0.0.1" || devHost == "10.0.2.2" ||
                    devHost.startsWith("192.168.") || devHost.startsWith("10.") -> TEST
                else -> DEV
            }
        }
    }
}
