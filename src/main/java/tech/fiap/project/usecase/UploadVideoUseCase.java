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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UploadVideoUseCase {

	private final AmazonS3 s3Client;

	private final String bucketName;

	@Autowired
	public UploadVideoUseCase(AmazonS3 s3Client, AwsConfig awsConfig) {
		this.s3Client = s3Client;
		this.bucketName = awsConfig.getBucketName();
	}

	// ✅ Método original (MultipartFile)
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

	// ✅ Novo método para Kafka (byte[] + filename)
	public FileUploadResponse uploadFile(String originalFilename, byte[] fileBytes) {
		return uploadFile(originalFilename, fileBytes, "video/mp4"); // default content
																		// type
	}

	// ✅ Método privado comum aos dois
	private FileUploadResponse uploadFile(String originalFilename, byte[] fileBytes, String contentType) {
		FileUploadResponse fileUploadResponse = new FileUploadResponse();

		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new MediaTypeException("Nome de arquivo inválido.",
					new IllegalArgumentException("Nome de arquivo inválido."));
		}

		String randomUUID = getFilenameWithoutExtension(originalFilename);
		String fileExtension = getFileExtension(originalFilename);
		String videoFileName = randomUUID + "." + fileExtension;
		String filePath = "videos/" + videoFileName;

		try {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(fileBytes.length);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
			s3Client.putObject(bucketName, filePath, inputStream, objectMetadata);

			fileUploadResponse.setFilename(randomUUID);
			fileUploadResponse.setVideoFilename(videoFileName);
			fileUploadResponse.setDateTime(LocalDateTime.now());
		}
		catch (Exception e) {
			throw new S3UploadException("Erro ao fazer upload para o S3: " + e.getMessage(), e);
		}

		return fileUploadResponse;
	}

	private String getFileExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new MediaTypeException("Extensão de arquivo inválida.",
					new IllegalArgumentException("Extensão de arquivo inválida."));
		}
		return filename.substring(dotIndex + 1);
	}

	private String getFilenameWithoutExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new MediaTypeException("Arquivo sem extensão inválido.",
					new IllegalArgumentException("Arquivo sem extensão inválido."));
		}
		return filename.substring(0, dotIndex);
	}

}
