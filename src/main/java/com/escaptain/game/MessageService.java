package com.escaptain.game;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageService {

    private Map<String, GameMessageListener> listeners;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(30);

    public static final int NETWORK_DELAY_MILLISECONDS = 100;

    public MessageService() {
        listeners = new ConcurrentHashMap<>();
    }

    public void addListener(String id, GameMessageListener listener) {
        listeners.put(id, listener);
    }

    public void sendInputs(List<Input> inputs) {
        GameMessage message = new GameMessage(MessageType.INPUT, inputs);
        send(message, Server.SERVER_ID);
    }

    public void send(GameMessage message, String recipientId) {
        scheduler.schedule(() -> listeners.get(recipientId).onMessage(message), NETWORK_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS);
    }


}
