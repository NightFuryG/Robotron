class SeekBot extends Robot {

  SeekBot (float x, float  y) {
    super(x, y);
  }

  void update(){}

  void display() {
    fill(255, 0, 255);
    square(this.position.x, this.position.y, this.size);
  }

  void draw(){
    display();
  }
}
