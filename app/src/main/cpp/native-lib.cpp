#include <jni.h>
#include <string>
#include "classifier/ei_run_classifier.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_noash_hearmewatch_EdgeImpulseProcessor_runAudioInference(JNIEnv* env, jclass clazz, jfloatArray inputArray) {
    jfloat* inputData = env->GetFloatArrayElements(inputArray, nullptr);

    float buffer[EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE];
    for (int i = 0; i < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE; i++) {
        buffer[i] = inputData[i];
    }

    signal_t signal;
    auto get_signal_data = [&](size_t offset, size_t length, float *out_ptr) -> int {
        memcpy(out_ptr, buffer + offset, length * sizeof(float));
        return 0;
    };
    signal.total_length = EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE;
    signal.get_data = get_signal_data;

    ei_impulse_result_t result;
    EI_IMPULSE_ERROR res = run_classifier(&signal, &result, false);

    std::string output = "Result:\n";
    if (res == EI_IMPULSE_OK) {
        for (size_t i = 0; i < EI_CLASSIFIER_LABEL_COUNT; i++) {
            output += result.classification[i].label;
            output += ": ";
            output += std::to_string(result.classification[i].value * 100);
            output += "%\n";
        }
    } else {
        output = "Error running classifier.";
    }

    env->ReleaseFloatArrayElements(inputArray, inputData, 0);
    return env->NewStringUTF(output.c_str());
}
