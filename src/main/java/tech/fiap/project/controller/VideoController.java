package tech.fiap.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.fiap.project.domain.FileProcessedResponse;
import tech.fiap.project.domain.FileUploadResponse;
import tech.fiap.project.domain.exceptions.S3UploadException;
import tech.fiap.project.usecase.ProcessVideoUseCase;
import tech.fiap.project.usecase.UploadVideoUseCase;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

	private final UploadVideoUseCase uploadVideoUseCase;

	private final ProcessVideoUseCase processVideoUseCase;

	@PostMapping("/sync/upload")
	@ExceptionHandler(S3UploadException.class)
	public ResponseEntity<?> process(@RequestParam("file") MultipartFile file) {
		try {
			FileUploadResponse result = uploadVideoUseCase.uploadFile(file);

			FileProcessedResponse lambdaResult = processVideoUseCase.invokeLambdaFunction(result.getVideoFilename());

			return ResponseEntity.ok(lambdaResult);
		}
		catch (S3UploadException e) {
			return ResponseEntity.badRequest().body(e);
		}
		catch (Exception e) {
			return ResponseEntity.internalServerError().body(e);
		}
	}

	@PostMapping("/async/upload")
	@ExceptionHandler(S3UploadException.class)
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
		try {
			FileUploadResponse result = uploadVideoUseCase.uploadFile(file);

			processVideoUseCase.invokeAsyncLambdaFunction(result.getVideoFilename());

			return ResponseEntity.ok(result);
		}
		catch (S3UploadException e) {
			return ResponseEntity.badRequest().body(e);
		}
		catch (Exception e) {
			return ResponseEntity.internalServerError().body(e);
		}
	}

}
