FROM openjdk:17-jdk-slim

WORKDIR /app

# Instala dependências e clona o projeto
RUN apt-get update \
  && apt-get install --no-install-recommends -y git maven \
  && apt-get clean \
  && git config --global user.name "Leonardo Soares" \
  && git config --global user.email "leonardo.soares@sptech.school.com.br" \
  && git clone https://github.com/projeto-fiap/video-manager-hackaton.git

WORKDIR /app/video-manager-hackaton

# Builda o projeto
RUN mvn clean install -DskipTests

# Copia o JAR para o diretório raiz
RUN cp target/video-manager-hackaton-0.0.1-SNAPSHOT.jar /app/

# Cria usuário não-root
RUN useradd -m nonroot
USER nonroot

EXPOSE 8081

# Roda o jar corretamente do local onde está
CMD ["java", "-jar", "/app/video-manager-hackaton-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prd"]