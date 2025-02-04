class Bullet {

  final int topSpeed = displayWidth/300;
  final int bulletSize = displayWidth/300;

  PVector position;
  PVector destination;
  PVector direction;
  PVector velocity;
  PVector acceleration;
  boolean enemy;

  //Bullet act as a simple projectile towards a target
  Bullet(float startX, float startY, float endX, float endY, boolean enemy) {
    this.position = new PVector(startX, startY);
    this.destination = new PVector(endX, endY);
    this.velocity = new PVector(0,0);
    this.direction = calculateDirection();
    this.acceleration = calculateAcceleration();
    this.enemy = enemy;
  }

  //calculate direction of travel
  PVector calculateDirection() {
    return PVector.sub(destination, position);
  }

  //calculate acceleration
  PVector calculateAcceleration() {
    PVector a = this.direction.normalize();
    a = this.direction.mult(0.5);
    return a;
  }

  void update(){
    velocity.add(acceleration);
    velocity.limit(topSpeed);
    position.add(velocity);
  }

  void display(){
    stroke(100);
    if(enemy) {
      fill(255, 0, 255);
    } else {
      fill(0,255,255);
    }

    circle(position.x, position.y, bulletSize);
  }

  void draw(){
    update();
    display();
  }
}
