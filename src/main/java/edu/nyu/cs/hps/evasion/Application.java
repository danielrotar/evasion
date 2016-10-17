package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameHost;

public class Application {
  public static void main(String[] args) {

    try {
      GameHost.hostGame(5001, 5002, 10, 15);
    } catch (Exception e){
      System.err.println(e.getMessage());
    }
  }
}
