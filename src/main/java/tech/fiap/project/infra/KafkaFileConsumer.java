package tech.fiap.project.infra;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tech.fiap.project.domain.VideoProducerDTO;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.exceptions.LambdaInvokeException;
import tech.fiap.project.domain.exceptions.S3UploadException;
import tech.fiap.project.usecase.ProcessVideoUseCase;
import tech.fiap.project.usecase.UploadVideoUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

@Service
public class KafkaFileConsumer {

	private final UploadVideoUseCase uploadVideoUseCase;

	private final ProcessVideoUseCase processVideoUseCase;

	private final KafkaStatusProducer producer;

	public KafkaFileConsumer(UploadVideoUseCase uploadVideoUseCase, ProcessVideoUseCase processVideoUseCase,
			KafkaStatusProducer producer) {
		this.uploadVideoUseCase = uploadVideoUseCase;
		this.processVideoUseCase = processVideoUseCase;
		this.producer = producer;
	}

	@KafkaListener(topics = "v1.video-upload-content", groupId = "video-service")
	public void consumirMensagem(String mensagem) {
		System.out.println("✅");
		try {
			VideoProducerDTO object = new ObjectMapper().readValue(mensagem, VideoProducerDTO.class);
			System.out.println("🎥 Kafka - Vídeo recebido: " + object.getFilename());

			// Decode base64
			byte[] compressed = Base64.getDecoder().decode(object.getData());

			// Decompress GZIP
			byte[] videoBytes = decompress(object.getFilename(), compressed);

			FileUploadResponse uploadResult = uploadVideoUseCase.uploadFile(object.getFilename(),
					object.getContentType(), videoBytes);

			FileProcessedResponse processResult = processVideoUseCase
					.invokeLambdaFunction(uploadResult.getVideoFilename());

			producer.success(object.getFilename(), processResult.getStorage(), processResult.getDownloadUrl());
		}
		catch (Exception e) {
			System.err.println("❌ Erro inesperado: " + e.getMessage());
			producer.error("Erro inesperado", "Formato de vídeo inválido");
		}
	}

	private byte[] decompress(String filename, byte[] compressedData) throws IOException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
				GZIPInputStream gis = new GZIPInputStream(bis)) {
			return IOUtils.toByteArray(gis);
		}
	}

}
