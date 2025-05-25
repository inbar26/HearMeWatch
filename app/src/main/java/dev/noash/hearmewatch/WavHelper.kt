package dev.noash.hearmewatch

import android.content.Context
import java.io.File

object WavHelper {
    @JvmStatic
    fun loadWavAsFloatArray(context: Context, filePath: String): FloatArray {
        val inputStream = File(filePath).inputStream()
        inputStream.skip(44)
        val byteArray = inputStream.readBytes()
        val shortArray = ShortArray(byteArray.size / 2)
        for (i in shortArray.indices) {
            val low = byteArray[i * 2].toInt() and 0xff
            val high = byteArray[i * 2 + 1].toInt()
            shortArray[i] = ((high shl 8) or low).toShort()
        }
        return shortArray.map { it / 32768.0f }.toFloatArray()
    }
}