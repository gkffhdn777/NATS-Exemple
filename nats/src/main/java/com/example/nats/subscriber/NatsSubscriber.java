package com.example.nats.subscriber;

import java.time.Duration;

import com.example.nats.domain.Event;
import com.example.nats.repository.NatsRepository;
import com.google.gson.Gson;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsSubscriber {

	private final Connection connection;

	private final Gson gson;

	private final NatsRepository natsRepository;

	private static final String SAVE = "save";

	private static final String FIND_ID = "findById";

	public void save() {
		this.connection.createDispatcher(msg -> {
			try {
				var event = gson.fromJson(new String(msg.getData()), Event.class);
				natsRepository.save(event).subscribe();
				log.info("Nats save : {}", gson.fromJson(new String(msg.getData()), Event.class));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage());
			}
		}).subscribe(SAVE);
	}

	public void findById() {
		this.connection.createDispatcher(msg -> {
			try {
				var id = gson.fromJson(new String(msg.getData()), Long.class);
				this.natsRepository.findById(id)
						.flatMap(s -> {
							this.connection.publish(msg.getReplyTo(), gson.toJson(new Event(id, s)).getBytes());
							return Mono.just(s);
						}).subscribe();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}).subscribe(FIND_ID);
	}

	public void saveLog() {
		Subscription subscription = connection.subscribe(SAVE);
		try {
			//동기 방식으로 데이터를 가져온다.
			Message message = subscription.nextMessage(Duration.ofSeconds(0));
			if (message == null) {
				return;
			}
			message.getSubject();
			message.getSID();
			message.getReplyTo();
			message.getData();
			message.getConnection().getConnectedUrl();
			log.info("Monitoring save action -> subject : {}, SID : {}, replyTo : {}, data : {}, url : {}", message.getSubject(), message.getSID(), message.getReplyTo(), new String(message.getData()), message.getConnection().getConnectedUrl());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dispatchQueue(final String queueName) {
		connection.createDispatcher(msg -> {
			var id = gson.fromJson(new String(msg.getData()), Long.class);
			natsRepository.findById(id)
					.map(event -> {
						log.info("Message queue : {}, consume : {}", queueName, event);
						return event;
					}).subscribe();
		}).subscribe(FIND_ID, queueName);
	}
}
