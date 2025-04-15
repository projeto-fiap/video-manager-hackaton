package tech.fiap.project.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.exceptions.S3UploadException;
import tech.fiap.project.usecase.ProcessVideoUseCase;
import tech.fiap.project.usecase.UploadVideoUseCase;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTests {

	@Mock
	private UploadVideoUseCase uploadVideoUseCase;

	@Mock
	private ProcessVideoUseCase processVideoUseCase;

	@InjectMocks
	private VideoController videoController;

	@Test
	void process_ShouldReturnProcessedResponse_WhenSuccessful() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("filename", "filename.mp4", "video/mp4", "test content".getBytes());
		FileUploadResponse uploadResponse = new FileUploadResponse();
		uploadResponse.setFilename("filename");
		uploadResponse.setDateTime(LocalDateTime.now());
		uploadResponse.setVideoFilename("videofilename");

		FileProcessedResponse processedResponse = new FileProcessedResponse();
		processedResponse.setStorage("storage://filename.zip");
		processedResponse.setZipFilename("filename.zip");
		processedResponse.setDateTime(LocalDateTime.now());
		processedResponse.setFilename("filename");

		when(uploadVideoUseCase.uploadFile(file)).thenReturn(uploadResponse);
		when(processVideoUseCase.invokeLambdaFunction("videofilename")).thenReturn(processedResponse);

		// Act
		ResponseEntity<?> response = videoController.process(file);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(processedResponse, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
		verify(processVideoUseCase, times(1)).invokeLambdaFunction("videofilename");
	}

	@Test
	void process_ShouldReturnBadRequest_WhenS3UploadExceptionThrown() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("video", "video.mp4", "video/mp4", "test content".getBytes());
		S3UploadException exception = new S3UploadException("Upload failed",
				new IllegalArgumentException("Upload failed"));
		when(uploadVideoUseCase.uploadFile(file)).thenThrow(exception);

		// Act
		ResponseEntity<?> response = videoController.process(file);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(exception, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
		verify(processVideoUseCase, never()).invokeLambdaFunction(any());
	}

	@Test
	void process_ShouldReturnInternalServerError_WhenUnexpectedExceptionThrown() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("video", "video.mp4", "video/mp4", "test content".getBytes());
		Exception exception = new RuntimeException("Unexpected error");
		when(uploadVideoUseCase.uploadFile(file)).thenThrow(exception);

		// Act
		ResponseEntity<?> response = videoController.process(file);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals(exception, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
	}

	@Test
	void upload_ShouldReturnUploadResponse_WhenSuccessful() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("filename", "filename.mp4", "video/mp4", "test content".getBytes());
		FileUploadResponse uploadResponse = new FileUploadResponse();
		uploadResponse.setFilename("filename");
		uploadResponse.setDateTime(LocalDateTime.now());
		uploadResponse.setVideoFilename("videofilename");

		when(uploadVideoUseCase.uploadFile(file)).thenReturn(uploadResponse);

		// Act
		ResponseEntity<?> response = videoController.upload(file);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(uploadResponse, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
		verify(processVideoUseCase, times(1)).invokeAsyncLambdaFunction("videofilename");
	}

	@Test
	void upload_ShouldReturnBadRequest_WhenS3UploadExceptionThrown() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("video", "video.mp4", "video/mp4", "test content".getBytes());
		S3UploadException exception = new S3UploadException("Upload failed",
				new IllegalArgumentException("Upload failed"));
		when(uploadVideoUseCase.uploadFile(file)).thenThrow(exception);

		// Act
		ResponseEntity<?> response = videoController.upload(file);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(exception, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
		verify(processVideoUseCase, never()).invokeAsyncLambdaFunction(any());
	}

	@Test
	void upload_ShouldReturnInternalServerError_WhenUnexpectedExceptionThrown() throws Exception {
		// Arrange
		MultipartFile file = new MockMultipartFile("video", "video.mp4", "video/mp4", "test content".getBytes());
		Exception exception = new RuntimeException("Unexpected error");
		when(uploadVideoUseCase.uploadFile(file)).thenThrow(exception);

		// Act
		ResponseEntity<?> response = videoController.upload(file);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals(exception, response.getBody());
		verify(uploadVideoUseCase, times(1)).uploadFile(file);
		verify(processVideoUseCase, never()).invokeAsyncLambdaFunction(any());
	}

}
