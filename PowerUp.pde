class PowerUp {

  final int POWERUP_SIZE = 80;

  PVector position;
  int size;

  PowerUp(float x, float y) {
    this.position = new PVector(x, y);
    this.size = displayWidth/POWERUP_SIZE;
  }

  void draw(){}
}
