package com.example.nats.client.publisher;

import java.util.concurrent.TimeUnit;

import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsPublisher {

	private final Connection connection;

	public Mono<Boolean> publish(final String subject, final String message) {
		try {
			this.connection.publish(subject, message.getBytes());
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return Mono.just(Boolean.FALSE);
		}
		return Mono.just(Boolean.TRUE);
	}

	public Mono<Message> request(final String subject, final String id) {
		return Mono.fromFuture(() -> this.connection.request(subject, id.getBytes()).orTimeout(2, TimeUnit.SECONDS))
				.onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())));
	}
}
