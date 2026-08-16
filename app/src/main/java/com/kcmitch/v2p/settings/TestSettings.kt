package com.kcmitch.v2p.settings

import android.content.Context
import com.google.android.ump.ConsentDebugSettings

/**
 * =================================================================================================
 * TEST SETTINGS & UMP DEBUG CONFIGURATION (TestSettings.kt)
 * =================================================================================================
 *
 * Provides testing parameters for AdMob and Google User Messaging Platform (UMP) consent flows.
 * Allows simulating different world regions (European GDPR, US CCPA, Mexico, Canada, Australia, Asia, Africa).
 * Reference: https://developers.google.com/admob/android/privacy#testing
 * =================================================================================================
 */
object TestSettings {

    /**
     * Indicates whether the app is running in Test Mode.
     * When true, enables test ads, UMP debug geography, test devices, and the golden "Test Mode" badge.
     */
    var isTestMode: Boolean = false

    enum class TestRegion(
        val displayName: String,
        val isGdpr: Boolean,
        val locations: List<String>
    ) {
        EUROPE(
            displayName = "European Nation (GDPR)",
            isGdpr = true,
            locations = listOf(
                "France (Paris)",
                "Germany (Berlin)",
                "Italy (Rome)",
                "Spain (Madrid)",
                "Netherlands (Amsterdam)",
                "Sweden (Stockholm)",
                "Poland (Warsaw)",
                "Norway (Oslo)",
                "Ireland (Dublin)",
                "United Kingdom (London)",
                "Switzerland (Bern)",
                "Portugal (Lisbon)",
                "Austria (Vienna)",
                "Belgium (Brussels)",
                "Denmark (Copenhagen)"
            )
        ),
        US(
            displayName = "United States (CCPA/CPRA)",
            isGdpr = false,
            locations = listOf(
                "California (Los Angeles)",
                "Texas (Austin)",
                "New York (New York City)",
                "Florida (Miami)",
                "Virginia (Richmond)",
                "Colorado (Denver)",
                "Washington (Seattle)",
                "Illinois (Chicago)",
                "Connecticut (Hartford)",
                "Utah (Salt Lake City)"
            )
        ),
        MEXICO(
            displayName = "Mexico",
            isGdpr = false,
            locations = listOf(
                "Jalisco (Guadalajara)",
                "Mexico City (CDMX)",
                "Nuevo León (Monterrey)",
                "Quintana Roo (Cancún)",
                "Oaxaca",
                "Yucatán (Mérida)",
                "Puebla",
                "Veracruz"
            )
        ),
        CANADA(
            displayName = "Canada",
            isGdpr = false,
            locations = listOf(
                "Ontario (Toronto)",
                "British Columbia (Vancouver)",
                "Quebec (Montreal)",
                "Alberta (Calgary)",
                "Nova Scotia (Halifax)",
                "Manitoba (Winnipeg)"
            )
        ),
        AUSTRALIA(
            displayName = "Australia",
            isGdpr = false,
            locations = listOf(
                "New South Wales (Sydney)",
                "Victoria (Melbourne)",
                "Queensland (Brisbane)",
                "Western Australia (Perth)",
                "South Australia (Adelaide)",
                "Tasmania (Hobart)"
            )
        ),
        ASIA(
            displayName = "Asian Nation",
            isGdpr = false,
            locations = listOf(
                "Japan (Tokyo)",
                "South Korea (Seoul)",
                "Singapore",
                "India (Mumbai)",
                "Thailand (Bangkok)",
                "Vietnam (Hanoi)",
                "Philippines (Manila)",
                "Indonesia (Jakarta)",
                "Malaysia (Kuala Lumpur)"
            )
        ),
        AFRICA(
            displayName = "African Nation",
            isGdpr = false,
            locations = listOf(
                "Nigeria (Lagos)",
                "South Africa (Johannesburg)",
                "Egypt (Cairo)",
                "Kenya (Nairobi)",
                "Morocco (Casablanca)",
                "Ghana (Accra)",
                "Ethiopia (Addis Ababa)",
                "Senegal (Dakar)"
            )
        )
    }

    /**
     * Currently selected test region and simulated location.
     */
    var selectedRegion: TestRegion = TestRegion.EUROPE
        private set

    var currentLocationName: String = TestRegion.EUROPE.locations.first()
        private set

    /**
     * Debug geography to simulate different regulatory regions:
     * - [ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA]: Simulates user located in European Economic Area / UK
     * - [ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA]: Simulates user outside EEA (e.g. US, Canada, Mexico, etc.)
     * - [ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED]: Uses actual physical device geolocation
     */
    val debugGeography: Int
        get() = if (selectedRegion.isGdpr) {
            ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
        } else {
            ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
        }

    /**
     * Select a new region and randomly assign a simulated country/subregion.
     */
    fun selectRegion(region: TestRegion, randomizeLocation: Boolean = true): String {
        selectedRegion = region
        if (randomizeLocation || !region.locations.contains(currentLocationName)) {
            currentLocationName = region.locations.random()
        }
        return currentLocationName
    }

    /**
     * Re-randomize location within the currently selected region.
     */
    fun randomizeCurrentLocation(): String {
        currentLocationName = selectedRegion.locations.random()
        return currentLocationName
    }

    /**
     * Test device hashed IDs for AdMob & UMP testing.
     * Check logcat on app startup for: "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("...")"
     */
    val testDeviceHashedIds: MutableList<String> = mutableListOf(
        "8CD6FE8F9D292A2A29B08DC58DA51A7E", 
        "33BE2250B43518CCDA7DE426D04EE231",
        "B3EEABB8EE11C2BE770B684D95219ECB"
    )

    /**
     * Set to true if you want to reset the consent state on each app launch during testing.
     */
    var resetConsentOnStartup: Boolean = false

    /**
     * Computes the current device's own Test Device Hashed ID (MD5 hash of Settings.Secure.ANDROID_ID)
     * as required by Google AdMob and UMP ConsentDebugSettings.
     */
    fun getDeviceTestHashedId(context: Context): String {
        return try {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            if (androidId.isNullOrBlank()) return ""
            val md = java.security.MessageDigest.getInstance("MD5")
            val array = md.digest(androidId.toByteArray())
            val sb = java.lang.StringBuilder()
            for (b in array) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) sb.append('0')
                sb.append(hex)
            }
            sb.toString().uppercase()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Retrieves all registered test device hashed IDs, including this physical/virtual device's own hashed ID.
     */
    fun getAllTestDeviceHashedIds(context: Context): List<String> {
        val result = LinkedHashSet<String>()
        val deviceId = getDeviceTestHashedId(context)
        if (deviceId.isNotBlank()) {
            result.add(deviceId)
        }
        result.addAll(testDeviceHashedIds)
        return result.toList()
    }

    /**
     * Dynamically register a test device hashed ID at runtime.
     */
    fun addTestDeviceHashedId(hashedId: String) {
        val trimmed = hashedId.trim().uppercase()
        if (trimmed.isNotBlank() && !testDeviceHashedIds.contains(trimmed)) {
            testDeviceHashedIds.add(trimmed)
        }
    }
}
