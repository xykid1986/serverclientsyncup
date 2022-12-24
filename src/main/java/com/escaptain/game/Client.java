package com.escaptain.game;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Slf4j
public class Client implements GameMessageListener, WorldState {

    private MessageService messageService;
    private String id;
    private Map<String, Entity> entities;
    private boolean enablePrediction = true;
    private boolean enableReconciliation = true;
    private boolean enableInterpolation = true;
    private BlockingQueue<Input> receivedInputs;
    private LinkedList<Input> pendingInputs;
    private AtomicInteger inputSequenceNumber;
    private BlockingQueue<GameMessage> messageBuffer;
    private Entity entity; // entity of this client


    public Client(MessageService messageService, String id) { //frame per second
        this.messageService = messageService;
        this.id = id;
        this.messageService.addListener(id, this);
        entities = new HashMap<>();
        receivedInputs = new LinkedBlockingQueue<>();
        pendingInputs = new LinkedList<>();
        messageBuffer = new LinkedBlockingQueue<>();
        inputSequenceNumber = new AtomicInteger(0);
    }

    @Override
    public void onMessage(GameMessage message) {
        messageBuffer.add(message);
    }

    private void processInputs() {
        List<Input> curInput = new ArrayList<>();
        //TODO Should there be a limit of inputs per tick? Or send inputs at a certain interval?
        receivedInputs.drainTo(curInput, 5);

        for (Input input : curInput) {
            //press time to add to input
            input.setSequenceNumber(inputSequenceNumber.getAndIncrement());
            input.setEntityId(entity.getId());

            // Client prediction happens if enabled
            if (enablePrediction) {
                this.entity.applyInput(input);
            }
            this.pendingInputs.add(input);
        }
        messageService.sendInputs(curInput);

    }

    private void processServerEvents() {
        List<GameMessage> curMessages = new ArrayList<>();
        messageBuffer.drainTo(curMessages);

        for (GameMessage message : curMessages) {
            for (Entity authEntity : message.getEntities()) {
                // new entity, add it
                if (!entities.containsKey(authEntity.getId())) {
                    log.info(id + ": New entity detected");
                    entities.put(authEntity.getId(), authEntity.copy());
                }

                //existing entity
                Entity curEntity = entities.get(authEntity.getId());

                log.debug("Current local state: " + curEntity.getX() + ", " + curEntity.getY());

                if (this.entity.getId().equals(curEntity.getId())) { // entity of this client
                    // Set the entity's state to the state of authEntity
                    curEntity.setX(authEntity.getX());
                    curEntity.setY(authEntity.getY());

                    log.debug("Current server state: " + authEntity);

                    // server reconciliation
                    if (enableReconciliation) {
                        while (!pendingInputs.isEmpty()) {
                            Input input = pendingInputs.getFirst();

                            if (input.getSequenceNumber() <= authEntity.getLastProcessedInput()) {
                                // input already processed or lost, remove it.
                                pendingInputs.removeFirst();
                            } else {
                                // not processed by server, re-apply the inputs
                                // we assume the input in pendingInputs are in order
                                // so we can just break and apply all rest inputs
                                break;
                            }
                        }

                        log.debug("Pending inputs: " + pendingInputs);

                        // Apply the rest of pending input that hasn't been return by server.
                        for (Input input : pendingInputs) {
                            curEntity.applyInput(input);
                        }
                        log.debug("Current local state after reconciliation: " + curEntity.getX() + ", " + curEntity.getY());

                    } else {
                        this.pendingInputs = new LinkedList<>();
                    }
                } else {
                    // Received the position of an entity other than this client's.
                    if (enableInterpolation) {
                        curEntity.getPositionBuffer().add(new Position(System.currentTimeMillis(), authEntity.getX(), authEntity.getY()));
                    } else {
                        curEntity.setX(authEntity.getX());
                        curEntity.setY(authEntity.getY());
                    }
                }
            }

        }
    }

    public void interpolateEntities() {
        long now = System.currentTimeMillis();
        long renderTimestamp = now - (1000 / BallPanel.FPS);

        for (Entity entity : entities.values()) {
            //skip entity of this client, do interpolation for the rest entities
            if (entity.getId() != this.entity.getId()) {
                // Find the two authoritative positions surrounding the rendering timestamp.
                List<Position> buffer = entity.getPositionBuffer();
                int j = 0;
                if (buffer.size() >= 2) {
                    for (int i = 1; i < buffer.size(); i++) {
                        if (buffer.get(i - 1).getTimestamp() <= renderTimestamp && buffer.get(i).getTimestamp() >= renderTimestamp) {
                            j = i - 1;
                            break;
                        }
                    }
                }
                //Delete the buffer before that
                entity.setPositionBuffer(buffer.subList(j, buffer.size()));

                if (entity.getPositionBuffer().size() >= 2) {
                    Position position0 = entity.getPositionBuffer().get(0);
                    Position position1 = entity.getPositionBuffer().get(1);

                    Position interpolatedPosition = interpolationDelta(position0, position1, renderTimestamp);
                    // Update the entity to interpolated entity
                    entity.setX(interpolatedPosition.getX());
                    entity.setY(interpolatedPosition.getY());
                }
            }
        }
    }

    private static Position interpolationDelta(Position p1, Position p2, long renderTimestamp) {
        double ratio = ((double) (renderTimestamp - p1.getTimestamp())) / (p2.getTimestamp() - p1.getTimestamp());
        int x = interpolate(p1.getX(), p2.getX(), ratio);
        int y = interpolate(p1.getY(), p2.getY(), ratio);
        return new Position(renderTimestamp, x, y);
    }

    private static int interpolate(int start, int end, double ratio) {
        return start + (int) ((end - start) * ratio);
    }

    @Override
    public List<Entity> getEntities() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public Entity getActorEntity() {
        return entity;
    }

    @Override
    public void update() {
        processServerEvents();

        if (this.entity == null) {
            return;  // Not connected yet.
        }

        processInputs();

        //interpolate entities
        if (enableInterpolation) {
            interpolateEntities();
        }
    }

    @Override
    public void onInput(Input input) {
        receivedInputs.add(input);
        log.debug(id + " received input " + input.getDirection());
    }
}
