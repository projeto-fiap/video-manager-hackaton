package tech.fiap.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaResponse {

	private String message;

	private String storage;

	private String videoDeleted;

}
