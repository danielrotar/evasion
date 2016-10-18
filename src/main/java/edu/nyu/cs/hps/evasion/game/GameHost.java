package edu.nyu.cs.hps.evasion.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameHost {

  public static void hostGame(int portP1, int portP2, int maxWalls, int wallPlacementDelay) throws Exception {

    System.out.println("Player 1: connect to port " + portP1);
    System.out.println("Player 2: connect to port " + portP2);

    IO io = new IO();
    List<Integer> ports = new ArrayList<>();
    ports.add(portP1);
    ports.add(portP2);
    io.start(ports);

    System.out.println("Starting game.");

    int hunterIndex = 0;
    int preyIndex = 1;
    int gameNum = 0;

    int p1AsPreyScore = 0;
    int p2AsPreyScore = 0;

    while(gameNum < 8) {
      Game game = new Game(maxWalls, wallPlacementDelay);

      io.sendLine(hunterIndex, "hunter");
      io.sendLine(preyIndex, "prey");

      boolean done = false;
      while (!done) {
        String gameString = gameNum + " " + game.getState().toString();
        io.sendLine(hunterIndex, gameString);
        io.sendLine(preyIndex, gameString);

        Thread.sleep(1000 / 60);

        String hunterInput = io.getLatestLine(hunterIndex);
        String preyInput = io.getLatestLine(preyIndex);

        Game.WallCreationType hunterWallAction = Game.WallCreationType.NONE;
        List<Integer> hunterWallsToDelete = new ArrayList<>();
        Point preyMovement = new Point(0, 0);

        if (hunterInput != null) {
          List<Integer> data = Arrays.stream(hunterInput.split("\\s+"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
          if(data.get(1) == game.getState().ticknum) {
            if (data.size() >= 3 && data.get(0) == gameNum) {
              if (data.get(2) == 1) {
                hunterWallAction = Game.WallCreationType.HORIZONTAL;
              } else if (data.get(2) == 2) {
                hunterWallAction = Game.WallCreationType.VERTICAL;
              }
              hunterWallsToDelete = data.subList(3, data.size());
            }
          } else {
            System.out.println("Player " + (hunterIndex+1) + " is lagging.");
          }
        } else {
          System.out.println("Player " + (hunterIndex+1) + " has not given input.");
        }
        if (preyInput != null) {
          List<Integer> data = Arrays.stream(preyInput.split("\\s+"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
          if(data.get(1) == game.getState().ticknum) {
            if (data.size() >= 4 && data.get(0) == gameNum && data.get(1) == game.getState().ticknum) {
              preyMovement.x = data.get(2);
              preyMovement.y = data.get(3);
            }
          } else {
            System.out.println("Player " + (preyIndex+1) + " is lagging.");
          }
        } else {
          System.out.println("Player " + (preyIndex+1) + " has not given input.");
        }

        done = game.tick(hunterWallAction, hunterWallsToDelete, preyMovement);
      }

      if(preyIndex == 0){
        p1AsPreyScore += game.getState().ticknum;
      } else {
        p2AsPreyScore += game.getState().ticknum;
      }
      System.out.println("Score (hunter=" + io.getName(hunterIndex) + ", prey=" + io.getName(preyIndex) + "): " + game.getState().ticknum);

      hunterIndex = 1-hunterIndex;
      preyIndex = 1-preyIndex;
      gameNum++;
    }

    if(p1AsPreyScore == p2AsPreyScore){
      System.out.println("Tied! Both = " + p1AsPreyScore);
    }
    String winner = (p1AsPreyScore > p2AsPreyScore) ? io.getName(0) : io.getName(1);
    System.out.println(winner + " wins (" + io.getName(0) + " = " + p1AsPreyScore + ", " + io.getName(1) + " = " + p2AsPreyScore + ")");

    io.sendLine(hunterIndex, "done");
    io.sendLine(preyIndex, "done");

    io.destroy();
  }
}
