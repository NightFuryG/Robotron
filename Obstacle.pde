class Obstacle {

  final int OBSTACLE_SIZE = 50;
  final float ROTATION_SPEED = 0.1f;

  PVector position;
  int size;
  float theta;
  float spin;

  Obstacle(float x, float y){
    this.position = new PVector(x,y);
    this.size = displayWidth/OBSTACLE_SIZE;
    this.spin = ROTATION_SPEED;
    this.theta = 0;
  }

  void update(){
    this.theta += this.spin;
  }

  void display(){
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

  void draw(){
    update();
    display();
  }
}
