package tech.fiap.project.infra;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tech.fiap.project.domain.VideoStatus;
import tech.fiap.project.domain.VideoStatusKafka;

@Service
public class KafkaStatusProducer {

	private final KafkaTemplate<String, VideoStatusKafka> kafkaTemplate;

	private final String groupName = "video-service";

	private final String topicName = "v1.video-status";

	public KafkaStatusProducer(KafkaTemplate<String, VideoStatusKafka> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendStatus(VideoStatusKafka mensagem) {
		kafkaTemplate.send(topicName, mensagem);
		System.out.println("🎥 Mensagem enviada para o tópico " + topicName + ": " + mensagem);
	}

	public void error(String videoId, String message) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.ERRO);
		status.setVideoId(videoId);
		sendStatus(status);
	}

	public void success(String videoId, String storage, String downloadUrl) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.FINALIZADO);
		status.setVideoId(videoId);
		status.setStorage(storage);
		sendStatus(status);
	}

}
