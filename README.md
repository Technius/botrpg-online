BotRPG Online
=============

[![Join the chat at https://gitter.im/Technius/botrpg-online](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Technius/botrpg-online?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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
  * Add ability to bot in a game
  * Add a navigation menu, possibly offcanvas
  * Cleanup `GameCtrl` and `GameService`
  * Always put data of self on the left in battles
  * Constrain action log to a size and autoscroll to latest
* Server
  * Cleanup websocket communication
  * Divide `UserActor` into actors for inbound, outbound, and internal
  * Support spectation
  * Add chat feature
  * Improve game observer handling by generalizing messages
  * Implement a real login system
* Shared Code
  * Use traits to categorize messages
  * Move game logic from server to shared code for possible 'AI sandboxing'
    on client
