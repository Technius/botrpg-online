BotRPG Online
=============

BotRPG Online is a work-in-progress, open source MMORPG built with Scala, Play Framework,
Akka, and Scala.js. Unlike most MMORPGs, where *players* are supposed to play
the game, players are supposed to program *bots* to play BotRPG for them.

Running
=======

1. Clone this repository.
2. `activator`
3. `project server`
4. `run`
5. In your favorite Internet browser, navigate to `http://localhost:9000`.

To do
=====
In no particular order:
* Client
  * Create matchmaking page
  * Implement games
  * Implement game interface
  * Add ability to bot in a game
* Server
  * Implement game starting and ending
  * Implement real login
