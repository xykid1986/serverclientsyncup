package com.escaptain.game;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GameMessage {

    private MessageType type;
    private List<Input> inputs;
    private List<Entity> entities;

    public GameMessage(MessageType type, List<Input> inputs) {
        this(type, inputs, new ArrayList<>());
    }

    public GameMessage(MessageType type, List<Input> inputs, List<Entity> entities) {
        this.type = type;
        this.inputs = inputs;
        this.entities = entities;
    }

}
