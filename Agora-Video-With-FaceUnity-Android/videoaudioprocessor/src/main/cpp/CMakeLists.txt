# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

#used just for android only
project(raw_data_process)
set(TARGET_NAME ${PROJECT_NAME})
cmake_minimum_required(VERSION 3.4.1)


#set (ENABLE_ANDROID OFF)
#set (ENABLE_IOS ON)

set(CMAKE_C_FLAGS_DEBUG "-g -Wall")
set(CMAKE_CXX_FLAGS_DEBUG ${CMAKE_C_FLAGS_DEBUG})
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wno-deprecated-declarations -Wno-reorder")
if(CLANG)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
set(SRC_PATH ${CMAKE_CURRENT_SOURCE_DIR})
add_definitions(-DNO_CRYPTO)
set(SOURCES
	${SRC_PATH}/raw_data_process_jni.cpp
)
message("into android build")
message("add static library")
include_directories(${SRC_PATH}/include)
add_library(libyuv STATIC IMPORTED)
set_target_properties(libyuv PROPERTIES IMPORTED_LOCATION ${SRC_PATH}/jniLibs/${ANDROID_ABI}/libyuv.a)


add_library(${TARGET_NAME} SHARED ${SOURCES})
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
set (PUBLIC_LINK_LIBS dl log libyuv m z)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.

                        ${PROJECT_NAME}
                       ${PUBLIC_LINK_LIBS} )