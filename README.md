BotRPG Online
=============

BotRPG Online is a work-in-progress, open source MMORPG built with Scala, Play
Framework, Akka, and Scala.js. Unlike most MMORPGs, where *players* are supposedto play the game, players are supposed to program *bots* to play BotRPG for
them.

An instance of BotRPG is running on Heroku [here](http://botrpg-online.herokuapp.com).

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
  * Fix login message bug
  * Fix play button alignment
  * Add ability to bot in a game
  * Cleanup `GameCtrl` and `GameService`
* Server
  * Cleanup websocket communication
  * Cleanup `UserActor`: Assume states other than `NoUser` are logged in
  * Eliminate `SocketMessage` sending boilerplate
  * Implement a real login system
  * Support spectation
  * Add chat feature
  * Improve game observer handling
* Shared Code
  * Use traits to categorize messages
