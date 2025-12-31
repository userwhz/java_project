package com.example.snake;

import javax.swing.JFrame;

/**
 * Simple window wrapper for the Snake game.
 */
public class GameFrame extends JFrame {
    public GameFrame() {
        add(new GamePanel());
        setTitle("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
