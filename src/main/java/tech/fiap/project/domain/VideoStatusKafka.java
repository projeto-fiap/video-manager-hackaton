package tech.fiap.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatusKafka {

	private String videoId;

	private String storage;

	private String downloadUrl;

	private String message;

	private VideoStatus status;

}
