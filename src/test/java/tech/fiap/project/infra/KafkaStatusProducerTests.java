package tech.fiap.project.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import tech.fiap.project.domain.VideoStatus;
import tech.fiap.project.domain.VideoStatusKafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaStatusProducerTests {

	@Mock
	private KafkaTemplate<String, VideoStatusKafka> kafkaTemplate;

	@InjectMocks
	private KafkaStatusProducer kafkaStatusProducer;

	@Captor
	private ArgumentCaptor<VideoStatusKafka> statusCaptor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void sendStatus_ShouldSendToKafka() {
		// Arrange
		VideoStatusKafka mensagem = new VideoStatusKafka("video123", "s3://path", "s3://storage-url", "message",
				VideoStatus.FINALIZADO);

		// Act
		kafkaStatusProducer.sendStatus(mensagem);

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-status"), eq(mensagem));
	}

	@Test
	void error_ShouldSendErrorStatus() {
		// Act
		kafkaStatusProducer.error("video123", "mensagem");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.ERRO;
	}

	@Test
	void success_ShouldSendFinalizadoStatusWithStorage() {
		// Act
		kafkaStatusProducer.success("video123", "s3://storage-url", "s3://storage-url");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.FINALIZADO;
		assert sent.getStorage().equals("s3://storage-url");
	}

}
