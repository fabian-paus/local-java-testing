# Local Java Testing

For integration tests or your local development environment, you often want to start other applications, e.g. Keycloak for authentication or Kafka as a message broker.
One common approach are [Testcontainers](https://testcontainers.com/). They run the applications using Docker.
This repository focuses on the main strength of Java: A cross-platform virtual machine that can run anything.
If you are using Java, you do not need the complexity and potential overhead of Docker to run your integration tests.
