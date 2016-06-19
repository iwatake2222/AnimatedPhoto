# Animated Photo
https://play.google.com/store/apps/details?id=com.iwiw.take.animatedphoto

## How to add OpenCV in Android studio
- OpenCV library is already included in this project. So, you don't need the following process
- download OpenCV-3.1.0-android-sdk.zip
- change target version
    - ~/AndroidStudio/OpenCV-android-sdk/sdk/java/project.properties
    - target=android-23
- copy library files
    - from: ~/AndroidStudio/OpenCV-android-sdk/sdk/native/libs
    - to: ~/AndroidStudio/AnimatedPhoto/app/src/main/jniLibs
- add library in a Android Project
    - File -> Project Structure -> "+" (New Module) -> Import Gradle Project
        - ~/AndroidStudio/OpenCV-android-sdk/sdk/java
    - File -> Project Structure -> app -> Dependencies -> "+" -> Module dependency
        - :openCVLibrary310
