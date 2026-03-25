
#!/bin/bash
# Compile script for SDA (Prison Management System)

echo "Compiling SE Project..."

# Path to your JavaFX SDK
PATH_TO_FX="/root/javafx-sdk-22.0.1/lib"

# Output directory
OUT_DIR="out/production"

# Create output directory
mkdir -p $OUT_DIR

# Compile all Java files
javac -d $OUT_DIR \
    --module-path $PATH_TO_FX \
    --add-modules javafx.controls,javafx.fxml \
    -cp "lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" \
    --source-path src \
    $(find src -name "*.java")


# Copy FXML resources
if [ -d "src/main/resources/fxml" ]; then
    mkdir -p $OUT_DIR/fxml
    cp -r src/main/resources/fxml/* $OUT_DIR/fxml/
fi

echo "Compilation complete!"
