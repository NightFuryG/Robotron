class Room {
  PVector position;
  int height;
  int width;

  /*
  Represents the playable room for the game.
  */
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

  void printDetails(){
    System.out.println("Left Edge: " + position.x);
    System.out.println("Right Edge: " + (position.x + width));
    System.out.println("Top Edge: " + position.y);
    System.out.println("Botoom Edge: " + (position.y + height));
  }
}
