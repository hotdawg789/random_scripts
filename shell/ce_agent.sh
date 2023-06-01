#!/bin/bash

# Check if CE_AgentDriver is loaded
if lsmod | grep -q CE_AgentDriver; then

  # Check if CE_AgentDriver is used by any processes
  used_by=$(lsof -nP | grep CE_AgentDriver | wc -l)

  # If CE_AgentDriver is not used by any processes, remove it
  if [ "$used_by" -eq 0 ]; then
    sudo rmmod CE_AgentDriver
  fi
fi
