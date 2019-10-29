class RangedBot extends Robot {

  RangedBot(float x, float  y, int roomIndex) {
    super(x, y, roomIndex);
  }

  void update(){
    this.ensureRobotInArea();
    this.wander();
  }

  void display() {
    fill( 0, 255, 0);
    square(this.position.x, this.position.y, this.size);
  }

  void draw(){
    update();
    display();
  }
}
