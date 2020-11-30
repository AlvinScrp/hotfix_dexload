# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.10.2)

add_library(cydiahook SHARED src/main/cpp/cydia-hook.cpp)
target_include_directories(cydiahook PRIVATE  ${CMAKE_SOURCE_DIR}/src/main/cpp/include)
find_library(log-lib log)
file(GLOB libs ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libsubstrate.so ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libsubstrate-dvm.so)
target_link_libraries( cydiahook   ${libs}  ${log-lib})




