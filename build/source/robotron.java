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

Map map;
Player player;
boolean w, a, s, d;
ArrayList<Bullet> bullets;
ArrayList<Human> family;



public void setup () {
  
  cursor(CROSS);
  
  map = new Map();
  player = spawnPlayer();
  w = a = s = d = false;
  bullets = new ArrayList();
  family = new ArrayList();
  spawnFamily();
  System.out.println(family.size());

}

public void draw () {
  background(0);
  map.draw();
  ensurePlayerInArea();
  playerMove();
  player.draw();
  removeMissedBullets();
  drawBullets();
  drawFamily();
}

public void spawnFamily(){

  int randomRoomIndex;
  ArrayList<Integer> selectedRooms = new ArrayList();
  int humanCount = 0;
  int boundarySpace = displayWidth/HUMAN_RADIUS_PROPORTION;

  while(humanCount < 3) {

    randomRoomIndex = map.randomRoomIndex();

    if (!selectedRooms.contains(randomRoomIndex)) {
      Room randomRoom = map.rooms.get(randomRoomIndex);

      float x1 = (randomRoom.position.x+(2*boundarySpace));

      System.out.println("Random.position.x " + randomRoom.position.x);
      System.out.println("2*HUMAN_RADIUS " + (2*boundarySpace));
      System.out.println(x1);
      float x2 = (randomRoom.position.x+randomRoom.width-(4*boundarySpace));

      float y1 = (randomRoom.position.y+(2*boundarySpace));
      float y2 = (randomRoom.position.y+randomRoom.height-(4*boundarySpace));

      float randomX = random(x1, x2);
      float randomY = random(y1, y2);

      PVector randomPointInRoom = new PVector(randomX, randomY);

      randomRoom.printDetails();
      System.out.println("randomX: " + randomX + " " + "randomY: " + randomY);
          System.out.println();

      spawnFamilyMember(humanCount, randomPointInRoom);
      humanCount++;
      selectedRooms.add(randomRoomIndex);

    }

  }
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
  bullets.add(new Bullet(player.position.x, player.position.y, mouseX, mouseY));
  System.out.println(bullets.size());
}

public void playerMove() {
  if(w) {
    if(checkNotBlack(getUpColor())){
      player.move(1);
    }
  }
  if(s) {
    if(checkNotBlack(getDownColor())){
    player.move(2);
    }
  }
  if(d) {
    if(checkNotBlack(getRightColor())) {
      player.move(3);
    }
  }
  if(a) {
    if(checkNotBlack(getLeftColor())) {
      player.move(4);
    }
  }
}

public void ensurePlayerInArea(){

  int cornerBounce = player.playerSize*10;

  if(!checkNotBlack(getLeftColor())) {
    if (player.velocity.x < 0) {
      player.velocity.x = -player.velocity.x;
    }
    else if (player.velocity.x >= 0) {
      player.velocity.x = cornerBounce;
    }
  }
  if(!checkNotBlack(getRightColor())){
    if (player.velocity.x > 0) {
      player.velocity.x = -player.velocity.x;
    } else if (player.velocity.x <= 0) {
        player.velocity.x = -cornerBounce;
    }
  }
  if(!checkNotBlack(getUpColor())){
    if (player.velocity.y < 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y >= 0) {
        player.velocity.y = cornerBounce;
    }
  }
  if(!checkNotBlack(getDownColor())){
    if (player.velocity.y > 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y <= 0) {
      player.velocity.y = -cornerBounce;
    }
  }
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

public boolean checkWhite(int inColor) {
  return inColor == WHITE;
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

public void removeMissedBullets() {
  for(Bullet bullet : new ArrayList<Bullet>(bullets)) {
    int detectedColor = get((int) bullet.position.x, (int) bullet.position.y);
    if(!checkNotBlack(detectedColor)) {
      bullets.remove(bullet);
    }
  }
}
class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/5;
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
      roomSize = new PVector(random(3 * partition.width/4, partition.width - partition.width/8), random(3 * partition.height/4, partition.height - partition.height/8));
      roomPosition = new PVector(random(partition.position.x + CORRIDOR_SIZE, partition.width - roomSize.x), random(partition.position.y + CORRIDOR_SIZE, partition.height - roomSize.y));
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

  final int PLAYER_SPEED = displayWidth/750;
  final int PLAYER_RADIUS = displayWidth/50;

  final PVector NORTH = new PVector(0,-PLAYER_SPEED),
                SOUTH = new PVector(0, PLAYER_SPEED),
                EAST =  new PVector(PLAYER_SPEED, 0),
                WEST = new PVector(-PLAYER_SPEED, 0);


  PVector position;
  PVector velocity;
  int playerSize;
  int playerSpeed;

  Player(int x, int y) {
    this.position = new PVector(x, y);
    this.velocity = new PVector(0,0);
    this.playerSize = PLAYER_RADIUS;
    this.playerSpeed = PLAYER_SPEED;
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
