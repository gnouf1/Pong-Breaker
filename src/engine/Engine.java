/* ******************************************************
 * Project alpha - Composants logiciels 2015.
 * Copyright (C) 2015 <Binh-Minh.Bui-Xuan@ens-lyon.org>.
 * GPL version>=3 <http://www.gnu.org/licenses/>.
 * $Id: engine/Engine.java 2015-03-11 buixuan.
 * ******************************************************/
package engine;

import data.Ball;
import data.Palette;
import javafx.geometry.Pos;
import tools.HardCodedParameters;
import tools.User;
import tools.Position;
import tools.Sound;

import specifications.EngineService;
import specifications.DataService;
import specifications.RequireDataService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.ArrayList;

public class Engine implements EngineService, RequireDataService{
  private static final double friction=HardCodedParameters.friction;
  private Timer engineClock;
  private DataService data;
  private User.COMMAND command;
  private Random gen;
  private boolean moveLeft,moveRight,moveUp,moveDown;
  private boolean RmoveLeft,RmoveRight,RmoveUp,RmoveDown;
  private double blueVX,blueVY,redVX,redVY;
  private double ballVX,ballVY;

  public Engine(){}

  @Override
  public void bindDataService(DataService service){
    data=service;
  }
  
  @Override
  public void init(){
    engineClock = new Timer();
    moveLeft = false;
    moveRight = false;
    moveUp = false;
    moveDown = false;
    command = User.COMMAND.NONE;
    blueVX = 0;
    blueVY = 0;
    redVX = 0;
    redVY = 0;
    int chooseSide = new Random().nextInt(2);
    if (chooseSide == 0){
      ballVX = HardCodedParameters.paletteHeight-0.1;
    }
    else if (chooseSide == 1){
      ballVX = -(HardCodedParameters.paletteHeight-0.1);
    }

    ballVY = 0;
  }

  @Override
  public void start(){
    engineClock.schedule(new TimerTask(){
      public void run() {
        updateSpeedPalette();
        updateCommandPalette();
        updatePositionPalette();
        updateSpeedBall();
        updatePositionBall();
        if (collisionPaletteMainBall(data.getBlue())){
          ballVX = blueVX+10;
          ballVY = blueVY;
          if (data.getSpeed() <= 1.5){
            data.setSpeed(data.getSpeed()*1.1);
          }
          blueVX = 0;
          blueVY = 0;
        }
        if (collisionPaletteMainBall(data.getRed())){
          ballVX = redVX-10;
          ballVY = redVY;
          if (data.getSpeed() <= 1.5){
            data.setSpeed(data.getSpeed()*1.1);
          }
          redVX = 0;
          redVY = 0;
        }
        data.setStepNumber(data.getStepNumber()+1);
      }
    },0,HardCodedParameters.enginePaceMillis);
  }

  @Override
  public void stop(){
    engineClock.cancel();
  }

  @Override
  public void setPlayerCommand(User.COMMAND c){
    if (c==User.COMMAND.LEFT) moveLeft=true;
    if (c==User.COMMAND.RIGHT) moveRight=true;
    if (c==User.COMMAND.UP) moveUp=true;
    if (c==User.COMMAND.DOWN) moveDown=true;

    if (c==User.COMMAND.RLEFT) RmoveLeft=true;
    if (c==User.COMMAND.RRIGHT) RmoveRight=true;
    if (c==User.COMMAND.RUP) RmoveUp=true;
    if (c==User.COMMAND.RDOWN) RmoveDown=true;
  }

  @Override
  public void releasePlayerCommand(User.COMMAND c){
    if (c==User.COMMAND.LEFT) moveLeft=false;
    if (c==User.COMMAND.RIGHT) moveRight=false;
    if (c==User.COMMAND.UP) moveUp=false;
    if (c==User.COMMAND.DOWN) moveDown=false;

    if (c==User.COMMAND.RLEFT) RmoveLeft=false;
    if (c==User.COMMAND.RRIGHT) RmoveRight=false;
    if (c==User.COMMAND.RUP) RmoveUp=false;
    if (c==User.COMMAND.RDOWN) RmoveDown=false;
  }

  private void updateSpeedPalette(){
    blueVX*=HardCodedParameters.friction;
    blueVY*=HardCodedParameters.friction;

    redVX*=HardCodedParameters.friction;
    redVY*=HardCodedParameters.friction;
  }

  private void updateCommandPalette(){
    if (moveLeft) blueVX-=HardCodedParameters.paletteStep;
    if (moveRight) blueVX+=HardCodedParameters.paletteStep;
    if (moveUp) blueVY-=HardCodedParameters.paletteStep;
    if (moveDown) blueVY+=HardCodedParameters.paletteStep;

    if (RmoveLeft) redVX-=HardCodedParameters.paletteStep;
    if (RmoveRight) redVX+=HardCodedParameters.paletteStep;
    if (RmoveUp) redVY-=HardCodedParameters.paletteStep;
    if (RmoveDown) redVY+=HardCodedParameters.paletteStep;
  }

  private void updatePositionPalette(){
    if (data.getBluePosition().x+blueVX <= HardCodedParameters.defaultWidth/6){
      data.setBluePosition(new Position(data.getBluePosition().x+blueVX,data.getBluePosition().y+blueVY));
    }

    if (data.getRedPosition().x+redVX >= 5*(HardCodedParameters.defaultWidth/6)){
      data.setRedPosition(new Position(data.getRedPosition().x+redVX,data.getRedPosition().y+redVY));
    }


  }

  private void updateSpeedBall(){
    if (data.getSpeed() > 1){
      data.setSpeed(data.getSpeed()*0.99);
    }
    ballVX*=data.getSpeed();
    ballVY*=data.getSpeed();
  }

  private void updatePositionBall(){

    if (ballVX >= HardCodedParameters.paletteHeight){
      ballVX = HardCodedParameters.paletteHeight-0.1;
    }
    if (ballVX <= -HardCodedParameters.paletteHeight){
      ballVX = -(HardCodedParameters.paletteHeight-0.1);
    }
    System.out.println(ballVX);
    data.setMainBallPosition(new Position(data.getMainBallPosition().x+ballVX,data.getMainBallPosition().y+ballVY));

  }

  private boolean collisionPaletteMainBall(Palette pal){
    Ball mainBall = data.getMainBall();
    Position NO = pal.getPosition();
    Position NE = new Position(NO.x+pal.getHeight(), NO.y);
    Position SE = new Position(NO.x+pal.getHeight(),NO.y+ pal.getWidth());
    Position SO = new Position(NO.x,NO.y+ pal.getWidth());

    return pointInRectangle(NO, NE, SE, SO, mainBall.getPosition())
            || intersectCercle(mainBall.getPosition(), mainBall.getRayon(), NO, NE)
            || intersectCercle(mainBall.getPosition(), mainBall.getRayon(), NE, SE)
            || intersectCercle(mainBall.getPosition(), mainBall.getRayon(), SE, SO)
            || intersectCercle(mainBall.getPosition(), mainBall.getRayon(), SO, NO);
  }

  private double crossproduct(Position p, Position q, Position s, Position t){
    return (((q.x-p.x)*(t.y-s.y))-((q.y-p.y)*(t.x-s.x)));
  }

  public double dotProd(Position p, Position q, Position s, Position t){
    return ((q.x-p.x)*(t.x-s.x)+(q.y-p.y)*(t.y-s.y));
  }

  /**
   * Soit AB une droite et C quelconque
   */
  public static Position getPerpendicular(Position A, Position B, Position C){
    double vx = B.x-A.x; //x du vecteur AB
    double vy = B.y-A.y; //y du vecteur AB
    double ab2 = vx*vx + vy*vy; //norme au carré de AB
    double u = ((C.x - A.x) * vx + (C.y - A.y) * vy) / (ab2);
    return new Position ((int)Math.round(A.x + u * vx), (int)Math.round(A.y + u * vy)); //D appartient à AB
  }

  public boolean intersectCercle(Position centreCercle, double rayonCercle, Position A, Position B ){
    Position intersectPoint = getPerpendicular(A, B, centreCercle);
    if (0 <= dotProd(A, B, A, intersectPoint) && dotProd(A, B, A, intersectPoint) <= dotProd(A, B, A, B)){
      if (centreCercle.distance(intersectPoint) <= rayonCercle){
        return true;
      }
    }

    return false;
  }

  /**
   * Soit ABCD les points du rectangle et P le centre du cercle et rayon le rayon du cercle
   */
  private boolean pointInRectangle(Position A, Position B, Position C, Position D, Position P){
    // 0 ≤ AP·AB ≤ AB·AB and 0 ≤ AP·AD ≤ AD·AD
    double firstCP = dotProd(A, P, A, B);
    double firstComp = dotProd(A, B, A, B);

    double secondCP = dotProd(A, P, A, D);
    double secondComp = dotProd(A, D, A, D);

    return (0 <= firstCP && firstCP <= firstComp) && (0 <= secondCP && secondCP <= secondComp);

  }
}
