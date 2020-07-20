FROM adoptopenjdk:11-jdk-openj9
RUN apt-get update && apt-get install -y \ 
    curl \
    gnupg
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    curl -fsSL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt
COPY . /opt/movies
WORKDIR /opt/movies
CMD [ "sbt", "clean", "compile", "run" ]