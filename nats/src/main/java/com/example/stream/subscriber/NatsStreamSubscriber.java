package com.example.stream.subscriber;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Consumer;

import com.example.nats.domain.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.nats.streaming.Message;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsStreamSubscriber {

	private final StreamingConnection streamingConnection;

	private final Gson gson;

	private static final String SAVE = "save";

	public void save() {
		try {
			this.streamingConnection.subscribe(SAVE, msg -> {
				log.info("NatsStream save : {}", this.gson.fromJson(new String(msg.getData()), Event.class));
				ack(msg);
			}, durableOrderLastReceivedOption());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void queueConsumer() {
		var builder = new Builder();
		builder.subject(SAVE).queue("test-group").subscriptionOptions(durableFirstLastReceivedOption());
		this.ackConsumerGroup((Event e) -> {
			log.info("id: {}, msg : {}", e.getId(), e.getMessage() + " [Group Consumed]");
		}, builder.build(), Event.class).subscribe();
	}

	//모든 작업이 끝난 후에 승인 을 보내 도록 한다. (at-least-once)
	private void ack(final Message message) {
		try {
			message.ack();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public SubscriptionOptions durableOrderLastReceivedOption() {
		return new SubscriptionOptions.Builder()
				.startWithLastReceived() // 메시지 마지막 부터 전송
				.durableName("test-durable") //장애 및 어플리케이션 종료 후에도 처리 못한 데이터를 이어서 가져온다.
				.manualAcks() //메시지를 승인 할지에 대한 옵션 최소 한번 전달에 필요하다.
				.maxInFlight(1) //순서를 보장 받고 싶다면 1 (다만 1로 설정시 성능이 희생 된다.)
				.ackWait(Duration.ofSeconds(60)) //메시지 승인 타임아웃 설정
				.build();
	}

	public SubscriptionOptions durableFirstLastReceivedOption() {
		return new SubscriptionOptions.Builder()
				.deliverAllAvailable() // 처음 메시지 부터 전송
				.durableName("test-durable-first") //장애 및 어플리케이션 종료 후에도 처리 못한 데이터를 이어서 가져온다.
				.manualAcks() //메시지를 승인 할지에 대한 옵션 최소 한번 전달에 필요하다.
				.ackWait(Duration.ofSeconds(60)) //메시지 승인 타임아웃 설정
				.build();
	}

	public <T> Mono<Void> ackConsumerGroup(final Consumer<T> consumer, final Builder builder, final Class<T> clazz) {
		return Mono.fromSupplier(() -> {
			try {
				return this.streamingConnection.subscribe(builder.getSubject(), builder.getQueue(), msg -> {
					Type token = TypeToken.get(clazz).getType();
					consumer.accept(this.gson.fromJson(new String(msg.getData()), token));
					this.ack(msg);
				}, builder.getSubscriptionOptions());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}

		}).then();
	}

	@ToString
	private static class Builder {
		private String subject;

		private String queue;

		private SubscriptionOptions subscriptionOptions;

		public Builder() {
		}

		private Builder(String subject, String queue, SubscriptionOptions subscriptionOptions) {
			Assert.notNull(subject, "Subject is not null.");
			this.subject = subject;
			this.queue = queue == null ? "" : queue;
			this.subscriptionOptions = subscriptionOptions;
		}

		public String getSubject() {
			return subject;
		}

		public String getQueue() {
			return queue;
		}

		public SubscriptionOptions getSubscriptionOptions() {
			return subscriptionOptions;
		}

		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		private Builder queue(String queue) {
			this.queue = queue;
			return this;
		}

		private Builder subscriptionOptions(SubscriptionOptions subscriptionOptions) {
			this.subscriptionOptions = subscriptionOptions;
			return this;
		}

		public Builder build() {
			return new Builder(subject, queue, subscriptionOptions);
		}
	}
}
