package com.example.nats.client.controller;

import com.example.nats.client.domain.Event;
import com.example.nats.client.stream.publisher.NatsStreamPublisher;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/nats/stream")
@RequiredArgsConstructor
public class NatsStreamController {

	private final NatsStreamPublisher streamPublisher;

	private final Gson gson;

	private static final String SAVE = "save";

	@PostMapping("")
	public Mono<String> publish(@RequestBody final Event event) {
		return this.streamPublisher.publish(SAVE, gson.toJson(event));
	}

}
