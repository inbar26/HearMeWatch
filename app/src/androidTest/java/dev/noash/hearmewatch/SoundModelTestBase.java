package dev.noash.hearmewatch;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SoundModelTestBase {

    public enum ModelType {
        EDGE_IMPULSE,
        YAMNET
    }

    // Run model and get top prediction label
    public String runModelAndGetTopLabel(Context context, String assetPath, ModelType modelType) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(assetPath);

        byte[] wavBytes = readWavFileToPCM(inputStream);
        inputStream.close();

        int numSamples = wavBytes.length / 2;
        float[] inputEI = new float[numSamples];
        ByteBuffer fullBuffer = ByteBuffer.allocateDirect(wavBytes.length).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < numSamples; i++) {
            short sample = (short) ((wavBytes[i * 2] & 0xff) | (wavBytes[i * 2 + 1] << 8));
            inputEI[i] = sample / 32768f;
            fullBuffer.putShort(sample);
        }

        if (modelType == ModelType.EDGE_IMPULSE) {
            String result = EdgeImpulseProcessor.runAudioInference(inputEI);
            return extractTopLabel(result);
        } else {
            Log.d("YAMNET", "Buffer capacity: " + fullBuffer.capacity() + " bytes");
            fullBuffer.rewind();
            List<String> results = YamnetRunner.runOnBuffer(context, fullBuffer);

            for (String label : results) {
                Log.d("YAMNET", "🔍 Considering label: " + label);
            }
            return results.isEmpty() ? null : String.join(",", results);
        }
    }

    // Extract the top-scoring label from Edge Impulse result string
    public static String extractTopLabel(String resultString) {
        String[] lines = resultString.split("\n");
        String bestLabel = null;
        float bestScore = 0f;

        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String label = parts[0].trim();
                float score;
                try {
                    score = Float.parseFloat(parts[1].replace("%", "").trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = label;
                }
            }
        }
        return bestLabel;
    }

    // Load label list from asset file
    public String[] loadLabels(Context context, String assetLabelFile) {
        List<String> labels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(assetLabelFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            Log.e("LABELS", "Failed to load labels from " + assetLabelFile, e);
        }
        return labels.toArray(new String[0]);
    }

    // Check if a label exists in the label list
    public boolean labelExistsInList(String[] labelList, String label) {
        for (String lbl : labelList) {
            if (lbl.equalsIgnoreCase(label)) return true;
        }
        return false;
    }

    // Read PCM data from WAV (skip header)
    private byte[] readWavFileToPCM(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] header = new byte[44];
        is.read(header, 0, 44); // Skip WAV header
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        return out.toByteArray();
    }
}
