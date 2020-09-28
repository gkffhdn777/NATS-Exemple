package com.example.nats.client.controller;

import com.example.nats.client.domain.Event;
import com.example.nats.client.publisher.NatsPublisher;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/nats")
@RequiredArgsConstructor
public class NatsController {

	private static final String SAVE = "save";

	private static final String FIND_ID = "findById";

	private final NatsPublisher natsPublisher;

	private final Gson gson;

	@PostMapping("")
	public Mono<Boolean> publish(@RequestBody final Event event) {
		return this.natsPublisher.publish(SAVE, gson.toJson(event));
	}

	@GetMapping("/{id}")
	public Mono<String> requestAndReply(@PathVariable final String id) {
		return this.natsPublisher.request(FIND_ID, id)
				.flatMap(message -> Mono.just(new String(message.getData())));
	}
}
