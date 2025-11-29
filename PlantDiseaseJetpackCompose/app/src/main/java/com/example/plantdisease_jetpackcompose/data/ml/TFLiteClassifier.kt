package com.example.plantdisease_jetpackcompose.data.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
class TFLiteClassifier @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val imageSize = 224 //200
    private val numClasses = 5

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (interpreter == null) {
            interpreter = Interpreter(loadModelFile("model.tflite"))
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun classify(bitmap: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        val interpreter = interpreter ?: throw IllegalStateException("Model not initialized")

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Normalize manually
        val buffer = tensorImage.buffer
        val normalizedBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, imageSize, imageSize, 3),
            org.tensorflow.lite.DataType.FLOAT32
        )

        val floatArray = FloatArray(imageSize * imageSize * 3)
        buffer.rewind()
        buffer.asFloatBuffer().get(floatArray)

        for (i in floatArray.indices) {
            floatArray[i] = floatArray[i] / 255f
        }

        normalizedBuffer.loadArray(floatArray)

        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, numClasses),
            org.tensorflow.lite.DataType.FLOAT32
        )

        interpreter.run(normalizedBuffer.buffer, outputBuffer.buffer.rewind())

        outputBuffer.floatArray
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

