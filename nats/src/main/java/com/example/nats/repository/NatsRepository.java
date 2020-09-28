package com.example.nats.repository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.example.nats.domain.Event;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class NatsRepository {

	private static final Map<Long, String> REPOSITORY = new ConcurrentHashMap<>();

	public Mono<Void> save(final Event event) {
		var e = Objects.requireNonNull(event);
		REPOSITORY.putIfAbsent(e.getId(), e.getMessage());
		return Mono.just(e).then();
	}

	public Mono<String> findById(final Long id) {
		return Mono.just(Optional.ofNullable(REPOSITORY.get(id)).orElse(""));
	}
}
