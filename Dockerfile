FROM adoptopenjdk:11-jdk-openj9
RUN apt-get update && apt-get install -y \ 
    curl \
    gnupg \
    systemd
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    curl -fsSL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt
EXPOSE 9000
RUN curl -fsSL https://www.mongodb.org/static/pgp/server-4.2.asc | apt-key add - && \
    echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.2 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.2.list && \
    apt-get update && \
    apt-get install -y mongodb-org
COPY . /opt/movies
RUN ["chmod", "+x", "/opt/movies/run.sh"]
ENTRYPOINT [ "/opt/movies/run.sh"] 