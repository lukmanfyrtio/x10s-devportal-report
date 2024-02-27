#!/bin/bash

SERVER_IP="103.164.54.199"
USERNAME="root"

echo "Connecting to server $SERVER_IP as user $USERNAME..."

# Add any additional commands or actions you want to perform after connecting to the server
# For example, you can run remote commands, transfer files, etc.

# Replace the following line with your desired SSH command or action
ssh "$USERNAME@$SERVER_IP"

echo "SSH connection closed."

