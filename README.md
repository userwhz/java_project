# Java Demo (Spring Boot + Swing)

This project contains a Spring Boot web app and a simple Swing Snake game.

## Requirements

- JDK 8+
- Maven

## Spring Boot

Run the web app:

```bash
mvn spring-boot:run
```

Then open:

```
http://localhost:8080/
```

## Swing Snake Game

Compile and run the Swing app:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.example.snake.GameApplication
```
