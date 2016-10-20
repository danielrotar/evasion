# evasion

This is the implementation of the architecture for the [evasion game](http://cs.nyu.edu/courses/fall16/CSCI-GA.2965-001/evasion.html). Any questions, requests, or bug reporting, please contact me at dmr465@nyu.edu or via class mailing list!

# Requirements

* Java 8 SDK
* Apache Maven

(available on energon2)

# Clone and build

(on energon2)

```
git clone git@github.com:danielrotar/evasion.git
cd evasion
module load java-1.8
mvn clean package
```

# Run

(on energon2)

```
module load java-1.8
java -jar ./target/evasion-1.0-SNAPSHOT.jar [player 1 port] [player 2 port] [max walls] [wall placement delay]
```

# Details

Players should connect on the ports specified as arguments to the jar after it has started executing. Each message sent in either direction (to client or to server) should/will end with a newline.

Upon connecting, players will be prompted with `sendname`; they should respond with their team name. The game won't start until both players have done so.

Multiple games will be played during a single session. At the start of each game, the player will receive the message `hunter` or `prey`, which identifies their role in the upcoming match. Players should not respond to this message.

Immediately after that, the game will commence and the server will begin sending messages containing the full game state at each iteration to each player. These messages look like:

```
[playerTimeLeft] [gameNum] [tickNum] [maxWalls] [wallPlacementDelay] [boardsizeX] [boardsizeY] [currentWallTimer] [hunterXPos] [hunterYPos] [hunterXVel] [hunterYVel] [preyXPos] [preyYPos] [numWalls] {wall1 info} {wall2 info} ... 
```

Each "{wall info}" above is just a set of numbers describing a wall on the playing field. There will be `[numWalls]` such sets.

A horizontal wall is identified by: `0 [y] [x1] [x2]` where `y` is its y location, `x1` is the x location of its left-most pixel (e.g. smaller x value), and `x2` is the x location of its right-most pixel (e.g. larger x value). 

A vertical wall is identified by: `1 [x] [y1] [y2]` where `x` is its x location, `y1` is the y location of its bottom-most pixel (e.g. smaller y value), and `y2` is the y location of its top-most pixel (e.g. larger y value). 

The order of the `{wall info}` sets is relevent; when the hunter references a wall to delete it should do so using the wall's place in this list, starting at 0.

Note that both players get the same game state information each tick, EXCEPT for playerTimeLeft which is specific to the given player (and given in milliseconds). (Players are not permitted to know how close their opponents are to exhausting their time limits.)

# Hunter

In response to each received game state message, the hunter should send the following:

```
[gameNum] [tickNum] [wall type to add] [wall index to delete] [wall index to delete] [wall index to delete] ...
```

`[gameNum]` and `[tickNum]` should be relayed directly back to the server based on which game state message this action is in response to. 

`[wall type to add]` should be 0 for no wall, 1 for horizontal wall, 2 for vertical wall. There is no penalty for asking for a wall that can't be built for whatever reason.

Each `[wall index to delete]` specifies a wall to be deleted, based on its position (starting at 0) in the game state message. There can be any number of these, or none. Asking to delete a wall index that doesn't exist has no effect.

The hunter bounce behavior is as follows (* = wall, arrow = hunter moving in the direction it points):

```
First the pixels adjacent to both the hunter and the one being hit are considered...

+-+-+    +-+-+
|*| |    |*|↗|
+-+-+ -> +-+-+
|*|↖|    |*| |
+-+-+    +-+-+

+-+-+    +-+-+
|*|*|    |*|*|
+-+-+ -> +-+-+
| |↖|    |↙| |
+-+-+    +-+-+

+-+-+    +-+-+
|*|*|    |*|*|
+-+-+ -> +-+-+
|*|↖|    |*|↘|
+-+-+    +-+-+


+-+-+
|*| |
+-+-+  = search is expanded, now including the other pixels adjacent to the one being hit...
| |↖|
+-+-+


  +-+        +-+
  | |        | |
+-+-+-+    +-+-+-+
|*|*| | -> |*|*| |
+-+-+-+    +-+-+-+
  | |↖|      |↙| |
  +-+-+      +-+-+

  +-+        +-+
  |*|        |*|
+-+-+-+    +-+-+-+
| |*| | -> | |*|↗|
+-+-+-+    +-+-+-+
  | |↖|      | | |
  +-+-+      +-+-+

  +-+        +-+
  |*|        |*|
+-+-+-+    +-+-+-+
|*|*| | -> |*|*| |
+-+-+-+    +-+-+-+
  | |↖|      | |↘|
  +-+-+      +-+-+

  +-+        +-+
  | |        | |
+-+-+-+    +-+-+-+
| |*| | -> | |*| |
+-+-+-+    +-+-+-+
  | |↖|      | |↘|
  +-+-+      +-+-+
```

(The same calculations are mirrored about the relevant axes for the other three directions in which the hunter can be traveling.)

# Prey

In response to each received game state message, the prey should send the following:

```
[gameNum] [tickNum] [x movement] [y movement]
```

`[gameNum]` and `[tickNum]` should be relayed directly back to the server based on which game state message this action is in response to. 

The x and y movement specifies the direction in which the prey wishes to travel. This should be 1, 0, or -1 for each -- values outside this range will be clamped. On ticks in which the prey can't move (`tickNum % 2 == 0`), these fields will have no effect, but placeholder values should still be sent.

# Capture

As noted in the class site game overview, the game is over when the Euclidean distance between hunter and prey is within four, and there is no wall between them. Specifically, this implementation calculates whether a wall is between them by using [Bresenham's line algorithm](https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm) to find the points approximately on the line segment connecting them, and testing whether a wall occupies any of those points. 

Specifically, this [helpful reference](http://www.roguebasin.com/index.php?title=Bresenham%27s_Line_Algorithm#Ruby) was used as a guideline for implementing the algorithm. The Ruby version specifically was translated into a Java implementation for use in this project.

# Other notes

Each player has two minutes of "thinking" time, which can be distributed across ticks as they see fit. If a player's total elapsed time reaches zero on any turn, they forfeit. (Note: this is basically the opposite of what was described in class -- the realtime, 1/60th of a second per tick, server-ignoring-old-moves system has been scrapped.)

The current game is over when the server sends the message `hunter` or `prey`, meaning a new game is about to start, or `done`, meaning the session is over and the player should disconnect.

# Player code

Sample player code can be found in the "players" directory (random_player.py).

To run: `python random_player.py [port on which to connect]`

# TODO

* Design and implement a visual game display.
* Fix any bugs that are inevitably present.
