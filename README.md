# Java Snake Game

This is a simple Swing implementation of the classic Snake game.

## How to build and run

You only need a Java 8+ JDK. From the project root:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.example.snake.GameApplication
```

Use the arrow keys to move the snake and press **Enter** after a game over to restart.
