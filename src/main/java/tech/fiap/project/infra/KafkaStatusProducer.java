package tech.fiap.project.infra;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tech.fiap.project.domain.VideoStatus;
import tech.fiap.project.domain.VideoStatusKafka;

@Service
public class KafkaStatusProducer {

	private final KafkaTemplate<String, VideoStatusKafka> kafkaTemplate;

	private final String groupName = "video-service";

	private final String topicName = "v1.video-upload-status";

	public KafkaStatusProducer(KafkaTemplate<String, VideoStatusKafka> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendStatus(VideoStatusKafka mensagem) {
		kafkaTemplate.send(topicName, mensagem);
		System.out.println("🎥 Mensagem enviada para o tópico " + topicName + ": " + mensagem);
	}

	public void received(String videoId) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.received);
		status.setVideoId(videoId);
		sendStatus(status);
	}

	public void uploading(String videoId) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.uploading);
		status.setVideoId(videoId);
		sendStatus(status);
	}

	public void processing(String videoId) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.processing);
		status.setVideoId(videoId);
		sendStatus(status);
	}

	public void processed(String videoId, String storage, String downloadUrl) {
		VideoStatusKafka status = new VideoStatusKafka();
		status.setStatus(VideoStatus.processed);
		status.setVideoId(videoId);
		status.setStorage(storage);
		status.setDownloadUrl(downloadUrl);
		sendStatus(status);
	}

}
