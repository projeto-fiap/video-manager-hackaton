package tech.fiap.project.usecase;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.fiap.project.config.AwsConfig;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.LambdaResponse;
import tech.fiap.project.domain.exceptions.LambdaInvokeException;
import tech.fiap.project.domain.exceptions.MediaTypeException;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessVideoUseCaseTests {

	private AWSLambda awsLambda;

	private AwsConfig awsConfig;

	private ProcessVideoUseCase processVideoUseCase;

	@BeforeEach
	void setUp() {
		awsLambda = mock(AWSLambda.class);
		awsConfig = mock(AwsConfig.class);
		when(awsConfig.getLambdaFunction()).thenReturn("test-lambda");
		processVideoUseCase = new ProcessVideoUseCase(awsLambda, awsConfig, mock());
	}

	@Test
	void invokeLambdaFunction_ShouldReturnProcessedResponse() throws Exception {
		// Arrange
		String filename = "video.mp4";
		LambdaResponse lambdaResponse = new LambdaResponse("OK", "s3://bucket/path", "s3://bucket/download/path",
				"false");

		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		String payloadJson = mapper.writeValueAsString(lambdaResponse);
		InvokeResult invokeResult = new InvokeResult().withPayload(ByteBuffer.wrap(payloadJson.getBytes()));

		when(awsLambda.invoke(any(InvokeRequest.class))).thenReturn(invokeResult);

		// Act
		FileProcessedResponse response = processVideoUseCase.invokeLambdaFunction(filename);

		// Assert
		assertEquals("video", response.getFilename());
		assertEquals("video.zip", response.getZipFilename());
		assertEquals("s3://bucket/path", response.getStorage());
		assertEquals("s3://bucket/download/path", response.getDownloadUrl());
		assertNotNull(response.getDateTime());
	}

	@Test
	void invokeLambdaFunction_ShouldThrowLambdaInvokeException_WhenFileNameIsNull() {
		// Act & Assert
		assertThrows(LambdaInvokeException.class, () -> processVideoUseCase.invokeLambdaFunction(null));
	}

	@Test
	void invokeLambdaFunction_ShouldThrowLambdaInvokeException_WhenLambdaFails() {
		// Arrange
		String filename = "video.mp4";
		when(awsLambda.invoke(any(InvokeRequest.class))).thenThrow(new RuntimeException("Lambda error"));

		// Act & Assert
		LambdaInvokeException exception = assertThrows(LambdaInvokeException.class,
				() -> processVideoUseCase.invokeLambdaFunction(filename));
		assertTrue(exception.getMessage().contains("Erro ao invocar a Lambda"));
	}

	@Test
	void changeFileExtensionToZip_ShouldThrowMediaTypeException_WhenNoExtension() throws Exception {
		var method = ProcessVideoUseCase.class.getDeclaredMethod("changeFileExtensionToZip", String.class);
		method.setAccessible(true);

		try {
			method.invoke(processVideoUseCase, "video");
			fail("Deveria lançar MediaTypeException");
		}
		catch (InvocationTargetException e) {
			assertTrue(e.getCause() instanceof MediaTypeException);
			assertEquals("Extensão de arquivo inválida.", e.getCause().getMessage());
		}
	}

	@Test
	void getFilenameWithoutExtension_ShouldThrowMediaTypeException_WhenNoDotInFilename() throws Exception {
		var method = ProcessVideoUseCase.class.getDeclaredMethod("getFilenameWithoutExtension", String.class);
		method.setAccessible(true);

		try {
			method.invoke(processVideoUseCase, "video");
			fail("Deveria lançar MediaTypeException");
		}
		catch (InvocationTargetException e) {
			assertTrue(e.getCause() instanceof MediaTypeException);
			assertEquals("Arquivo sem extensão inválido.", e.getCause().getMessage());
		}
	}

}
