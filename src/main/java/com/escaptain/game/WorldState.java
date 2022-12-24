package com.escaptain.game;


import java.util.List;

public interface WorldState {

    // Get all entities in the world
    List<Entity> getEntities();

    Entity getActorEntity();

    // Update then world state/entities
    void update();

    // On getting an input to the world
    void onInput(Input input);
}
