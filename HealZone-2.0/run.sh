
#!/bin/bash
echo "Building Docker image for HealZone..."
docker build -t healzone .
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