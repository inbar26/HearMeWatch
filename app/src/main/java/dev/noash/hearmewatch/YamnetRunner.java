package dev.noash.hearmewatch;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class YamnetRunner {

    public static List<String> runOnBuffer(Context context, ByteBuffer buffer) {
        List<String> topLabels = new ArrayList<>();

        try {
            // Convert PCM 16-bit buffer to normalized float array
            float[] audioData = new float[Math.min(buffer.capacity() / 2, 15600)];
            buffer.rewind();
            for (int i = 0; i < audioData.length; i++) {
                audioData[i] = buffer.getShort() / 32768f;
            }

            if (audioData.length < 15600) {
                Log.w("YAMNet", "Audio too short");
                return topLabels;
            }

            Interpreter yamnet = ModelHelper.getYamnetModel();

            float[][] input = new float[1][15600];
            System.arraycopy(audioData, 0, input[0], 0, 15600);

            float[][] output = new float[1][521];
            yamnet.run(input, output);

            List<String> labels = loadLabels(context);

            // Step 1: find max score
            float bestScore = 0f;
            for (int i = 0; i < 521; i++) {
                if (output[0][i] > bestScore) {
                    bestScore = output[0][i];
                }
            }

            // Step 2: collect all labels with max score
            for (int i = 0; i < 521; i++) {
                if (Math.abs(output[0][i] - bestScore) < 1e-6) { // float precision
                    topLabels.add(labels.get(i));
                }
            }

            Log.d("YAMNet", "Top categories: " + topLabels + " (" + (int)(bestScore * 100) + "%)");

            return topLabels;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("YAMNet", "Error during inference: " + e.getMessage());
            return topLabels;
        }
    }


    private static List<String> loadLabels(Context context) throws IOException {
        List<String> labels = new ArrayList<>();
        InputStream inputStream = context.getAssets().open("yamnet_label_list.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        return labels;
    }
}
