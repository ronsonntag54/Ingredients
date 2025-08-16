subprojects {
    plugins.withId("com.android.application") {
        // Nothing needed here unless customization
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        // Same, this ensures plugin is applied
    }
}
