
#!/bin/bash
#!/bin/bash
echo "Checking Docker daemon..."
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker daemon is not running or you lack permissions. Run 'sudo systemctl start docker' or add your user to the docker group."
    exit 1
fi

echo "Checking X11 display..."
if [ -z "$DISPLAY" ]; then
    echo "Error: DISPLAY variable is not set. Setting to :0"
    export DISPLAY=:0
fi
echo "DISPLAY is set to $DISPLAY"

echo "Setting up X11 permissions..."
xhost +local:docker >/dev/null 2>&1 || {
    echo "Warning: Failed to set X11 permissions with xhost +local:docker. Trying fallback..."
    xhost +si:localuser:root >/dev/null 2>&1 || {
        echo "Error: Failed to set X11 permissions. Ensure X11 is running and try 'xhost +'. Exiting."
        exit 1
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
    exit 1
fi