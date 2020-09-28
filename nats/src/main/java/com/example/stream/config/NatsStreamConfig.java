package com.example.stream.config;

import java.io.IOException;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass({ Connection.class })
public class NatsStreamConfig {

	private static final String CLUSTER_ID = "test-cluster";

	//클라이언트 아이디는 고유 해야한다 그이유는 재 연결시 해당 값으로 식별하여 이벤트를 정기 구독 한다.
	private static final String CLIENT_ID = "client-sub-1";

	@Bean
	@ConditionalOnMissingBean
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
		builder.natsConn(connection).clusterId(CLUSTER_ID).clientId(CLIENT_ID).connectWait(Duration.ofSeconds(3)).traceConnection();
		StreamingConnectionFactory factory = new StreamingConnectionFactory(builder.build());
		return factory.createConnection();
	}
}
