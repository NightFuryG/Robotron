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

Map map;

public void setup () {
  
  background(0);
  cursor(CROSS);
  map = new Map();
}

public void draw () {
  map.draw();
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

    // if(partition.width > partition.height && partition.width / partition.height >=1.25) {
    //   splitHorizontal = false;
    // } else if(partition.height > partition.width && partition.height / partition.width >= 1.25) {
    //   splitHorizontal = true;
    // }

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
          corridors.add(new Room(pointB.x, pointA.y, abs(w), CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h)));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w), CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h)));
        }
      } else if (h > 0) {
        if(randomBoolean()) {
          corridors.add(new Room(pointB.x, pointA.y, abs(w), CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h)));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w), CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h)));
        }
      } else {
        corridors.add(new Room(pointB.x, pointB.y, abs(w), CORRIDOR_SIZE));
      }
    } else if (w > 0) {
        if (h < 0) {
          if (randomBoolean()){
            corridors.add(new Room(pointA.x, pointB.y, abs(w), CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h)));
          } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w), CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h)));
          }
      } else if (h > 0) {
          if (randomBoolean()) {
            corridors.add(new Room(pointA.x, pointA.y, abs(w), CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h)));
          } else {
            corridors.add(new Room(pointA.x, pointB.y, abs(w), CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h)));
          }
        } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w), CORRIDOR_SIZE));
        }
    } else {
      if (h < 0) {
        corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h)));
      } else if (h > 0) {
        corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h)));
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

    System.out.println(rooms.size());
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
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "robotron" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
