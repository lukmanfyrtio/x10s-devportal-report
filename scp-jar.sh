#!/bin/bash

#SERVER_IP="103.164.54.199"
SERVER_IP="bulog_2"
USERNAME="root"
TARGET_FOLDER="/root/x10s-backend/devportal"

echo "Copying JAR file to server $SERVER_IP as user $USERNAME..."

# Replace `your-jar-file.jar` with the actual name of your JAR file
rsync -avz target/report-usage-api-0.0.1-SNAPSHOT.jar "$USERNAME@$SERVER_IP:$TARGET_FOLDER"

echo "JAR file copied successfully."

