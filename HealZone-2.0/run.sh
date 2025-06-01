#!/bin/bash
echo "Checking Docker daemon..."
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker daemon is not running or you lack permissions. Run 'sudo systemctl start docker' or add your user to the docker group."
    exit 1
fi

echo "Checking X11 display..."
if [ -z "$DISPLAY" ]; then
    echo "Warning: DISPLAY variable is not set. Setting to :0"
    export DISPLAY=:0
fi
echo "DISPLAY is set to $DISPLAY"

echo "Verifying X11 server..."
if ! xdpyinfo >/dev/null 2>&1; then
    echo "Error: X11 server is not running or inaccessible. Install 'x11-utils' and ensure an X server is active."
    echo "Try: sudo apt install x11-utils"
    exit 1
fi

echo "Checking X11 socket..."
if [ ! -e /tmp/.X11-unix/X0 ]; then
    echo "Error: X11 socket (/tmp/.X11-unix/X0) not found. Ensure X11 is running."
    exit 1
fi

echo "Setting up X11 permissions..."
xhost +local:docker >/dev/null 2>&1 || {
    echo "Warning: 'xhost +local:docker' failed. Trying fallback..."
    xhost +si:localuser:root >/dev/null 2>&1 || {
        echo "Warning: 'xhost +si:localuser:root' failed. Using 'xhost +' as fallback..."
        xhost + >/dev/null 2>&1 || {
            echo "Error: Failed to set X11 permissions. Ensure X11 is configured."
            exit 1
        }
    }
}

echo "Building Docker image for HealZone..."
docker build -t healzone .
if [ $? -ne 0 ]; then
    echo "Error: Docker build failed."
    exit 1
fi

echo "Running HealZone application..."
mkdir -p ~/.healzone
docker run --rm -it \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -v ~/.healzone:/app/db \
    -e SQLITE_DB_PATH=/app/db/healzone.db \
    -p 8080:8080 \
    --name healzone \
    healzone
if [ $? -ne 0 ]; then
    echo "Error: Docker run failed. Check logs with 'docker logs healzone' or verify X11 setup."
    echo "Try running 'xterm' locally to test X11."
    exit 1
fi