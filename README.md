BotRPG Online
=============

[![Join the chat at https://gitter.im/Technius/botrpg-online](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Technius/botrpg-online?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

BotRPG Online is a work-in-progress, open source MMORPG built with Scala, Play
Framework, Akka, and Scala.js. Unlike most MMORPGs, where *players* are supposed
to play the game, players are supposed to program *bots* to play BotRPG for
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
  * Always put data of self on the left/top in battles
  * Find way to detect if user changes scroll to bottom in action autoscroll
* Server
  * Cleanup websocket communication
  * Divide `UserActor` into actors for inbound, outbound, and internal
  * Add chat feature
  * Implement a real login system
  * Automatically force a draw if turn exceeds a certain number (30?)
* Shared Code
  * Use traits to categorize messages
  * Move game logic from server to shared code for possible 'AI sandboxing'
    on client
