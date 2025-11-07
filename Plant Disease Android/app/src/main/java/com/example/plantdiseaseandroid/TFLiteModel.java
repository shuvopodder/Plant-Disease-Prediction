package com.example.plantdiseaseandroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModel {
    private Interpreter interpreter;
    private final int imageSize = 150;

    public TFLiteModel(Context context) throws IOException{
        interpreter = new Interpreter(loadModelFile(context,"model.tflite"));
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset, declaredLength);
    }

    public float[] classify(Bitmap bitmap) throws IOException {

        // Preprocessing
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        TensorImage tensorImage = new TensorImage(org.tensorflow.lite.DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        // Create output buffer
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(
                new int[]{1, getNumClasses()},
                org.tensorflow.lite.DataType.FLOAT32
        );

        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        return outputBuffer.getFloatArray();
    }

    private int getNumClasses() {
        return 5; // change based on your model (e.g. 2 for healthy vs blight)
    }
    public void close() {
        if (interpreter != null){
            interpreter.close();
        }
    }
}
