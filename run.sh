#! /bin/bash
mongod -f /etc/mongod.conf &
cd /opt/movies
sbt run