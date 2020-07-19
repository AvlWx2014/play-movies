#! /bin/bash
docker container stop play-server && docker container rm play-server
docker build -t play:latest .
docker run -it --name play-server --publish 9000:9000 play:latest
