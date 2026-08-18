package com.kcmitch.v2p.config

/**
 * Master Application Configuration for v2p (Video to Photos).
 */
object AppConfig {
    const val versionNumber: String = "1.1.6"

    /**
     * Master test mode toggle.
     * When false (production): Uses live AdMob ad units and production configs.
     * When true (testing): Uses Google test ad units.
     */
    const val testMode: Boolean = false

    /**
     * Ad-Free upgrade test mode toggle.
     * When true, simulates billing purchases without Google Play Billing API.
     */
    const val adFreeUpgradeTestMode: Boolean = false

    // Production AdMob Ad Unit IDs
    const val prodBannerAdUnitId: String = "ca-app-pub-8741391110749449/7936166459"
    const val prodInterstitialAdUnitId: String = "ca-app-pub-8741391110749449/8987606831"
    const val prodNativeAdUnitId: String = "ca-app-pub-8741391110749449/4302256369"

    // Standard Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/2247696110"

    // Active Ad Unit IDs resolved dynamically
    val activeBannerAdUnitId: String
        get() = if (testMode) TEST_BANNER_AD_UNIT_ID else prodBannerAdUnitId

    val activeInterstitialAdUnitId: String
        get() = if (testMode) TEST_INTERSTITIAL_AD_UNIT_ID else prodInterstitialAdUnitId

    val activeNativeAdUnitId: String
        get() = if (testMode) TEST_NATIVE_AD_UNIT_ID else prodNativeAdUnitId

    // Google Play Licensing RSA Public Key
    const val GOOGLE_PLAY_LICENSING_KEY: String =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxboh0WKHc9nnrksVb6CMup5VVzJnuEB0WNNywEmotjLHp62SHjPJK8ONgO55dkTmX30J72APsYRrBzrkZuFY0KpIsbWPEyoQS4yKMpjRtmr3vcD/Ne9RwI7SV3RJd/mZScgzFYFXMpoPi/Do64QpcUq222JW5b3qR5vCN43WhiRdXnM6zFZMgPZ02K8aoe5awO6yIMyHSRjio4FQXKIiWMuhqkqXszFxbu3IPxvsy38jLWemA1X43t50+8aY/QfVbb0lkfSNK9R/ZkRb1EtgB+t7U+1tY90uU1vZyVKQtCFqOklU14SBbn0KnpHvae3jGlwKQdnFzkBg0S/QR87HjQIDAQAB"
}
