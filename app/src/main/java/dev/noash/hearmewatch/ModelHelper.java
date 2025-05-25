package dev.noash.hearmewatch;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelHelper {

    private static Interpreter edgeModel;
    private static Interpreter yamnetModel;

    public static void initializeModels(Context context) throws IOException {
        edgeModel = new Interpreter(loadModelFile(context, "trained.tflite"));
        yamnetModel = new Interpreter(loadModelFile(context, "1.tflite"));
    }

    private static MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    public static Interpreter getEdgeModel() {
        return edgeModel;
    }

    public static Interpreter getYamnetModel() {
        return yamnetModel;
    }
}