package com.signtogether.model

/**
 * Interface demonstrating the architectural foundation for Phase 10: Multi-language + Scalability.
 * 
 * Future implementations for ASL (American Sign Language) or BSL (British Sign Language) 
 * will implement this interface. The dependency injection framework will provide the 
 * correct implementation based on the user's selected language in SettingsScreen,
 * preventing hardcoding of ISL paths exclusively in the core processing logic.
 */
interface LanguageModelManager {
    /**
     * Load the specific machine learning model for the selected sign language.
     * @param languageCode The locale code (e.g., "en-IN" for ISL, "en-US" for ASL)
     */
    fun loadModel(languageCode: String)

    /**
     * Process a given frame or set of landmarks to predict the sign.
     * @param frameData Represents the landmarks or raw frame buffer.
     * @return The predicted text token.
     */
    fun processFrame(frameData: Any): String

    /**
     * @return List of supported language codes by this specific manager implementation.
     */
    fun getSupportedLanguages(): List<String>
}

/**
 * Concrete implementation for the MVP (Indian Sign Language).
 * Currently, the logic is embedded within NativeModeActivity and SignLanguageProcessor.
 * In a future phase, that logic will be migrated to this concrete class.
 */
class ISLModelManager : LanguageModelManager {
    override fun loadModel(languageCode: String) {
        if (languageCode != "hi-IN" && languageCode != "en-IN") {
            throw IllegalArgumentException("ISLModelManager only supports Indian locales.")
        }
        // Load ISL specific .tflite models here
    }

    override fun processFrame(frameData: Any): String {
        // Delegate to existing SignLanguageProcessor logic
        return ""
    }

    override fun getSupportedLanguages(): List<String> {
        return listOf("hi-IN", "en-IN")
    }
}
