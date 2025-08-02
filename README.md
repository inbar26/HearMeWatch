# HearMe Watch

**HearMe Watch** is an Android-based smart assistant designed to enhance environmental awareness for people with hearing impairments. The app uses machine learning models to detect specific environmental sounds, such as names being called or background noises like alarms or speech and immediately notifies the user via smartwatch.

## Motivation

People with hearing impairments often face challenges in detecting important sounds from their surroundings. These challenges can affect safety, communication, and independence. HearMe Watch aims to bridge that gap using on-device AI inference.

## Features

- 📣 Real-time sound classification
- ⏱️ Continuous listening via a Foreground Service
- 🤖 Dual-model inference:
  - **Edge Impulse** for detecting specific keywords (e.g., names)
  - **YAMNet** for identifying general environmental sounds (e.g., speech, alarms)
- 🎧 Live microphone input (no file storage required)
- 🔔 Immediate alerts shown visually and tactically on Wear OS smartwatches
- 🛠️ Customizable sound categories based on user preferences (via Firebase)

## 🧩 System Architecture

- **Foreground Service**: captures 1-second PCM audio buffers from the microphone in real time.
- **Edge Impulse Model**: native C++ inference via JNI for keyword spotting.
- **YAMNet Model**: TensorFlow Lite-based model for multi-class audio classification.
- **SharedPreferences + Firebase**: used to store and sync user-defined sound preferences.
- **Wear OS Data Layer API**: sends alerts from phone to watch (bi-directional messaging supported).

## Technologies Used

- Kotlin & Java (Android)
- TensorFlow Lite
- [YAMNet Model](app/src/main/java/dev/noash/hearmewatch/YamnetRunner.java)
- [Edge Impulse C++ SDK](app/src/main/cpp)
- [Foreground Service](app/src/main/java/dev/noash/hearmewatch/Foreground/MyForegroundService.java)
- [Firebase Realtime Database](app/src/main/java/dev/noash/hearmewatch/Utilities/DBManager.java)
- [SharedPreferences](app/src/main/java/dev/noash/hearmewatch/Utilities/SPManager.java)
- [Wear OS communication (MessageClient / NodeClient)](wearos/src/main/java/dev/noash/hearmewatch/MessageReceiverService.java)
- [JNI Integration for native inference](app/src/main/cpp/native-lib.cpp)

## 🧪 Testing

- ✅ Accuracy tests using categorized WAV datasets (Edge Impulse and YAMNet)
- 🕒 End-to-end response time tests (from sound detection to watch alert)

 ## 👩‍💻 Developers
- [Noa Sharabi](https://www.linkedin.com/in/noa-sharabi-32616329b/)
- [Inbar Zafar](https://www.linkedin.com/in/inbarzafar/)

## Running the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/inbar26/HearMeWatch.git

2. Open the project in Android Studio.

3. Ensure you have a connected Wear OS device or emulator.

4. Build and run the app on both phone and watch modules.


