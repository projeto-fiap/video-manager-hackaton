package tech.fiap.project.infra;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tech.fiap.project.domain.VideoStatusKafka;

@Service
public class KafkaStatusProducer {

	private final KafkaTemplate<String, VideoStatusKafka> kafkaTemplate;

	private final String groupName = "video-service";

	private final String topicName = "v1.video-upload-status";

	public KafkaStatusProducer(KafkaTemplate<String, VideoStatusKafka> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void enviarMensagem(VideoStatusKafka mensagem) {
		kafkaTemplate.send(topicName, mensagem);
		System.out.println("Mensagem enviada para o tópico " + topicName + ": " + mensagem);
	}

}
