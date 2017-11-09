# gRPC Spring Security demo

This project demonstrates how to use Spring Security's method-based security mechanism to secure
gRPC services. It is a gRPC server written in Java and built on Spring Boot. It uses gRPC
interceptors to integrate with Spring Security, and supports two authentication mechanisms: HTTP
Basic Auth and OAuth2 with JSON Web Tokens.

Within this demo, the following may be of interest:

- [Package containing the gRPC interceptors](src/main/java/com/revinate/grpcspringsecurity/grpc/interceptor)
- [The gRPC service endpoint implementation with method-based security](src/main/java/com/revinate/grpcspringsecurity/grpc/DemoGrpcService.java)
- [The Spring Security configuration](src/main/java/com/revinate/grpcspringsecurity/SecurityConfiguration.java)
- [Tests demonstrating client-side code, including usage of Basic Authentication credentials](src/test/java/com/revinate/grpcspringsecurity/grpc/DemoGrpcServiceTests.java)

The [accompanying blog post](https://eng.revinate.com/2017/11/07/grpc-spring-security.html) contains
more details about the background and motivations for this demo.

## Requirements

* Java 1.8 or newer

## Usage

To start the application, run the following command in the project root directory:

```
./gradlew bootRun
```

This brings up the gRPC server, which runs on localhost port 7080.

For a simple way to interact with the gRPC server, without the need to create a client application,
[grpcc](https://github.com/njpatel/grpcc) can be used.

With grpcc installed, start a client that connects to the gRPC server by running the following
command in the `src/main/proto` directory in this project:

```
grpcc -i -p revinate/demo/demo.proto -a localhost:7080
```

This command starts the grpcc shell. Once in the shell, first create a variable to hold the metadata
entry for Basic Auth credentials:

```javascript
var md = cm({Authorization: "Basic Z3JwY3NwcmluZzpncnBjc3ByaW5n"})
```

Then make a call to the server using the credentials:

```javascript
client.fibonacci({value: 10}, md, pr)
```
