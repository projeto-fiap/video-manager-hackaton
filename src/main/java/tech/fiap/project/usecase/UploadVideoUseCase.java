package tech.fiap.project.usecase;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.fiap.project.config.AwsConfig;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.exceptions.MediaTypeException;
import tech.fiap.project.domain.exceptions.S3UploadException;
import tech.fiap.project.infra.KafkaStatusProducer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UploadVideoUseCase {

	private final AmazonS3 s3Client;

	private final String bucketName;

	private final KafkaStatusProducer producer;

	@Autowired
	public UploadVideoUseCase(AmazonS3 s3Client, AwsConfig awsConfig, KafkaStatusProducer producer) {
		this.s3Client = s3Client;
		this.bucketName = awsConfig.getBucketName();
		this.producer = producer;
	}

	public FileUploadResponse uploadFile(MultipartFile multipartFile) {
		String originalFilename = multipartFile.getOriginalFilename();
		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new MediaTypeException("Nome de arquivo inválido.",
					new IllegalArgumentException("Nome de arquivo inválido."));
		}

		try {
			return uploadFile(originalFilename, multipartFile.getBytes(), multipartFile.getContentType());
		}
		catch (IOException e) {
			throw new S3UploadException("Erro ao ler bytes do MultipartFile: " + e.getMessage(), e);
		}
	}

	public FileUploadResponse uploadFile(String originalFilename, String contentType, byte[] fileBytes) {
		return uploadFile(originalFilename, fileBytes, contentType);
	}

	private FileUploadResponse uploadFile(String originalFilename, byte[] fileBytes, String contentType) {
		FileUploadResponse fileUploadResponse = new FileUploadResponse();

		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new MediaTypeException("Nome de arquivo inválido.",
					new IllegalArgumentException("Nome de arquivo inválido."));
		}

		String fileExtension = getFileExtension(contentType);
		String videoFileName = originalFilename + "." + fileExtension;
		String filePath = "videos/" + videoFileName;

		try {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(fileBytes.length);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
			s3Client.putObject(bucketName, filePath, inputStream, objectMetadata);

			fileUploadResponse.setFilename(originalFilename);
			fileUploadResponse.setVideoFilename(videoFileName);
			fileUploadResponse.setDateTime(LocalDateTime.now());
		}
		catch (Exception e) {
			producer.error(originalFilename, "Erro ao realizar upload do vídeo");
			throw new S3UploadException("Erro ao fazer upload para o S3: " + e.getMessage(), e);
		}

		return fileUploadResponse;
	}

	String getFileExtension(String contentType) {
		if (contentType == null || !contentType.contains("/")) {
			throw new MediaTypeException("Content-Type inválido.",
					new IllegalArgumentException("Content-Type inválido."));
		}
		return contentType.substring(contentType.indexOf('/') + 1);
	}

	String getFilenameWithoutExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new MediaTypeException("Arquivo sem extensão inválido.",
					new IllegalArgumentException("Arquivo sem extensão inválido."));
		}
		return filename.substring(0, dotIndex);
	}

}
