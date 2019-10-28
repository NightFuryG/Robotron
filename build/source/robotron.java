import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class robotron extends PApplet {

final int BLACK = color(0),
            WHITE = color(255);

final int HUMAN_RADIUS_PROPORTION = 50;

final int TEXT_POSITION = 50,
          TEXT_SIZE = 100;

Map map;
Player player;
boolean w, a, s, d;
ArrayList<Bullet> bullets;
ArrayList<Human> family;
ArrayList<Obstacle> obstacles;
ArrayList<Robot> robots;
ArrayList<PVector> spawns;
int score;
int size;
int wave;
boolean alive;
boolean startScreen;


public void setup () {
  
  cursor(CROSS);
  
  map = new Map();
  player = spawnPlayer();
  w = a = s = d = false;
  score = 0;
  wave = 0;
  size = displayWidth/HUMAN_RADIUS_PROPORTION;
  alive = true;
  startScreen = true;
  bullets = new ArrayList();
  family = new ArrayList();
  obstacles = new ArrayList();
  robots = new ArrayList();
  spawns = new ArrayList();
  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  checkRooms();
}

public void draw () {
    background(0);
    pushStyle();
    textAlign(CENTER);
    fill(255);
    textSize(displayWidth/TEXT_SIZE);
    text("Wave: " + wave, displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Lives: " + player.lives, 3.3f * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Score: " + score, 5.7f * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));

    popStyle();

    if(alive) {
      if(startScreen) {
        pushStyle();
        textAlign(CENTER);
        textSize(64);
        fill(0 ,255, 255);
        text("ROBOTRON", displayWidth/2, displayHeight/2);
        textSize(24);
        text("Click to start", displayWidth/2, 3*displayHeight/4);
        popStyle();
      } else {
        map.draw();
        ensurePlayerInArea();
        playerMove();
        player.draw();
        removeMissedBullets();
        drawBullets();
        drawFamily();
        drawObstacles();
        drawRobots();
        detectPlayerFamilyCollision();
        detectPlayerObstacleCollision();
        detectBulletCollision();
        newWave();
        alive = checkNotDead();
      }
    } else {
      pushStyle();
      textAlign(CENTER);
      textSize(64);
      fill(255, 0 ,255);
      text("GAME OVER", displayWidth/2, displayHeight/2);
      textSize(24);
      text("Click to Restart", displayWidth/2, 3*displayHeight/4);
      popStyle();
    }
}

public void newWave(){
  if(checkWaveEnd()) {
    delay(300);
    map = new Map();
    reset();
    wave++;
  }
}

public void reset(){
  bullets.clear();
  family.clear();
  obstacles.clear();
  robots.clear();
  spawns.clear();
  player = spawnPlayer();
  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  alive = true;
}

public void newGame(){
  map = new Map();
  player.lives = 3;
  score = 0;
  wave = 0;
  reset();
  alive = true;

}

public boolean checkNotDead(){
  return (player.lives > 0 ? true : false);
}



public Player spawnPlayer() {
  Room firstRoom = map.rooms.get(0);
  int startX = (int) firstRoom.position.x + firstRoom.width/2;
  int startY = (int) firstRoom.position.y + firstRoom.height/2;
  return new Player(startX, startY);
}

public void keyPressed() {
    if(key == 'w') {
      w = true;
    } else if (key == 's') {
      s = true;
    } else if (key == 'd') {
      d = true;
    } else if (key == 'a') {
      a = true;
    }
}

public void keyReleased() {
  if(key == 'w') {
    w = false;
  } else if (key == 's') {
    s = false;
  } else if (key == 'd') {
    d = false;
  } else if (key == 'a') {
    a = false;
  }
}

public void mousePressed(){
  if(!startScreen) {
    if(alive) {
      bullets.add(new Bullet(player.position.x, player.position.y, mouseX, mouseY));
    } else {
      newGame();
    }
  } else {
    startScreen = false;
  }


}

public void playerMove() {
  if(w) {
    if(checkNotBlack(getUpColor()) && !checkTopEdge()){
      player.move(1);
    }
  }
  if(s) {
    if(checkNotBlack(getDownColor()) && !checkBottomEdge()){
    player.move(2);
    }
  }
  if(d) {
    if(checkNotBlack(getRightColor()) && !checkRightEdge()) {
      player.move(3);
    }
  }
  if(a) {
    if(checkNotBlack(getLeftColor()) && !checkLeftEdge()) {
      player.move(4);
    }
  }
}

public void ensurePlayerInArea(){

  int cornerBounce = player.playerSize*10;

  if(!checkNotBlack(getLeftColor()) || checkLeftEdge()) {
    if (player.velocity.x < 0) {
      player.velocity.x = -player.velocity.x;
    }
    else if (player.velocity.x >= 0) {
      player.velocity.x = cornerBounce;
    }
  }
  if(!checkNotBlack(getRightColor()) || checkRightEdge()){
    if (player.velocity.x > 0) {
      player.velocity.x = -player.velocity.x;
    } else if (player.velocity.x <= 0) {
        player.velocity.x = -cornerBounce;
    }
  }
  if(!checkNotBlack(getUpColor()) || checkTopEdge()){
    if (player.velocity.y < 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y >= 0) {
        player.velocity.y = cornerBounce;
    }
  }
  if(!checkNotBlack(getDownColor()) || checkBottomEdge()){
    if (player.velocity.y > 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y <= 0) {
      player.velocity.y = -cornerBounce;
    }
  }
}



public boolean checkBottomEdge() {
  int downY= (int) player.position.y + player.playerSize/2;
  return downY >= displayHeight;
}

public boolean checkLeftEdge(){
  int leftX = (int) player.position.x - player.playerSize/2;
  return leftX <= 0;
}

public boolean checkRightEdge(){
  int rightX = (int) player.position.x + player.playerSize/2;
  return rightX >= displayWidth;
}

public boolean checkTopEdge(){
  int topY = (int) player.position.y - player.playerSize/2;
  return topY <= 0;

}

public int getLeftColor() {
  int leftX = (int) player.position.x - player.playerSize/2;
  int leftY = (int) player.position.y;
  int leftColor = get(leftX, leftY);
  return leftColor;
}

public int getRightColor() {
  int rightX = (int) player.position.x + player.playerSize/2;
  int rightY = (int) player.position.y;
  int rightColor = get(rightX, rightY);
  return rightColor;
}

public int getUpColor() {
  int upX = (int) player.position.x;
  int upY = (int) player.position.y - player.playerSize/2;
  int upColor = get(upX, upY);
  return upColor;
}

public int getDownColor() {
  int downX = (int) player.position.x;
  int downY= (int) player.position.y + player.playerSize/2;
  int downColor = get(downX, downY);
  return downColor;
}

public boolean checkNotBlack(int inColor){
  return inColor != BLACK;
}


public void drawBullets() {
  for(Bullet bullet : bullets) {
    bullet.draw();
  }
}

public void drawFamily() {
  for(Human human : family) {
    human.draw();
  }
}

public boolean checkWaveEnd(){
  return (robots.size() == 0 ? true : false);
}

public void removeMissedBullets() {
  for(Bullet bullet : new ArrayList<Bullet>(bullets)) {
    int detectedColor = get((int) bullet.position.x, (int) bullet.position.y);
    if(!checkNotBlack(detectedColor)) {
      bullets.remove(bullet);
    }
  }
}

public void spawnFamilyAndSeekBots(){

  int randomRoomIndex;
  ArrayList<Integer> selectedRooms = new ArrayList();
  int humanCount = 0;

  while(humanCount < 3) {

    randomRoomIndex = map.randomRoomIndex();

    if (!selectedRooms.contains(randomRoomIndex)) {


      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      PVector seekBotSpawnPoint = inverseRandomPointInRoom(randomRoomIndex, randomPointInRoom);

      if(checkSpawnLocation(randomPointInRoom) && checkSpawnLocation(seekBotSpawnPoint)) {
        spawnFamilyMember(humanCount, randomPointInRoom);

        if(checkCentralRoomSpawn(randomPointInRoom, randomRoomIndex)) {
          robots.add(spawnSeekBot(seekBotSpawnPoint));
          spawns.add(seekBotSpawnPoint);
        } else {
          robots.add(spawnDefaultSeekBot(randomRoomIndex));
        }
        humanCount++;
        selectedRooms.add(randomRoomIndex);
        spawns.add(randomPointInRoom);

      }

    }

  }
}

public SeekBot spawnSeekBot(PVector seekBotSpawnPoint) {
  return new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2);
}

public SeekBot spawnDefaultSeekBot(int randomRoomIndex){
  int spawnRadius = size;
  Room room = map.rooms.get(randomRoomIndex);
  float roomX = room.position.x + spawnRadius;
  float roomY = room.position.y + spawnRadius;
  PVector spawnLocation = new PVector(roomX, roomY);
  spawns.add(spawnLocation);

  return spawnSeekBot(spawnLocation);

}

public boolean checkCentralRoomSpawn(PVector spawnLocation, int randomRoomIndex) {
  float spawnRadius = displayWidth/HUMAN_RADIUS_PROPORTION;
  Room room = map.rooms.get(randomRoomIndex);
  PVector spawn = new PVector(room.position.x + room.width/2, room.position.y + room.height/2);

  if(spawnLocation.x > spawn.x - spawnRadius && spawnLocation.x < spawn.x + spawnRadius) {
    if(spawnLocation.y > spawn.y - spawnRadius && spawnLocation.y < spawn.y + spawnRadius) {
      return false;
    }
  }
  return true;
}




public boolean checkSpawnLocation(PVector spawnLocation) {
  float spawnRadius = displayWidth/(HUMAN_RADIUS_PROPORTION/2);

  if(spawns.size() == 0) {
    return true;
  }

  for(PVector spawn : spawns) {
    if(spawnLocation.x > spawn.x - spawnRadius && spawnLocation.x < spawn.x + spawnRadius) {
      if(spawnLocation.y > spawn.y - spawnRadius && spawnLocation.y < spawn.y + spawnRadius) {
        return false;
      }
    }
  }
  return true;
}


public PVector inverseRandomPointInRoom(int index, PVector familyPosition) {

  Room room = map.rooms.get(index);

  float inverseX = 2*room.position.x + room.width - familyPosition.x;
  float inverseY = 2*room.position.y + room.height - familyPosition.y;

  PVector seekBotSpawnPosition = new PVector(inverseX, inverseY);

  return seekBotSpawnPosition;
}

public void spawnFamilyMember(int i, PVector randomPointInRoom){
    switch(i) {
      case 0:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'F'));
        break;
      case 1:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'M'));
        break;
      case 2:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'C'));
        break;
      default:
        break;
    }
}

//ADDSCORE
public void detectPlayerFamilyCollision(){
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  for(Human human : new ArrayList<Human>(family)) {
    float humanX = human.position.x;
    float humanY = human.position.y;
    int humanRadius = human.humanSize;
    if(dist(playerX, playerY, humanX, humanY) < playerRadius/2 + humanRadius/2) {
      if(human.member == 'F') {
        score += 1000;
      }
      family.remove(human);
    }
  }
}

public void detectPlayerObstacleCollision() {
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  if(obstacles.size() > 0) {
    playerObstacleCollision(playerX, playerY, playerRadius);
  }
  if(robots.size() > 0) {
    playerRobotCollision(playerX, playerY, playerRadius);
  }
}

public void playerObstacleCollision(float playerX, float playerY, int playerSize){
  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    float obstacleX = obstacle.position.x;
    float obstacleY = obstacle.position.y;
    int obstacleSize = obstacle.size;

    if(playerX > obstacleX - obstacleSize && playerX < obstacleX + obstacleSize) {
      if(playerY > obstacleY - obstacleSize && playerY < obstacleY + obstacleSize) {
        obstacles.remove(obstacle);
        player.lives--;
      }
    }
  }
}

public void playerRobotCollision(float playerX, float playerY, int playerSize){
  for(Robot robot : new ArrayList<Robot>(robots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(playerX - playerSize/2 < robotX + robotSize && playerX + playerSize/2 > robotX) {
      if(playerY - playerSize/2 < robotY + robotSize && playerY + playerSize/2 > robotY) {
        robots.remove(robot);
        player.lives--;
      }
    }
  }
}

public void detectBulletCollision(){
  for(Bullet bullet : new ArrayList<Bullet>(bullets)){


    if(obstacles.size() > 0) {
      bulletObstacleCollision(bullet);
    }

    if(robots.size() > 0) {
      bulletRobotCollision(bullet);
    }
  }
}

public void bulletObstacleCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    float obstacleX = obstacle.position.x;
    float obstacleY = obstacle.position.y;
    int obstacleSize = obstacle.size;

    if(bulletX > obstacleX - obstacleSize && bulletX < obstacleX + obstacleSize) {
      if(bulletY > obstacleY - obstacleSize && bulletY < obstacleY + obstacleSize) {
        obstacles.remove(obstacle);
        bullets.remove(bullet);
      }
    }
  }

}

public void bulletRobotCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(Robot robot : new ArrayList<Robot>(robots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(bulletX > robotX - robotSize && bulletX < robotX + robotSize) {
      if(bulletY > robotY - robotSize && bulletY < robotY + robotSize) {
        robots.remove(robot);
        bullets.remove(bullet);
      }
    }
  }
}

public void spawnObstacles(){
  int randomRoomIndex;
  int obstacleCount = 0;

  while(obstacleCount < 10) {

      randomRoomIndex = map.randomRoomIndex();
      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      if(checkSpawnLocation(randomPointInRoom)) {
        spawns.add(randomPointInRoom);
        obstacles.add(new Obstacle(randomPointInRoom.x, randomPointInRoom.y));
        obstacleCount++;
       }
    }
}

public void spawnRobots() {
  int randomRoomIndex;
  int robotCount = 0;

  while(robotCount < 10) {

      randomRoomIndex = map.randomRoomIndex();
      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      if(checkSpawnLocation(randomPointInRoom)) {
        if(robotCount % 2 == 0) {
          robots.add(new MeleeBot(randomPointInRoom.x, randomPointInRoom.y));
          spawns.add(randomPointInRoom);
          robotCount++;
        } else {
          robots.add(new RangedBot(randomPointInRoom.x, randomPointInRoom.y));
          spawns.add(randomPointInRoom);
          robotCount++;
        }


    }
  }
}

public void drawRobots(){
  for(Robot robot : robots) {
    robot.draw();
  }
}



public PVector randomPointInRoom(int randomRoomIndex) {
  int boundarySpace = displayWidth/HUMAN_RADIUS_PROPORTION;
  Room randomRoom = map.rooms.get(randomRoomIndex);

  float x1 = (randomRoom.position.x +(boundarySpace));
  float x2 = (randomRoom.position.x + randomRoom.width - (2 * boundarySpace));

  float y1 = (randomRoom.position.y + (boundarySpace));
  float y2 = (randomRoom.position.y + randomRoom.height - (2 * boundarySpace));

  float randomX = random(x1, x2);
  float randomY = random(y1, y2);

  return new PVector(randomX, randomY);
}

public void drawObstacles() {
  for(Obstacle obstacle : obstacles) {
    obstacle.draw();
  }
}

public void printSpawns() {
  for(PVector spawn : spawns) {
    System.out.println(spawn);
  }
}

public void checkRooms() {
  for(Room room : map.rooms) {
    if(room.position.x + room.width > displayWidth) {
      System.out.println("bad x");
    }
    if(room.position.y + room.height > displayHeight) {
      System.out.println("bad y");
    }
  }
}
class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/6;
  final int CORRIDOR_SIZE = displayWidth/30;

  Partition partition;
  BSPNode leftChild;
  BSPNode rightChild;
  ArrayList<Room> corridors;


  BSPNode(Partition partition) {
    this.partition = partition;
    this.leftChild = null;
    this.rightChild = null;
  }

  public boolean split() {
    //split already occurred
    if(leftChild != null || rightChild != null) {
      return false;
    }

    boolean splitHorizontal = randomBoolean();

    if(partition.width > partition.height && partition.width / partition.height >=1.25f) {
      splitHorizontal = false;
    } else if(partition.height > partition.width && partition.height / partition.width >= 1.25f) {
      splitHorizontal = true;
    }

    int max = (splitHorizontal ? partition.height : partition.width) - MIN_PARTITION_SIZE;

    if(max <= MIN_PARTITION_SIZE) {
      return false;
    }

    int splitLocation = (int) random(MIN_PARTITION_SIZE, max);

    if(splitHorizontal) {
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, partition.width, splitLocation));
      this.rightChild = new BSPNode(new Partition(partition.position.x, partition.position.y + splitLocation, partition.width, partition.height - splitLocation));
    } else {
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, splitLocation, partition.height));
      this.rightChild = new BSPNode(new Partition(partition.position.x + splitLocation, partition.position.y, partition.width - splitLocation, partition.height));
    }
    return true;
  }

  public void createRooms() {

    if(leftChild != null || rightChild != null) {
      if(leftChild != null) {
        leftChild.createRooms();
      }
      if(rightChild != null) {
        rightChild.createRooms();
      }
      if(leftChild != null && rightChild != null) {
        createCorridor(leftChild.getRoom(), rightChild.getRoom());
      }


    } else {
      PVector roomSize;
      PVector roomPosition;
      roomSize = new PVector(random(0.75f * partition.width, 0.9f * partition.width), random(0.75f * partition.height, 0.9f * partition.height));
      roomPosition = new PVector(random(partition.position.x + 0.1f * partition.width, partition.position.x + 0.9f * partition.width - roomSize.x), random(partition.position.y + 0.1f * partition.height, partition.position.y + 0.9f * partition.height - roomSize.y));
      partition.room = new Room(roomPosition.x, roomPosition.y, roomSize.x, roomSize.y);
    }
  }

  public Room getRoom() {
    if(partition.room != null) {
      return partition.room;
    } else {

      Room leftRoom = null;
      Room rightRoom = null;

      if(leftChild != null) {
        leftRoom = leftChild.getRoom();
      }
      if(rightChild != null) {
        rightRoom = rightChild.getRoom();
      }
      if(leftRoom == null && rightRoom == null) {
        return null;
      } else if (rightRoom == null) {
        return leftRoom;
      } else if (leftRoom == null) {
        return rightRoom;
      } else if (randomBoolean()) {
        return leftRoom;
      } else {
        return rightRoom;
      }
    }
  }

  public void createCorridor(Room leftRoom, Room rightRoom) {
    corridors = new ArrayList();

    PVector pointA = new PVector(random(leftRoom.position.x + CORRIDOR_SIZE, leftRoom.position.x + leftRoom.width - 2*CORRIDOR_SIZE),
     random(leftRoom.position.y + CORRIDOR_SIZE, leftRoom.position.y + leftRoom.height - 2*CORRIDOR_SIZE));
    PVector pointB = new PVector(random(rightRoom.position.x + CORRIDOR_SIZE, rightRoom.position.x + rightRoom.width - 2*CORRIDOR_SIZE),
     random(rightRoom.position.y + CORRIDOR_SIZE, rightRoom.position.y + rightRoom.height - 2*CORRIDOR_SIZE));

    float w = pointB.x - pointA.x;
    float h = pointB.y - pointA.y;

    if(w < 0) {
      if(h < 0) {
        if(randomBoolean()) {
          corridors.add(new Room(pointB.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        }
      } else if (h > 0) {
        if(randomBoolean()) {
          corridors.add(new Room(pointB.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        }
      } else {
        corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
      }
    } else if (w > 0) {
        if (h < 0) {
          if (randomBoolean()){
            corridors.add(new Room(pointA.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          }
      } else if (h > 0) {
          if (randomBoolean()) {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          } else {
            corridors.add(new Room(pointA.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          }
        } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
        }
    } else {
      if (h < 0) {
        corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
      } else if (h > 0) {
        corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
      }
    }
  }




  public boolean randomBoolean() {
    return random(1) > 0.5f;
  }
}
class BSPTree {

  final int MAX_PARTITION_SIZE = displayWidth/2;

  ArrayList<BSPNode> nodes;

  BSPTree() {
    nodes = new ArrayList();
    Partition base = new Partition(0,0, displayWidth, displayHeight);
    nodes.add(new BSPNode(base));
    generateNodes();
  }

  public void generateNodes() {
    boolean split = true;

    while(split) {
      split = false;
      for(BSPNode node : new ArrayList<BSPNode>(nodes)) {
        if(node.leftChild == null && node.rightChild == null) {
          if(node.partition.width > MAX_PARTITION_SIZE || node.partition.height > MAX_PARTITION_SIZE ||random75() ) {
            if(node.split()){
              nodes.add(node.leftChild);
              nodes.add(node.rightChild);
              split = true;
            }
          }
        }
      }
    }

    nodes.get(0).createRooms();
  }




  public void printNodes() {
    for(BSPNode node : nodes) {
      System.out.println(node.partition.position + " " + node.partition.height + " " + node.partition.width );
      System.out.println();
    }
  }


  public boolean random75() {
    return random(1) > 0.25f;
  }


}
class Bullet {

  final int topSpeed = displayWidth/300;
  final int bulletSize = displayWidth/300;

  PVector position;
  PVector destination;
  PVector direction;
  PVector velocity;
  PVector acceleration;

  Bullet(float startX, float startY, int endX, int endY) {
    this.position = new PVector(startX, startY);
    this.destination = new PVector(endX, endY);
    this.velocity = new PVector(0,0);
    this.direction = calculateDirection();
    this.acceleration = calculateAcceleration();


  }

  public PVector calculateDirection() {
    return PVector.sub(destination, position);
  }

  public PVector calculateAcceleration() {
    PVector a = this.direction.normalize();
    a = this.direction.mult(0.5f);
    return a;
  }

  public void checkWalls() {

  }

  public void update(){
    velocity.add(acceleration);
    velocity.limit(topSpeed);
    position.add(velocity);
  }

  public void display(){
    fill(255,0,0);
    circle(position.x, position.y, bulletSize);
  }

  public void draw(){
    update();
    display();
  }
}
class Human {

  final int HUMAN_SIZE = displayWidth/50;

  PVector position;
  int humanSize;
  char member;

  Human(float x, float y, char member){
    this.position = new PVector(x, y);
    this.humanSize = HUMAN_SIZE;
    this.member = member;
  }


  public void update(){

  }

  public void display(){
    fill(255,0,0);
    circle(position.x, position.y, humanSize);
    fill(255);
    textAlign(CENTER, CENTER);
    text(member, position.x, position.y);
  }

  public void draw() {
    update();
    display();
  }

}
class Line {
  PVector start;
  PVector end;

  Line(float x1, float y1, float x2, float y2) {
    start = new PVector(x1, y1);
    end = new PVector(x2, y2);
  }
}
class Map {

  BSPTree tree;
  ArrayList<Room> rooms;
  ArrayList<Room> corridors;

  Map() {
    tree = new BSPTree();
    rooms = new ArrayList();
    corridors = new ArrayList();
    addRooms();
  }

  public void draw(){
    for(Room room : rooms) {
      if(rooms.get(0) == room) {
        pushStyle();
        strokeWeight(8);
        stroke(255, 0, 0);
        rect(room.position.x, room.position.y, room.width, room.height);
        popStyle();
      }
      room.draw();
    }
    for(Room corridor : corridors) {
      corridor.draw();
    }
  }

  public void addRooms(){
    for(BSPNode node : tree.nodes) {
      if(node.partition.room != null) {
        rooms.add(node.partition.room);
      }
      if(node.corridors != null) {
        corridors.addAll(node.corridors);
      }
    }
  }

  public int randomRoomIndex() {
    return (int) random(1, rooms.size());
  }

  //useful printmethod;
  public void printRooms(ArrayList<Room> roomList){
    for(Room room : roomList) {
      System.out.println(room.position + " " + room.width + " " + room.height);
    }
    System.out.println();
  }
}
class MeleeBot extends Robot {

  MeleeBot(float x, float y) {
    super(x , y);
  }

  public void update() {

  }

  public void display() {
    fill(255, 255, 0);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(){
    display();
  }

}
class Obstacle {

  final int OBSTACLE_SIZE = 50;
  final float ROTATION_SPEED = 0.1f;

  PVector position;
  int size;
  float theta;
  float spin;

  Obstacle(float x, float y){
    this.size = displayWidth/OBSTACLE_SIZE;
    this.position = new PVector(x + size/2,y + size/2);
    this.spin = ROTATION_SPEED;
    this.theta = 0;
  }

  public void update(){
    this.theta += this.spin;
  }

  public void display(){
    pushStyle();
    rectMode(CENTER);
    fill( 0,0,255);

    pushMatrix();
    translate(position.x, position.y);
    rotate(theta);
    rect(0,0,size, size);
    popMatrix();
    popStyle();
  }

  public void draw(){
    update();
    display();
  }
}
class Partition {
  PVector position;
  int height;
  int width;
  Room room;

  Partition(float x, float y, int width, int height) {
    this.position = new PVector(x, y);
    this.width = width;
    this.height = height;
    this.room = null;
  }

  public void setRoom(Room room) {
    this.room = room;
  }

  public void draw() {
    fill(255);
    if(room != null) {
      room.draw();
    }
    }
}
class Player {

  final int PLAYER_SPEED = displayWidth/750,
            PLAYER_RADIUS = displayWidth/50,
            PLAYER_LIVES = 3;



  PVector position;
  PVector velocity;
  int playerSize;
  int playerSpeed;
  int lives;

  Player(int x, int y) {
    this.position = new PVector(x, y);
    this.velocity = new PVector(0,0);
    this.playerSize = PLAYER_RADIUS;
    this.playerSpeed = PLAYER_SPEED;
    this.lives = PLAYER_LIVES;
  }

  public void move(int i) {
    switch (i) {
      case 1:
        velocity.y -= PLAYER_SPEED;
        break;
      case 2:
        velocity.y += PLAYER_SPEED;
        break;
      case 3:
        velocity.x += PLAYER_SPEED;
        break;
      case 4:
        velocity.x -= PLAYER_SPEED;
        break;
      default:
        break;
    }
  }

  public void update() {
    velocity.limit(2*PLAYER_SPEED);
    velocity.x *= 0.90f;
    velocity.y *= 0.90f;
    position.add(velocity);

  }

  public void display() {
    fill(255,0,0);
    circle(position.x, position.y, playerSize);
  }


  public void draw(){
    update();
    display();
  }
}
class RangedBot extends Robot {

  RangedBot(float x, float  y) {
    super(x, y);
  }

  public void display() {
    fill( 0, 255, 0);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(){
    display();
  }
}
class Robot {
  final int ROBOT_SIZE = 50;

  PVector position;
  PVector velocity;
  int size;

  Robot(float x, float y) {
    this.position = new PVector(x, y);
    this.velocity = new PVector(0,0);
    this.size = displayWidth/ROBOT_SIZE;
  }

  public void draw () {

  }
  
}
class Room {
  PVector position;
  int height;
  int width;

  Room(float x, float y, float width, float height) {
    this.position = new PVector(x, y);
    this.width = (int) width;
    this.height = (int) height;
  }

  public void draw() {
    fill(255);
    noStroke();
    rect(position.x, position.y, width, height);
  }

  public void printDetails(){
    System.out.println("Left Edge: " + position.x);
    System.out.println("Right Edge: " + (position.x + width));
    System.out.println("Top Edge: " + position.y);
    System.out.println("Botoom Edge: " + (position.y + height));
  }
}
class SeekBot extends Robot {

  SeekBot (float x, float  y) {
    super(x, y);
  }

  public void update(){}

  public void display() {
    fill(255, 0, 255);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(){
    display();
  }
}
  public void settings() {  fullScreen();  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "robotron" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
