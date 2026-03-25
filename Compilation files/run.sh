#!/bin/bash
# Run script for SDA (Prison Management System)

echo "Starting SDA..."

# Path to JavaFX SDK
PATH_TO_FX="/root/javafx-sdk-22.0.1/lib"

# Main class (change if your main class is different)
MAIN_CLASS="com.prison.PrisonManagementApp"

java \
    --module-path $PATH_TO_FX \
    --add-modules javafx.controls,javafx.fxml \
    -cp "out/production:lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" \
    $MAIN_CLASS

echo "SDA has exited."