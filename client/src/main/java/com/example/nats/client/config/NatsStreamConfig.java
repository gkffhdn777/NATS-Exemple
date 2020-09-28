package com.example.nats.client.config;

import java.io.IOException;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NatsStreamConfig {

	private static final String CLUSTER_ID = "test-cluster";

	private static final String CLIENT_ID = "client-pub-1";

	@Bean
	public StreamingConnection streamingConnection(final Connection connection) throws IOException, InterruptedException {
		Options.Builder builder = new Options.Builder();
		builder.connectionListener((conn, type) -> log.info("NATS-Stream connection status changed " + type));
		builder.errorListener(new ErrorListener() {
			@Override
			public void slowConsumerDetected(Connection conn, Consumer consumer) {
				log.info("NATS-Stream connection slow consumer detected");
			}

			@Override
			public void exceptionOccurred(Connection conn, Exception exp) {
				log.info("NATS-Stream connection exception occurred", exp);
			}

			@Override
			public void errorOccurred(Connection conn, String error) {
				log.info("NATS-Stream connection error occurred " + error);
			}
		});
		builder.natsConn(connection).clusterId(CLUSTER_ID).clientId(CLIENT_ID);
		builder.traceConnection();
		// 메시지 승인 타입아웃
		builder.pubAckWait(Duration.ofSeconds(3));
		//보낼 는 메시를 제한 하며 ack 승인 여부에 달려있다.
		//ex) 처리 응답을 받지 못한다면 최대 100개 까지 메시지만 보낼수 있다.
		builder.maxPubAcksInFlight(100);
		StreamingConnectionFactory factory = new StreamingConnectionFactory(builder.build());
		return factory.createConnection();
	}
}
