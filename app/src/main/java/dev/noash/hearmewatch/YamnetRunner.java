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

    public static String runOnBuffer(Context context, ByteBuffer buffer) {
        try {
            // Convert PCM 16-bit buffer to normalized float array
            float[] audioData = new float[Math.min(buffer.capacity() / 2, 15600)];
            buffer.rewind();
            for (int i = 0; i < audioData.length; i++) {
                audioData[i] = buffer.getShort() / 32768f;
            }

            if (audioData.length < 15600) return "Audio too short";

            Interpreter yamnet = ModelHelper.getYamnetModel();

            float[][] input = new float[1][15600];
            System.arraycopy(audioData, 0, input[0], 0, 15600);

            float[][] output = new float[1][521];
            yamnet.run(input, output);

            List<String> labels = loadLabels(context);
            int bestIndex = 0;
            float bestScore = 0f;
            for (int i = 0; i < 521; i++) {
                if (output[0][i] > bestScore) {
                    bestScore = output[0][i];
                    bestIndex = i;
                }
            }

            String label = labels.get(bestIndex);
            String result = label + " (" + (int)(bestScore * 100) + "%)";
            Log.d("YAMNet", "Detected: " + result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "YAMNet Error";
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
