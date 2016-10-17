package edu.nyu.cs.hps.evasion.game;

import java.awt.*;

public class HorizontalWall implements Wall {

  private int y;
  private int x1;
  private int x2;

  HorizontalWall(int y, int x1, int x2){
    this.y = y;
    this.x1 = x1;
    this.x2 = x2;
  }

  public boolean occupies(Point point){
    return point.y == this.y && point.y >= this.x1 && point.y <= this.x2;
  }

}
