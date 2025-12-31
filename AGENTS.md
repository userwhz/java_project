# Agent Instructions

## Project Overview
- Java Swing Snake game.
- Main application entry point: `com.example.snake.GameApplication`.
- Additional scratch/demo file: `src/Main.java` (not part of the Snake app).

## Structure
- `src/com/example/snake/`: game UI and logic.
- `README.md`: build/run instructions.

## Build and Run
- Requires JDK 8+.
- Compile:
- `javac -d out $(find src -name "*.java")`
- Run:
  - `java -cp out com.example.snake.GameApplication`

## Coding Conventions
- Keep classes in the `com.example.snake` package for game code.
- Prefer clear, small methods for UI updates and game loop logic.
- Avoid introducing new dependencies unless necessary.

## Changes and Safety
- If modifying game mechanics, keep input handling and rendering responsive.
- If editing `src/Main.java`, treat it as a standalone demo and avoid mixing it with the game code.
