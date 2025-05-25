# HearMe Watch

**HearMe Watch** is an Android-based smart assistant designed to enhance environmental awareness for people with hearing impairments. The app uses machine learning models to detect specific environmental sounds, such as names being called or background noises like alarms or speech and immediately notifies the user via smartwatch.

## Motivation

People with hearing impairments often face challenges in detecting important sounds from their surroundings. These challenges can affect safety, communication, and independence. HearMe Watch aims to bridge that gap using on-device AI inference.

## Features

- 📣 Real-time sound recognition
- ⏱️ Runs continuously using a Foreground Service
- 🤖 Dual-model integration:
  - Edge Impulse: for detecting specific keywords (e.g., names)
  - YAMNet: for recognizing general sound events (e.g., speech, alarms)
- 🎧 Processes live audio input (no need to store WAV files)
- 🔔 Notifies users with in-app and UI alerts

## Architecture

- **Foreground Service**: continuously listens using the microphone and processes 1-second audio frames.
- **Edge Impulse Model**: processes audio with JNI and returns classification results.
- **YAMNet Model**: uses TensorFlow Lite for multi-class environmental sound detection.

## Technologies Used

- Kotlin & Java (Android)
- TensorFlow Lite
- Edge Impulse C++ SDK
- Android Foreground Service
- Jetpack Compose UI
- JNI Integration for native inference

## Running the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/inbar26/HearMeWatch.git
