class Partition {
  PVector position;
  int height;
  int width;

  Partition(float x, float y, int width, int height) {
    this.position = new PVector(x, y);
    this.width = width;
    this.height = height;
  }


  void draw() {
    stroke(155);
    rect(position.x, position.y, width, height);
  }
}
