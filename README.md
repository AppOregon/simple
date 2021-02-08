[![Build Status](https://travis-ci.org/PacificEngine/simple.svg?branch=main)](https://travis-ci.org/PacificEngine/simple)

Compile
```bash
gradlew clean build pom test "-Dorg.gradle.java.home=F:/Program Files/Java/openjdk-8u272-b10"
```

Pre-Commit
```bash
rm -rf gradle/ gradlew gradlew.bat
gradle wrapper --gradle-version=6.8.2 --distribution-type=all
git add gradle/ gradlew gradlew.bat
git update-index --chmod=+x gradle/wrapper/gradle-wrapper.jar
git update-index --chmod=+x gradlew
git update-index --chmod=+x gradlew.bat
git add gradle/ gradlew gradlew.bat
```