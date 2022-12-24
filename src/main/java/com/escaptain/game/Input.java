package com.escaptain.game;

import lombok.Data;

@Data
public class Input {

    private int sequenceNumber;
    private String entityId;
    private Direction direction;

    public Input(Direction direction) {
        this.direction = direction;
    }

}
