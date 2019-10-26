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

  PVector calculateDirection() {
    return PVector.sub(destination, position);
  }

  PVector calculateAcceleration() {
    PVector a = this.direction.normalize();
    a = this.direction.mult(0.5);
    return a;
  }

  void checkWalls() {

  }

  void update(){
    velocity.add(acceleration);
    velocity.limit(topSpeed);
    position.add(velocity);
  }

  void display(){
    fill(255,0,0);
    circle(position.x, position.y, bulletSize);
  }

  void draw(){
    update();
    display();
  }
}
