package com.escaptain.game;

import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class Server implements GameMessageListener, WorldState{


    private List<Client> clients;
    private Map<String, Entity> entities;
    private Map<String, Integer> lastProcessedInput;
    private BlockingQueue<Input> inputsReceived;
    private MessageService messageService;


    public static final String SERVER_ID = "Server";
    private static final Color[] COLORS = new Color[] {Color.red, Color.blue, Color.green, Color.yellow};


    public Server(MessageService messageService) {
        clients = new ArrayList<>();
        entities = new HashMap<>();
        lastProcessedInput = new HashMap<>();
        inputsReceived = new LinkedBlockingQueue<>();
        messageService.addListener(SERVER_ID, this);
        this.messageService = messageService;
    }

    @Override
    public void update() {
        processClientInputs();
        broadcastGameState();
    }

    @Override
    public void onInput(Input input) {
        // Do nothing, there must be no UI input to the server.
    }

    public void processClientInputs() {
        List<Input> curInputs = new ArrayList<>();
        inputsReceived.drainTo(curInputs);
        if(curInputs.size() > 0) {
            log.info("Process client inputs: " + curInputs);
        }
        for(Input input : curInputs) {
            //Filter out invalid inputs
            if (this.validateInput(input)) {
                String entityId = input.getEntityId();
                this.entities.get(entityId).applyInput(input);
                // TODO What if the last input sequence number is larger then the incoming input sequence number?
                this.lastProcessedInput.put(entityId, input.getSequenceNumber());
            }
        }
    }

    public boolean validateInput(Input input) {
        //TODO implement the way to detect invalid inputs
        return true;
    }

    public void broadcastGameState() {
        List<Entity> worldState = new ArrayList<>();
        for(Entity entity : entities.values()) {
            //TODO this is where optimization can happen: 1. Filter out unchanged entities. 2. Only send changed field of entities.
           worldState.add(entity.copy()); // No need to copy if it is going to be serialized and sent to remote client.
        }
        GameMessage message = new GameMessage(MessageType.STATE, null, worldState);

        //Broadcast updated world state to all clients.
        for(Client client : clients) {
            messageService.send(message, client.getId());
        }
    }

    @Override
    public void onMessage(GameMessage message) {
        if(message.getType() == MessageType.INPUT) {
            inputsReceived.addAll(message.getInputs());
        } else {
            log.error("Received non-input message!");
        }
    }

    @Override
    public List<Entity> getEntities() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public Entity getActorEntity() {
        // server has no actor entity
        return null;
    }


    public void onConnection(Client client) {
        Position startPoint = BallPanel.getRandomSpawnPoint();
        Entity entity = new Entity(startPoint.getX(), startPoint.getY(), COLORS[clients.size()]);
        log.info("Assigning entity " + entity.getId() + " to client " + client.getId());
        clients.add(client);
        client.setEntity(entity.copy());
        entities.put(entity.getId(), entity);
    }
}
