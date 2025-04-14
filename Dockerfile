FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update \
&& apt-get install --no-install-recommends -y build-essential nginx \
&& apt-get clean \
&& apt-get install --no-install-recommends -y build-essential git \
&& apt-get clean \
&& git config --global user.name "Leonardo Soares" \
&& git config --global user.email "leonardo.soares@sptech.school.com.br" \
&& git clone https://github.com/projeto-fiap/video-manager-hackaton.git
WORKDIR /app/tech-challenge-kitchen

RUN apt-get update \
&& apt-get install --no-install-recommends -y build-essential maven \
&& apt-get clean \
&& mvn clean install -DskipTests

RUN useradd -m nonroot

USER nonroot
EXPOSE 8080
CMD ["java", "-jar", "target/ms-kitchen-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prd"]