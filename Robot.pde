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

  void draw () {

  }
  
}
