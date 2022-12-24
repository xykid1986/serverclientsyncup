## Quick start

You may copy the example to use as a starting template, and build off from there. Either to use directly in your projects, or as examples for you to integrate your own choice of networking layer.

Just checkout the code and run class DemoGame.java

![Alt text](demo-snapshot.png?raw=true "Demo")

Use keys A/W/S/D to move the ball of client 1, UP/DOWN/LEFT/RIGHT arrows keys to move the ball of client 2. You can also click on the panel of client 1 and client 2 to the ball to where it is clicked.

You can tune the FPS in BallPanel.java and network delay in MessageService.

## Requirement

Java 8+ and Maven

## About

Realtime multiplayer game networking is complex. This project is to give an illusion that how game state is synced up between multiple game clients despite having significant network latency between the clients and the server. There are many different ways to solve the game networking problem, this project uses the below methods to overcome the networking problems:

- **Client-side prediction.** Clients immediately apply their local input to their simulation before waiting for the server, so that the player's inputs feel responsive.
- **Server reconciliation.** Server runs a delayed, authoritative version of the simulation, and periodically sends authoritative snapshots to each client. Since the server's snapshots represent an earlier simulation frame, each client fast-forwards the snapshot they receive until it matches the same timestamp as what's being shown on screen. Once the timestamps match, clients smoothly blend their states to the snapshot states.
- **Display state interpolation.** The simulation can run at a different time-step from the render framerate, and the client will automatically interpolate between the two simulation frames to get the render display state.

## Acknowledge

Inspired by the article [Fast-Paced Multiplayer](https://www.gabrielgambetta.com/client-server-game-architecture.html) and the live example.