package com.example.nats.config;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NatsConfig implements AutoCloseable {

	private final Connection connection;

	private final StreamingConnection streamingConnection;

	@PreDestroy
	public void closeNatsStreaming() throws InterruptedException, TimeoutException, IOException {
		// nats, nats streaming 혼합 하여 사용 하였기 떄문에 먼저 close 시켜 준다.
		streamingConnection.close();
	}

	/**
	 * 메시지가 전부 소비 될떄 까지 서버 종료를 지연시킨다.
	 * @throws Exception
	 */
	@Override
	public void close() throws Exception {
		Mono.fromFuture(connection.drain(Duration.ofSeconds(0))).block();
	}
}
