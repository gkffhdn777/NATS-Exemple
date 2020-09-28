package com.example;

import com.example.nats.subscriber.NatsSubscriber;
import com.example.stream.subscriber.NatsStreamSubscriber;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * 구독 하는 방법에는 nats server, nats stream 두가지 방식이 있으며 한서버에서 혼합 해서 사용할수는 없다.
 * nats server 최대 한번 전달을 사용하며 요청-응답 모델을 사용할수 있다.
 * nats stream 최소 한번 전달을 사용하며 발행-구독 모델을 사용하며, 메시지 유실을 허용하지 않도록 하지만 중복은 발생한다.
 */
@RequiredArgsConstructor
@SpringBootApplication
public class NatsApplication {

	private final NatsSubscriber natsSubscriber;

	private final NatsStreamSubscriber natsStreamSubscriber;

	private static final String GROUP_ID = "findByIdGroup";

	public static void main(String[] args) {
		SpringApplication.run(NatsApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void natsReady() {
		//nats();
		natsStreaming();
	}

	/**
	 * nats server
	 */
	private void nats() {
		natsSubscriber.dispatchQueue(GROUP_ID);
		natsSubscriber.save();
		natsSubscriber.findById();
		natsSubscriber.saveLog();
	}

	/**
	 * nats streaming
	 */
	private void natsStreaming() {
		natsStreamSubscriber.save();
		natsStreamSubscriber.queueConsumer();
	}
}
