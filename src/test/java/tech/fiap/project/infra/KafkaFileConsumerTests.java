package tech.fiap.project.infra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.VideoProducerDTO;
import tech.fiap.project.usecase.ProcessVideoUseCase;
import tech.fiap.project.usecase.UploadVideoUseCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaFileConsumerTests {

	@Mock
	private UploadVideoUseCase uploadVideoUseCase;

	@Mock
	private ProcessVideoUseCase processVideoUseCase;

	@Mock
	private KafkaStatusProducer kafkaStatusProducer;

	@InjectMocks
	private KafkaFileConsumer kafkaFileConsumer;

	private byte[] compress(String str) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
			gzip.write(str.getBytes());
		}
		return bos.toByteArray();
	}

	@Test
	void consumirMensagem_ShouldProcessVideoSuccessfully() throws Exception {
		// Arrange
		String videoContent = "mock-video-bytes";
		byte[] compressedBytes = compress(videoContent);
		String base64Compressed = Base64.getEncoder().encodeToString(compressedBytes);

		VideoProducerDTO dto = new VideoProducerDTO("video.mp4", base64Compressed);

		FileUploadResponse uploadResponse = new FileUploadResponse();
		uploadResponse.setFilename("video.mp4");
		uploadResponse.setVideoFilename("video.mp4");
		uploadResponse.setDateTime(LocalDateTime.now());

		FileProcessedResponse processedResponse = new FileProcessedResponse();
		processedResponse.setFilename("video.mp4");
		processedResponse.setZipFilename("video.zip");
		processedResponse.setStorage("s3://processed/path");
		processedResponse.setDownloadUrl("s3://processed/download/path");
		processedResponse.setDateTime(LocalDateTime.now());

		when(uploadVideoUseCase.uploadFile(eq("video.mp4"), any())).thenReturn(uploadResponse);
		when(processVideoUseCase.invokeLambdaFunction("video.mp4")).thenReturn(processedResponse);

		// Act
		kafkaFileConsumer.consumirMensagem(dto);

		// Assert
		verify(kafkaStatusProducer).received("video.mp4");
		verify(kafkaStatusProducer).uploading("video.mp4");
		verify(kafkaStatusProducer).processing("video.mp4");
		verify(kafkaStatusProducer).processed("video.mp4", "s3://processed/path", "s3://processed/download/path");

		verify(uploadVideoUseCase).uploadFile(eq("video.mp4"), any());
		verify(processVideoUseCase).invokeLambdaFunction("video.mp4");
	}

	@Test
	void consumirMensagem_ShouldHandleExceptionGracefully() {
		// Arrange
		String invalidBase64 = "!!!not-valid-base64===";
		VideoProducerDTO dto = new VideoProducerDTO("video.mp4", invalidBase64);

		// Act
		kafkaFileConsumer.consumirMensagem(dto);

		// Assert
		verify(kafkaStatusProducer).received("video.mp4");
		verify(kafkaStatusProducer, never()).uploading(any());
		verify(kafkaStatusProducer, never()).processing(any());
		verify(kafkaStatusProducer, never()).processed(any(), any(), any());
	}

}
