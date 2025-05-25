cmake_minimum_required(VERSION 3.22.1)

project("models")

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/dsp
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/CMSIS
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/micro
        ${CMAKE_CURRENT_SOURCE_DIR}/model-parameters
        ${CMAKE_CURRENT_SOURCE_DIR}/tflite-model
)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp
)

file(GLOB_RECURSE MODEL_CPP "tflite-model/*.cpp")
target_sources(${CMAKE_PROJECT_NAME} PRIVATE ${MODEL_CPP})

file(GLOB_RECURSE SDK_CPP_ALL "edge-impulse-sdk/**/*.cpp")
file(GLOB_RECURSE SDK_C "edge-impulse-sdk/**/*.c")

set(SDK_CPP "")
foreach(file ${SDK_CPP_ALL})
    if (NOT ${file} MATCHES ".*/porting/posix/.*")
        list(APPEND SDK_CPP ${file})
    endif()
endforeach()

target_sources(${CMAKE_PROJECT_NAME} PRIVATE ${SDK_CPP} ${SDK_C})

# TensorFlow Lite for Microcontrollers
file(GLOB_RECURSE TFLM_CC
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/micro/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/core/api/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/kernels/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/kernels/internal/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/lite/c/*.cc"
)
target_sources(${CMAKE_PROJECT_NAME} PRIVATE ${TFLM_CC})

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log
        atomic
        m
)
