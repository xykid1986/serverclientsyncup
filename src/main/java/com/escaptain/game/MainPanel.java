package com.escaptain.game;


import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MainPanel extends JPanel implements KeyListener {

    private BallPanel player1UI;
    private BallPanel player2UI;
    private BallPanel serverUI;

    public MainPanel(Client client1, Client client2, Server server) {
        setPreferredSize(new Dimension(900, 300));
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        player1UI = new BallPanel(client1);
        player2UI = new BallPanel(client2);
        serverUI = new BallPanel(server);

        add(player1UI);
        add(serverUI);
        add(player2UI);

        addKeyListener(this);

        setFocusable(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == e.VK_UP) {
            player2UI.moveUp();
        } else if (keyCode == e.VK_DOWN) {
            player2UI.moveDown();
        } else if (keyCode == e.VK_LEFT) {
            player2UI.moveLeft();
        } else if (keyCode == e.VK_RIGHT) {
            player2UI.moveRight();
        }else if (keyCode == e.VK_S) {
            player1UI.moveDown();
        } else if (keyCode == e.VK_W) {
            player1UI.moveUp();
        } else if (keyCode == e.VK_D) {
            player1UI.moveRight();
        }else if (keyCode == e.VK_A) {
            player1UI.moveLeft();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}