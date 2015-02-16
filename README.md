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
  * Fix lobby's play button alignment
  * Add ability to bot in a game
  * Add a navigation menu, possibly offcanvas
  * Cleanup `GameCtrl` and `GameService`
* Server
  * Properly handle players who leave in the midddle of a game
  * Cleanup websocket communication
  * Refactor `UserActor` into another package
  * Divide `UserActor` into actors for inbound, outbound, and internal
  * Support spectation
  * Add chat feature
  * Improve game observer handling
  * Implement a real login system
* Shared Code
  * Use traits to categorize messages
  * Move game logic from server to shared code for possible 'ai sandboxing'
