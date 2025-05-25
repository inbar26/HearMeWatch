package dev.noash.hearmewatch

object EdgeImpulseProcessor {
    @JvmStatic
    external fun runAudioInference(input: FloatArray): String

    init {
        System.loadLibrary("models")
    }
}
