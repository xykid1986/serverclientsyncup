package com.escaptain.game;

import lombok.Data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Entity implements Cloneable{

    private String id;
    private int x;
    private int y;
    private int speed = 10;
    private List<Position> positionBuffer;
    private int lastProcessedInput;
    private Color color;

    public Entity(int x, int y) {
        this(x, y, null);
    }

    public Entity(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.id = UUID.randomUUID().toString().substring(0, 3);
        positionBuffer = new ArrayList<>();
        this.color = color;
    }

    public void applyInput(Input input) {
        lastProcessedInput = input.getSequenceNumber();
        switch (input.getDirection()) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
                break;
        }
    }


    public Entity copy() {
        Entity copy = new Entity(this.x, this.y);
        copy.setId(id);
        copy.setSpeed(speed);
        copy.color = color;
        copy.positionBuffer = new ArrayList<>();
        for(Position position : positionBuffer) {
            copy.positionBuffer.add(new Position(position.getTimestamp(), position.getX(), position.getY()));
        }
        copy.setLastProcessedInput(lastProcessedInput);
        return copy;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", lastProcessedInput=" + lastProcessedInput +
                '}';
    }
}
