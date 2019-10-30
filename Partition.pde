class Partition {
  PVector position;
  int height;
  int width;
  Room room;
  /*
  Represents a space is split into and the area a room may
  be created in
  */
  Partition(float x, float y, int width, int height) {
    this.position = new PVector(x, y);
    this.width = width;
    this.height = height;
    this.room = null;
  }

  void setRoom(Room room) {
    this.room = room;
  }

  void draw() {
    fill(255);
    if(room != null) {
      room.draw();
    }
    }
}
