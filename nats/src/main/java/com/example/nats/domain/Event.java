package com.example.nats.domain;

import lombok.Data;

@Data
public class Event {
	private final Long id;
	private final String message;
}
