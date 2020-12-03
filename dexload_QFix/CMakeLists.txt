# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.10.2)


add_library(qfix SHARED src/main/cpp/qfix_resolve.cpp)
find_library(log-lib log)
target_link_libraries(qfix  ${log-lib})


