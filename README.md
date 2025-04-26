# University of Applied Sciences Offenburg - AI6 - Distributed Systems - Multiplayer Click-Game

A Java project developed in Eclipse, where players compete against each other and try to click a specific number of fields in a row to score points.

## Features
- Host and client mode: Players can either create a game or join an existing one
- Players click fields to achieve X fields in a row (X is defined when the game is created)
- Real-time synchronization of moves between all players
- Join a game via IP address and port to play within the same network
- start multiple game instances on the same machine to create a game or join one

## Technical Details
- Network communication via UDP
- Peer-to-Peer Network: The game uses a peer-to-peer network model, where players can join via a friend. The group grows and shrinks dynamically as players join or leave


---------------------------------------------------------------------------------------------------------------------------------
[Video Preview of the Desktop Application]()

![Image Preview of the Desktop Application]()
