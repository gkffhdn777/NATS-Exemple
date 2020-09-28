NATS Server, NAST Streaming
===================

## Installing

NATS Server : https://docs.nats.io/nats-server/installation

NAST Streaming : https://docs.nats.io/nats-streaming-server/install

## Getting Started
#### NAST Server
~~~
Server A
nats-server -p 4222 -cluster nats://localhost:4248 &

Server B
nats-server -p 5222 -cluster nats://localhost:5248 -routes nats://localhost:4248 &

Server C
nats-server -p 6222 -cluster nats://localhost:6248 -routes nats://localhost:4248 &

#### Check the output of the server for the selected client and route ports.
~~~
#### NAST Streaming
~~~
nats-streaming-server -m 8222 -store file -dir store-a -clustered -cluster_node_id a -cluster_peers b,c -nats_server nats://localhost:4222 &

nats-streaming-server -store file -dir store-b -clustered -cluster_node_id b -cluster_peers a,c -nats_server nats://localhost:5222 &

nats-streaming-server  -store file -dir store-c -clustered -cluster_node_id c -cluster_peers a,b -nats_server nats://localhost:6222 &
~~~

## Client API
~~~
 POST http://localhost:8080/v1/nats/stream
{
   "id" : 1,
   "message" : "메시지를 보내봅니다."
 }
~~~
~~~
POST http://localhost:8080/v1/nats
{
   "id" : 1,
   "message" : "메시지를 보내봅니다."
 }
~~~
~~~
GET http://localhost:8080/v1/nats/1
~~~

## Explain
~~~java
@RequiredArgsConstructor
@SpringBootApplication
public class NatsApplication {

	private final NatsSubscriber natsSubscriber;

	private final NatsStreamSubscriber natsStreamSubscriber;

	private static final String GROUP_ID = "findByIdGroup";

	public static void main(String[] args) {
		SpringApplication.run(NatsApplication.class, args);
	}

	//사용시 주석 해제 
	@EventListener(ApplicationReadyEvent.class)
	public void natsReady() {
		//nats();
		natsStreaming();
	}
}
~~~