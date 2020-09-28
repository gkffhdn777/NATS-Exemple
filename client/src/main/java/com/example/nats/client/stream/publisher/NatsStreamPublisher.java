package com.example.nats.client.stream.publisher;

import io.nats.streaming.AckHandler;
import io.nats.streaming.StreamingConnection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class NatsStreamPublisher {

	private final StreamingConnection connection;

	private static final AckHandler ackHandler = (guid, err) -> {
		if (err != null) {
			log.error("Error publishing msg : {}, id : {}", guid, err.getMessage());
		} else {
			log.info("Success publishing msgId : {}", guid);
		}
	};

	public Mono<String> publish(final String subject, final String message) {
		try {
			return Mono.fromCallable(() -> this.connection.publish(subject, message.getBytes(), ackHandler));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
