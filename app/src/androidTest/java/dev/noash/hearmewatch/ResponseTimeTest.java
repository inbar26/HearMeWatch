package dev.noash.hearmewatch;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.io.IOException;

public class ResponseTimeTest extends SoundModelTestBase {
    private static final String TAG = "RESPONSE_TIME";
    private static final long MAX_ALLOWED_MS = 1000;

    @Test
    public void testModelResponseTime() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try {
            ModelHelper.initializeModels(context); // ✅ Initialize both models
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize models", e);
        }

        try {
            String[] categories = context.getAssets().list("sounds");
            if (categories == null) {
                Log.e(TAG, "No categories found in assets/sounds/");
                return;
            }

            for (String category : categories) {
                String basePath = "sounds/" + category;
                boolean isEdgeImpulse = category.equalsIgnoreCase("name calling");

                String[] folders = isEdgeImpulse ? context.getAssets().list(basePath) : new String[]{""};

                for (String subfolder : folders) {
                    String folderPath = isEdgeImpulse ? basePath + "/" + subfolder : basePath;
                    String[] files = context.getAssets().list(folderPath);

                    if (files == null) continue;

                    for (String fileName : files) {
                        if (!fileName.endsWith(".wav")) continue;

                        String assetPath = folderPath + "/" + fileName;

                        long start = System.nanoTime();
                        String prediction = runModelAndGetTopLabel(
                                context,
                                assetPath,
                                isEdgeImpulse ? ModelType.EDGE_IMPULSE : ModelType.YAMNET
                        );
                        long durationMs = (System.nanoTime() - start) / 1_000_000;

                        Log.d(TAG, "⏱ " + assetPath + " took " + durationMs + "ms");

                        // 🟡 Optional: print label count if using YAMNet
                        if (!isEdgeImpulse && prediction != null) {
                            String[] predictedLabels = prediction.split(",");
                            Log.d(TAG, "📊 Labels returned: " + predictedLabels.length + " -> " + prediction);
                        }

                        assertTrue("Model inference took too long: " + durationMs + "ms", durationMs <= MAX_ALLOWED_MS);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading assets", e);
        }
    }
}