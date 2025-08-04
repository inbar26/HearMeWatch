package dev.noash.hearmewatch;

import static org.junit.Assert.assertTrue;

import android.util.Log;
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.util.Locale;
import java.io.IOException;

public class SoundRecognitionAccuracyTest extends SoundModelTestBase {
    private static final String TAG = "ACCURACY";
    private static final double MIN_ACCURACY_THRESHOLD = 0.80;

    @Test
    public void testSoundRecognitionAccuracy() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try {
            ModelHelper.initializeModels(context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize models", e);
        }

        int correct = 0;
        int total = 0;

        try {
            for (String category : context.getAssets().list("sounds")) {
                boolean isEdgeImpulse = category.equalsIgnoreCase("name calling");
                String[] modelLabels = loadLabels(context, isEdgeImpulse ? "edgeImpulse_label_list.txt" : "yamnet_label_list.txt");

                if (isEdgeImpulse) {
                    for (String subfolder : context.getAssets().list("sounds/" + category)) {
                        String expectedLabel = subfolder.toLowerCase(Locale.ROOT);
                        if (!labelExistsInList(modelLabels, expectedLabel)) {
                            Log.w(TAG, "Skipping: " + expectedLabel + " not in edgeImpulse_label_list.txt");
                            continue;
                        }
                        for (String file : context.getAssets().list("sounds/" + category + "/" + subfolder)) {
                            if (!file.endsWith(".wav")) continue;
                            String assetPath = "sounds/" + category + "/" + subfolder + "/" + file;
                            String predictedLabel = runModelAndGetTopLabel(context, assetPath, ModelType.EDGE_IMPULSE);
                            boolean match = predictedLabel.equalsIgnoreCase(expectedLabel);
                            Log.d("ACCURACY_TEST", "EDGE_IMPULSE File: " + file +
                                    " | Expected: " + expectedLabel +
                                    " | Got: " + predictedLabel +
                                    " | Match: " + match);
                            total++;
                            if (match) correct++;
                        }
                    }
                } else {
                    String expectedCategory = category.toLowerCase(Locale.ROOT);
                    if (!labelExistsInList(modelLabels, expectedCategory)) {
                        Log.w(TAG, "Skipping: " + expectedCategory + " not in yamnet_label_list.txt");
                        continue;
                    }
                    for (String file : context.getAssets().list("sounds/" + category)) {
                        if (!file.endsWith(".wav")) continue;
                        String assetPath = "sounds/" + category + "/" + file;
                        String predictedLabel = runModelAndGetTopLabel(context, assetPath, ModelType.YAMNET);

                        boolean match = false;
                        String matchedLabel = null;

                        if (predictedLabel != null) {
                            String trimmedFull = predictedLabel.trim();
                            Log.d("YAMNET", "🔍 Full predicted label: " + trimmedFull);

                            String mappedCategory = LabelCategoryMap.getCategory(trimmedFull);
                            if (mappedCategory != null && expectedCategory.equalsIgnoreCase(mappedCategory)) {
                                match = true;
                                matchedLabel = trimmedFull;
                            } else {

                                for (String label : trimmedFull.split(",")) {
                                    String trimmed = label.trim();
                                    Log.d("YAMNET", "🔍 Considering label: " + trimmed);
                                    mappedCategory = LabelCategoryMap.getCategory(trimmed);
                                    if (mappedCategory != null && expectedCategory.equalsIgnoreCase(mappedCategory)) {
                                        match = true;
                                        matchedLabel = trimmed;
                                        break;
                                    }
                                }
                            }
                        }

                        Log.d("ACCURACY_TEST", "YAMNET File: " + file +
                                " | Expected: " + expectedCategory +
                                " | Got: " + (matchedLabel != null ? matchedLabel : "None") +
                                " | Match: " + match);
                        total++;
                        if (match) correct++;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading assets", e);
        }

        double accuracy = total > 0 ? (double) correct / total : 0;
        Log.d(TAG, (correct > 0 ? "✔" : "✖") + " Total correct: " + correct + " / " + total + " = " + (accuracy * 100) + "%");
        assertTrue("Model accuracy is too low", accuracy >= MIN_ACCURACY_THRESHOLD);
    }
}
