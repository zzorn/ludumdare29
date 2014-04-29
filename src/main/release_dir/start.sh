#!/bin/sh

# Get current dir
PREVIOUS_DIR="$PWD"

# Get directory this script is located in
PROGRAM_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Enter program directory
cd $PROGRAM_DIR

# Run the program
java -jar ludumdare29_CrushingDepth_zzorn.jar

# Return to previous dir
cd $PREVIOUS_DIR

