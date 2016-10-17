package edu.nyu.cs.hps.evasion.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameState {

  private List<Wall> walls;
  private int maxWalls;
  private int wallPlacementDelay;
  private int wallTimer;
  private PositionAndVelocity hunterPosAndVel;
  private Point preyPos;
  private long ticknum;

  private final static Point BOARD_SIZE = new Point(300,300);

  private static class PositionAndVelocity {
    public Point pos;
    public Point vel;

    public PositionAndVelocity(PositionAndVelocity positionAndVelocity){
      pos = new Point(positionAndVelocity.pos);
      vel = new Point(positionAndVelocity.vel);
    }

    public PositionAndVelocity(Point pos, Point vel){
      this.pos = new Point(pos);
      this.vel = new Point(vel);
    }
  }

  public enum WallCreationType {
    NONE,
    HORIZONTAL,
    VERTICAL
  }

  public GameState(int maxWalls, int wallPlacementDelay){
    this.walls = new ArrayList<>();
    this.maxWalls = maxWalls;
    this.wallPlacementDelay = wallPlacementDelay;
    this.wallTimer = 0;
    this.hunterPosAndVel = new PositionAndVelocity(new Point(0, 0), new Point(1,1));
    this.preyPos = new Point(230, 200);
    this.ticknum = 0;
  }

  public boolean tick(WallCreationType hunterWallAction, List<Integer> hunterWallsToDelete, Point preyMovement){
    for(Integer index : hunterWallsToDelete){
      removeWall(index);
    }
    Point prevHunterPos = new Point(hunterPosAndVel.pos);
    hunterPosAndVel = move(hunterPosAndVel);
    if(canPreyMove()) {
      preyPos = move(new PositionAndVelocity(this.preyPos, preyMovement)).pos;
    }
    doBuildAction(prevHunterPos, hunterWallAction);
    return captured();
  }

  private boolean isOccupied(Point p) {
    if(p.x < 0 || p.x >= BOARD_SIZE.x || p.y < 0 || p.y >= BOARD_SIZE.y){
      return true;
    }
    for(Wall wall : walls){
      if(wall.occupies(p)){
        return true;
      }
    }
    return false;
  }

  private boolean addWall(Wall wall){
    if(walls.size() < maxWalls && wallTimer <= 0){
      walls.add(wall);
      wallTimer = wallPlacementDelay;
      return true;
    } else {
      return false;
    }
  }

  private void removeWall(int index){
    if(index > 0 && index < walls.size()) {
      walls.remove(index);
    }
  }

  public boolean captured(){
    return hunterPosAndVel.pos.distance(preyPos) < 4.0;
  }

  private boolean canPreyMove(){
    return (ticknum % 2) != 0;
  }

  private boolean doBuildAction(Point pos, WallCreationType action){
    if(action == WallCreationType.HORIZONTAL){
      Point greater = new Point(pos);
      Point lesser = new Point(pos);
      while(!isOccupied(greater)){
        if(greater.equals(hunterPosAndVel.pos) || greater.equals(preyPos)){
          return false;
        }
        greater.x++;
      }
      while(!isOccupied(lesser)){
        if(lesser.equals(hunterPosAndVel.pos) || lesser.equals(preyPos)){
          return false;
        }
        lesser.x--;
      }
      HorizontalWall horizontalWall = new HorizontalWall(pos.y, lesser.x+1, greater.x-1);
      return addWall(horizontalWall);
    } else if(action == WallCreationType.VERTICAL){
      Point greater = new Point(pos);
      Point lesser = new Point(pos);
      while(!isOccupied(greater)){
        if(greater.equals(hunterPosAndVel.pos) || greater.equals(preyPos)){
          return false;
        }
        greater.y++;
      }
      while(!isOccupied(lesser)){
        if(lesser.equals(hunterPosAndVel.pos) || lesser.equals(preyPos)){
          return false;
        }
        lesser.y--;
      }
      VerticalWall verticalWall = new VerticalWall(pos.x, lesser.y+1, greater.y-1);
      return addWall(verticalWall);
    }
    return false;
  }

  private PositionAndVelocity move(PositionAndVelocity posAndVel){
    PositionAndVelocity newPosAndVel = new PositionAndVelocity(posAndVel);
    newPosAndVel.vel.x = Math.min(Math.max(newPosAndVel.vel.x, -1), 1);
    newPosAndVel.vel.y = Math.min(Math.max(newPosAndVel.vel.y, -1), 1);
    Point target = add(newPosAndVel.pos, newPosAndVel.vel);
    if(!isOccupied(target)){
      newPosAndVel.pos = add(newPosAndVel.pos, newPosAndVel.vel);
    } else {
      if(newPosAndVel.vel.x == 0 || newPosAndVel.vel.y == 0){
        if(newPosAndVel.vel.x != 0){
          newPosAndVel.vel.x = -newPosAndVel.vel.x;
        } else {
          newPosAndVel.vel.y = -newPosAndVel.vel.y;
        }
      } else {
        boolean oneRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x, 0)));
        boolean oneUp = isOccupied(add(newPosAndVel.pos, new Point(0, newPosAndVel.vel.y)));
        if (oneRight && oneUp) {
          newPosAndVel.vel.x = -newPosAndVel.vel.x;
          newPosAndVel.vel.y = -newPosAndVel.vel.y;
        } else if (oneRight) {
          newPosAndVel.vel.x = -newPosAndVel.vel.x;
          newPosAndVel.pos.y = target.y;
        } else if (oneUp) {
          newPosAndVel.vel.y = -newPosAndVel.vel.y;
          newPosAndVel.pos.x = target.x;
        } else {
          boolean twoUpOneRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x, newPosAndVel.vel.y * 2)));
          boolean oneUpTwoRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x * 2, newPosAndVel.vel.y)));
          if ((twoUpOneRight && oneUpTwoRight) || (!twoUpOneRight && !oneUpTwoRight)) {
            newPosAndVel.vel.x = -newPosAndVel.vel.x;
            newPosAndVel.vel.y = -newPosAndVel.vel.y;
          } else if (twoUpOneRight) {
            newPosAndVel.vel.x = -newPosAndVel.vel.x;
            newPosAndVel.pos.y = target.y;
          } else {
            newPosAndVel.vel.y = -newPosAndVel.vel.y;
            newPosAndVel.pos.x = target.x;
          }
        }
      }
    }
    return newPosAndVel;
  }

  private static Point add(Point a, Point b){
    return new Point(a.x + b.x, a.y + b.y);
  }

}
