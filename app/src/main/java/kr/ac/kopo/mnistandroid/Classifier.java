package kr.ac.kopo.mnistandroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class Classifier {
    private static final String TAG = "Classifier";
    private static final String MODEL_NAME = "mnist_model.tflite";

    private final Context context;
    private Interpreter interpreter;

    private int modelInputWidth;
    private int modelInputHeight;
    private int modelInputChannel;
    private int modelOutputClasses;

    public Classifier(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        try {
            ByteBuffer modelBuffer = loadModelFile(MODEL_NAME);
            modelBuffer.order(ByteOrder.nativeOrder());

            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(modelBuffer, options);

            initModelShape();
            Log.d(TAG, "Model initialized successfully");
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize model", e);
            throw e;
        }
    }

    private ByteBuffer loadModelFile(String modelName) throws IOException {
        AssetManager assetManager = context.getAssets();
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(modelName);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            Log.e(TAG, "Model file not found or unreadable: " + modelName, e);
            throw e;
        }
    }

    private void initModelShape() {
        int[] inputShape = interpreter.getInputTensor(0).shape(); // [1, width, height, channel]
        if (inputShape.length == 4) {
            modelInputWidth = inputShape[1];
            modelInputHeight = inputShape[2];
            modelInputChannel = inputShape[3];
        } else {
            modelInputWidth = inputShape[1];
            modelInputHeight = inputShape[2];
            modelInputChannel = 1;
        }

        int[] outputShape = interpreter.getOutputTensor(0).shape(); // [1, num_classes]
        modelOutputClasses = outputShape[1];

        Log.d(TAG, "Model input shape: " + modelInputWidth + "x" + modelInputHeight + "x" + modelInputChannel);
        Log.d(TAG, "Model output classes: " + modelOutputClasses);
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false);
    }

    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap) {
        int inputSize = modelInputWidth * modelInputHeight * modelInputChannel;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize * Float.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            float gray = (r + g + b) / 3.0f / 255.0f;
            byteBuffer.putFloat(gray);
        }

        return byteBuffer;
    }

    private Pair<Integer, Float> argmax(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxIndex = i;
                maxValue = array[i];
            }
        }
        return new Pair<>(maxIndex, maxValue);
    }

    public Pair<Integer, Float> classify(Bitmap image) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is not initialized");
            return new Pair<>(-1, 0f);
        }

        Bitmap resized = resizeBitmap(image);
        ByteBuffer inputBuffer = convertBitmapToGrayByteBuffer(resized);
        float[][] output = new float[1][modelOutputClasses];

        interpreter.run(inputBuffer, output);
        return argmax(output[0]);
    }

    public void finish() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
