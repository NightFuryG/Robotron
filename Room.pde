class Room {
  PVector position;
  int height;
  int width;

  Room(float x, float y, float width, float height) {
    this.position = new PVector(x, y);
    this.width = (int) width;
    this.height = (int) height;
  }

  void draw() {
    fill(255);
    noStroke();
    rect(position.x, position.y, width, height);
  }
}
