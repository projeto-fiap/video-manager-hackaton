package tech.fiap.project.usecase;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.fiap.project.config.AwsConfig;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.LambdaRequest;
import tech.fiap.project.domain.LambdaResponse;
import tech.fiap.project.domain.exceptions.LambdaInvokeException;
import tech.fiap.project.domain.exceptions.MediaTypeException;
import tech.fiap.project.infra.KafkaStatusProducer;

@Service
public class ProcessVideoUseCase {

	private final AWSLambda awsLambdaClient;

	private final String lambdaFunctionName;

	private final KafkaStatusProducer producer;

	@Autowired
	public ProcessVideoUseCase(AWSLambda awsLambdaClient, AwsConfig awsConfig, KafkaStatusProducer producer) {
		this.awsLambdaClient = awsLambdaClient;
		this.lambdaFunctionName = awsConfig.getLambdaFunction();
		this.producer = producer;
	}

	public void invokeAsyncLambdaFunction(String fileName) {
		new Thread(new Runnable() {
			public void run() {
				FileProcessedResponse result = invokeLambdaFunction(fileName);
			}
		}).start();
	}

	public FileProcessedResponse invokeLambdaFunction(String fileName) {
		FileProcessedResponse fileUploadResponse = new FileProcessedResponse();

		if (fileName == null || fileName.isEmpty()) {
			throw new LambdaInvokeException("Nome do arquivo inválido.",
					new IllegalArgumentException("Nome do arquivo inválido."));
		}

		try {
			LambdaRequest lambdaRequest = new LambdaRequest();
			lambdaRequest.setFilename(fileName);

			ObjectMapper objectMapper = new ObjectMapper();
			String payload = objectMapper.writeValueAsString(lambdaRequest);

			InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(lambdaFunctionName).withPayload(payload);

			InvokeResult invokeResult = awsLambdaClient.invoke(invokeRequest);

			String result = new String(invokeResult.getPayload().array());

			try {
				objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
				LambdaResponse lambdaResponse = objectMapper.readValue(result, LambdaResponse.class);
				fileUploadResponse.setStorage(lambdaResponse.getStorage());
			}
			catch (Exception ignored) {
			}

			String zipName = changeFileExtensionToZip(fileName);
			String originalFilename = getFilenameWithoutExtension(fileName);
			fileUploadResponse.setFilename(originalFilename);
			fileUploadResponse.setZipFilename(zipName);
			fileUploadResponse.setDateTime(java.time.LocalDateTime.now());
		}
		catch (Exception e) {
			producer.error(fileName, "Erro ao processar o vídeo");
			throw new LambdaInvokeException("Erro ao invocar a Lambda: " + e.getMessage(), e);
		}

		return fileUploadResponse;
	}

	private String getFilenameWithoutExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new MediaTypeException("Arquivo sem extensão inválido.",
					new IllegalArgumentException("Arquivo sem extensão inválido."));
		}
		return filename.substring(0, dotIndex);
	}

	private String changeFileExtensionToZip(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new MediaTypeException("Extensão de arquivo inválida.",
					new IllegalArgumentException("Extensão de arquivo inválida."));
		}
		return filename.substring(0, dotIndex) + ".zip";
	}

}
