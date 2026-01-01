# Agent Instructions

## Project Overview
- Spring Boot web app + Java Swing Snake game in one repo.
- Web app entry point: `com.example.Application`.
- Snake game entry point: `com.example.snake.GameApplication`.
- Additional scratch/demo file: `src/Main.java` (not part of the Snake app).

## Structure
- `pom.xml`: Maven build config.
- `src/main/java/com/example/`: Spring Boot app (`Application`, controllers).
- `src/main/java/com/example/snake/`: Swing game UI and logic.
- `src/main/resources/`: Spring Boot config (e.g. `application.properties`).
- `src/Main.java`: standalone Java demo (not part of Spring Boot).
- `README.md`: build/run instructions.

## Build and Run
- Requires JDK 8+.
- Spring Boot:
  - `mvn spring-boot:run`
- Swing:
  - `javac -d out $(find src/main/java -name "*.java")`
  - `java -cp out com.example.snake.GameApplication`

## Coding Conventions
- Keep classes in the `com.example.snake` package for game code.
- Prefer clear, small methods for UI updates and game loop logic.
- Avoid introducing new dependencies unless necessary.

## Changes and Safety
- If modifying game mechanics, keep input handling and rendering responsive.
- If editing `src/Main.java`, treat it as a standalone demo and avoid mixing it with the game code.
