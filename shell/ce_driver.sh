#!/bin/bash

# Check if the driver is currently loaded
if lsmod | grep "^CE_AgentDriver " > /dev/null; then
    echo "CE_AgentDriver is installed."
    # Check if the driver is being used by another module
    if lsmod | grep "^CE_AgentDriver " | awk '{print $3}' | grep -q "0"; then
        echo "CE_AgentDriver is not being used. Attempting to remove..."
        # Attempt to remove the driver
        if sudo rmmod CE_AgentDriver; then
            echo "CE_AgentDriver removed successfully."
        else
            echo "Failed to remove CE_AgentDriver. Please check the system logs for more information."
        fi
    else
        echo "CE_AgentDriver is currently in use and cannot be safely removed."
    fi
else
    echo "CE_AgentDriver is not installed."
fi
