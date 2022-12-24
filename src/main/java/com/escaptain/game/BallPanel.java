package com.escaptain.game;

import lombok.extern.slf4j.Slf4j;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BallPanel extends JPanel {


    public static final int FPS = 10;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private static int RADIUS = 15;    // ball radius

    private WorldState worldState;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);


    public BallPanel(WorldState worldState) {
        this.worldState = worldState;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        setBorder(blackline);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(e.getButton() == 1) {
                    log.info("Clicked " + e.getX() + ", " + e.getY());
                    log.info("Current position " + worldState.getActorEntity().getX() + ", " + worldState.getActorEntity().getY());
                    List<Input> inputs = convertToInputs(worldState.getActorEntity().getX(), worldState.getActorEntity().getY(),
                            e.getX(), e.getY(), worldState.getActorEntity().getSpeed());
                    for(Input input : inputs) {
                        worldState.onInput(input);
                    }
                }
            }
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                worldState.update();
                repaint();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1000 / FPS, TimeUnit.MILLISECONDS);

    }

    private static List<Input> convertToInputs(int x0, int y0, int x1, int y1, int speed) {
        List<Input> re = new ArrayList<>();
        Direction horizontalDirection = x0 < x1 ? Direction.RIGHT : Direction.LEFT;
        Direction verticalDirection = y0 < y1 ? Direction.DOWN : Direction.UP;

        int horizontalMove = Math.abs(x1-x0) / speed;
        int verticalMove = Math.abs(y1 - y0) / speed;
        while(horizontalMove -- >0) {
            re.add(new Input(horizontalDirection));
        }
        while(verticalMove-- >0) {
            re.add(new Input(verticalDirection));
        }
        return re;

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); // call superclass's paintComponent

        for (Entity entity : worldState.getEntities()) {
            g.setColor(entity.getColor());
            g.fillOval(entity.getX() - RADIUS, entity.getY() - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    }

    public void moveUp() {
        worldState.onInput(new Input(Direction.UP));
    }

    public void moveDown() {
        worldState.onInput(new Input(Direction.DOWN));
    }

    public void moveLeft() {
        worldState.onInput(new Input(Direction.LEFT));
    }

    public void moveRight() {
        worldState.onInput(new Input(Direction.RIGHT));
    }

    public static Position getRandomSpawnPoint() {
        int x = (int) (Math.random() * (WIDTH - 2 * RADIUS)) + RADIUS;
        int y = (int) (Math.random() * (HEIGHT - 2 * RADIUS)) + RADIUS;
        return new Position(System.currentTimeMillis(), x, y);
    }

}