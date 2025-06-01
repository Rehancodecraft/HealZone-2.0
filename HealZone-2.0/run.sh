#!/bin/bash
echo "Checking Docker daemon..."
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker daemon not running or insufficient permissions. Run 'sudo systemctl start docker' or add user to docker group."
    exit 1
fi

echo "Checking X11 display..."
if [ -z "$DISPLAY" ]; then
    echo "Warning: DISPLAY not set. Setting to :0"
    export DISPLAY=:0
fi
echo "DISPLAY is $DISPLAY"

echo "Verifying X11 server..."
if ! xdpyinfo >/dev/null 2>&1; then
    echo "Error: X11 server not running. Install 'x11-utils' with 'sudo apt install x11-utils'."
    exit 1
fi

echo "Checking X11 socket..."
if [ ! -e /tmp/.X11-unix/X0 ]; then
    echo "Error: X11 socket (/tmp/.X11-unix/X0) not found."
    exit 1
fi

echo "Setting X11 permissions..."
xhost +local:docker >/dev/null 2>&1 || xhost +si:localuser:root >/dev/null 2>&1 || xhost + >/dev/null 2>&1 || {
    echo "Warning: X11 permissions failed. Falling back to Xvfb."
    USE_XVFB=1
}

echo "Building Docker image for HealZone..."
docker build -t healzone .
if [ $? -ne 0 ]; then
    echo "Error: Docker build failed."
    exit 1
fi

echo "Running HealZone application..."
mkdir -p ~/.healzone
if [ "${USE_XVFB:-0}" -eq 1 ]; then
    echo "Using Xvfb virtual display..."
    docker run --rm -it \
        -v ~/.healzone:/app/db \
        -e SQLITE_DB_PATH=/app/db/healzone.db \
        -p 8080:8080 \
        --name healzone \
        healzone \
        /bin/bash -c "Xvfb :99 -screen 0 1280x720x24 & export DISPLAY=:99 && java --module-path /opt/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml -jar app.jar"
else
    docker run --rm -it \
        -e DISPLAY=$DISPLAY \
        -v /tmp/.X11-unix:/tmp/.X11-unix \
        --network host \
        -v ~/.healzone:/app/db \
        -e SQLITE_DB_PATH=/app/db/healzone.db \
        -p 8080:8080 \
        --name healzone \
        healzone
fi
if [ $? -ne 0 ]; then
    echo "Error: Docker run failed. Check logs with 'docker logs healzone'."
    exit 1
fi