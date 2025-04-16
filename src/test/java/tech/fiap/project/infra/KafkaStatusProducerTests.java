package tech.fiap.project.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import tech.fiap.project.domain.VideoStatus;
import tech.fiap.project.domain.VideoStatusKafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
		VideoStatusKafka mensagem = new VideoStatusKafka("video123", "s3://path", VideoStatus.processed);

		// Act
		kafkaStatusProducer.sendStatus(mensagem);

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-upload-status"), eq(mensagem));
	}

	@Test
	void received_ShouldSendReceivedStatus() {
		// Act
		kafkaStatusProducer.received("video123");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-upload-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.received;
	}

	@Test
	void uploading_ShouldSendUploadingStatus() {
		// Act
		kafkaStatusProducer.uploading("video123");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-upload-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.uploading;
	}

	@Test
	void processing_ShouldSendProcessingStatus() {
		// Act
		kafkaStatusProducer.processing("video123");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-upload-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.processing;
	}

	@Test
	void processed_ShouldSendProcessedStatusWithStorage() {
		// Act
		kafkaStatusProducer.processed("video123", "s3://storage-url");

		// Assert
		verify(kafkaTemplate).send(eq("v1.video-upload-status"), statusCaptor.capture());
		VideoStatusKafka sent = statusCaptor.getValue();
		assert sent.getVideoId().equals("video123");
		assert sent.getStatus() == VideoStatus.processed;
		assert sent.getStorage().equals("s3://storage-url");
	}

}
