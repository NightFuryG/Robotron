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
  
  map = new Map();
}

public void draw () {
  map.draw();
}
class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/5;


  Partition partition;
  Line line;
  BSPNode left;
  BSPNode right;

  BSPNode(Partition partition) {
    this.partition = partition;
    this.left = null;
    this.right = null;
  }

  public boolean split() {
    //split already occurred
    if(left != null || right != null) {
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
      this.left = new BSPNode(new Partition(partition.position.x, partition.position.y, partition.width, splitLocation));
      this.right = new BSPNode(new Partition(partition.position.x, partition.position.y + splitLocation, partition.width, partition.height - splitLocation));
    } else {
      this.left = new BSPNode(new Partition(partition.position.x, partition.position.y, splitLocation, partition.height));
      this.right = new BSPNode(new Partition(partition.position.x + splitLocation, partition.position.y, partition.width - splitLocation, partition.height));
    }
    return true;


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

    printNodes();

    while(split) {
      split = false;
      for(BSPNode node : new ArrayList<BSPNode>(nodes)) {
        if(node.left == null && node.right == null) {
          if(node.partition.width > MAX_PARTITION_SIZE || node.partition.height > MAX_PARTITION_SIZE ||random75() ) {
            if(node.split()){
              nodes.add(node.left);
              nodes.add(node.right);
              split = true;
            }
          }
        }
      }
    }
  }

  public void printNodes() {
    for(BSPNode node : nodes) {
      System.out.println(node.partition.position + " " + node.partition.height + " " + node.partition.width );
      System.out.println();
    }
  }

  public void draw() {
    for(BSPNode node : nodes) {
      node.partition.draw();
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

  Map() {
    tree = new BSPTree();
    tree.printNodes();
  }

  public void draw(){
    tree.draw();
  }

}
class Partition {
  PVector position;
  int height;
  int width;

  Partition(float x, float y, int width, int height) {
    this.position = new PVector(x, y);
    this.width = width;
    this.height = height;
  }


  public void draw() {
    stroke(155);
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
