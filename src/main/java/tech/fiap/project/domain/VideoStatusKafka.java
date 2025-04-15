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

	private VideoStatus status;

}
