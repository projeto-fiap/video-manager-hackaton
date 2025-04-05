## 🐳 1. Subir Kafka com Docker Compose

```bash
docker-compose up -d
```

---

## 📦 2. Acessar o container do Kafka

```bash
docker exec -it kafka bash
```

---

## 📡 3. Verificar tópicos existentes

```bash
kafka-topics --list --bootstrap-server kafka:9092
```

---

## 🛠️ 4. Criar tópico (se necessário)

```bash
kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic v1.video-upload-content
```

---

---

## 📤 5. Enviar mensagem manual (teste simples)

```bash
kafka-console-producer \
  --broker-list kafka:9092 \
  --topic v1.video-upload-content
```

Cole a mensagem JSON e pressione Enter:
```json
{"name": "teste", "data": "aGVsbG8="}
```

---

## 📝 6. Acompanhar os logs da aplicação (Spring Boot)

```bash
docker logs -f video-manager-app
```

---

## 🤖 Execução automática com Python

### Abrir o projeto Python


### Enviar vídeo para o Kafka

```bash
python send_video_kafka.py videoplayback1.mp4
```

---
