package tech.fiap.project.infra;

import com.amazonaws.util.IOUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tech.fiap.project.domain.VideoProducerDTO;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.FileProcessedResponse;
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
	public void consumirMensagem(VideoProducerDTO mensagem) {
		System.out.println("✅");
		System.out.println("🎥 Kafka - Vídeo recebido: " + mensagem.getFilename());
		producer.received(mensagem.getFilename());
		try {
			// Decode base64
			byte[] compressed = Base64.getDecoder().decode(mensagem.getData());

			// Decompress GZIP
			byte[] videoBytes = decompress(compressed);

			// Fazer upload para S3
			producer.uploading(mensagem.getFilename());
			FileUploadResponse uploadResult = uploadVideoUseCase.uploadFile(mensagem.getFilename(), videoBytes);

			// Processar vídeo com Lambda ou qualquer outro processo
			producer.processing(mensagem.getFilename());
			FileProcessedResponse processResult = processVideoUseCase
					.invokeLambdaFunction(uploadResult.getVideoFilename());

			producer.processed(mensagem.getFilename(), processResult.getStorage());
			System.out.println("✅ Vídeo processado com sucesso: " + processResult);

		}
		catch (Exception e) {
			System.err.println("❌ Erro ao processar vídeo do Kafka: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private byte[] decompress(byte[] compressedData) throws IOException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
				GZIPInputStream gis = new GZIPInputStream(bis)) {
			return IOUtils.toByteArray(gis);
		}
	}

}
