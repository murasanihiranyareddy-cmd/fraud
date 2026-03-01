package com.fraud_detector.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val smsInterpreter: Interpreter by lazy {
        Interpreter(loadModel("models/sms_classifier.tflite"))
    }
    private val callInterpreter: Interpreter by lazy {
        Interpreter(loadModel("models/call_fraud_detector.tflite"))
    }
    private val urlInterpreter: Interpreter by lazy {
        Interpreter(loadModel("models/url_embedding_model.tflite"))
    }

    fun runSmsModel(text: String): Float {
        val input = tokenize(text, 128)
        val output = Array(1) { FloatArray(1) }
        smsInterpreter.run(input, output)
        return output[0][0]
    }

    fun runCallModel(number: String, duration: Long): Float {
        val input = floatArrayOf(
            number.length.toFloat(),
            number.filter { it == '#' || it == '*' }.length.toFloat(),
            duration.toFloat()
        )
        val buf = ByteBuffer.allocateDirect(4 * 3).apply {
            order(ByteOrder.nativeOrder())
            input.forEach { putFloat(it) }
            rewind()
        }
        val output = Array(1) { FloatArray(1) }
        callInterpreter.run(buf, output)
        return output[0][0]
    }

    fun runUrlModel(url: String): Float {
        val input = tokenize(url, 64)
        val output = Array(1) { FloatArray(1) }
        urlInterpreter.run(input, output)
        return output[0][0]
    }

    private fun tokenize(text: String, maxLen: Int): ByteBuffer {
        val tokens = text.lowercase().split(" ")
            .take(maxLen)
            .map { it.hashCode() % 10000 }
        val buf = ByteBuffer.allocateDirect(4 * maxLen).order(ByteOrder.nativeOrder())
        repeat(maxLen) { i -> buf.putFloat(if (i < tokens.size) tokens[i].toFloat() else 0f) }
        return buf.apply { rewind() }
    }

    private fun loadModel(assetPath: String): MappedByteBuffer {
        val fd = context.assets.openFd(assetPath)
        return FileInputStream(fd.fileDescriptor).channel
            .map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }
}