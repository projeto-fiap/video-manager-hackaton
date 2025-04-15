package tech.fiap.project.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.fiap.project.domain.VideoStatusKafka;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaVideoStatusProducerConfig {
	@Value("${kafka.server}")
	private String sever;

	@Bean
	public ProducerFactory<String, VideoStatusKafka> videoStatusProducerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sever);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 200_000_000);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, VideoStatusKafka> videoStatusKafkaTemplate() {
		return new KafkaTemplate<>(videoStatusProducerFactory());
	}

}
