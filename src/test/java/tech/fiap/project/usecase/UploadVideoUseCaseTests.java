package tech.fiap.project.usecase;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import tech.fiap.project.config.AwsConfig;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.exceptions.MediaTypeException;
import tech.fiap.project.domain.exceptions.S3UploadException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadVideoUseCaseTests {

	@Mock
	private AmazonS3 s3Client;

	@Mock
	private AwsConfig awsConfig;

	@InjectMocks
	private UploadVideoUseCase uploadVideoUseCase;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(awsConfig.getBucketName()).thenReturn("my-bucket");
	}

	@Test
	void uploadFile_ShouldReturnFileUploadResponse_WhenValidMultipartFile() throws IOException {
		// Arrange
		String originalFilename = "video";
		MockMultipartFile multipartFile = new MockMultipartFile("file", originalFilename, "video/mp4", new byte[10]);

		FileUploadResponse expectedResponse = new FileUploadResponse();
		expectedResponse.setFilename("video");
		expectedResponse.setVideoFilename("video.mp4");

		when(s3Client.putObject(anyString(), anyString(), any(), any(ObjectMetadata.class))).thenReturn(null);

		// Act
		FileUploadResponse actualResponse = uploadVideoUseCase.uploadFile(multipartFile);

		// Assert
		assertNotNull(actualResponse);
		assertEquals(expectedResponse.getFilename(), actualResponse.getFilename());
		assertEquals(expectedResponse.getVideoFilename(), actualResponse.getVideoFilename());
	}

	@Test
	void uploadFile_ShouldThrowMediaTypeException_WhenFilenameIsNull() {
		// Arrange
		MockMultipartFile multipartFile = new MockMultipartFile("file", null, "video/mp4", new byte[10]);

		// Act & Assert
		assertThrows(MediaTypeException.class, () -> uploadVideoUseCase.uploadFile(multipartFile));
	}

	@Test
	void uploadFile_ShouldReturnFileUploadResponse_WhenValidByteArrayAndFilename() {
		// Arrange
		String filename = "video";
		String contentType = "video/mp4";
		byte[] fileBytes = new byte[10];
		FileUploadResponse expectedResponse = new FileUploadResponse();
		expectedResponse.setFilename("video");
		expectedResponse.setVideoFilename("video.mp4");

		when(s3Client.putObject(anyString(), anyString(), any(), any(ObjectMetadata.class))).thenReturn(null);

		// Act
		FileUploadResponse actualResponse = uploadVideoUseCase.uploadFile(filename, contentType, fileBytes);

		// Assert
		assertNotNull(actualResponse);
		assertEquals(expectedResponse.getFilename(), actualResponse.getFilename());
		assertEquals(expectedResponse.getVideoFilename(), actualResponse.getVideoFilename());
	}

	@Test
	void uploadFile_ShouldThrowMediaTypeException_WhenNoExtensionInFilename() {
		// Arrange
		String filename = "video";
		byte[] fileBytes = new byte[10];
		String contentType = "videomp4";

		// Act & Assert
		assertThrows(MediaTypeException.class, () -> uploadVideoUseCase.uploadFile(filename, contentType, fileBytes));
	}

	@Test
	void getFileExtension_ShouldThrowMediaTypeException_WhenNoExtensionInFilename() {
		// Arrange
		String filename = "video";

		// Act & Assert
		assertThrows(MediaTypeException.class, () -> uploadVideoUseCase.getFileExtension(filename));
	}

	@Test
	void getFilenameWithoutExtension_ShouldThrowMediaTypeException_WhenNoExtensionInFilename() {
		// Arrange
		String filename = "video";

		// Act & Assert
		assertThrows(MediaTypeException.class, () -> uploadVideoUseCase.getFilenameWithoutExtension(filename));
	}

}
