package com.escaptain.game;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.util.concurrent.TimeUnit;

public class DemoGame {
    public static void main(String[] args) throws InterruptedException {

        JFrame frame = new JFrame("Left: Client1, Middle: Server, Right: Client 2");
        frame.setSize(new Dimension(920, 330));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        MessageService messageService = new MessageService();

        Server server = new Server(messageService);
        Client client1 = new Client(messageService, "Client 1");
        Client client2 = new Client(messageService, "Client 2");

        MainPanel mainPanel = new MainPanel(client1, client2, server);

        frame.add(mainPanel);
        frame.setVisible(true); // display frame


        TimeUnit.SECONDS.sleep(1);
        System.out.println("Client 1 connect");
        server.onConnection(client1);

        TimeUnit.SECONDS.sleep(1);
        System.out.println("Client 2 connect");
        server.onConnection(client2);
    }
}
