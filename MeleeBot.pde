class MeleeBot extends Robot {

  MeleeBot(float x, float y) {
    super(x , y);
  }

  void update() {

  }

  void display() {
    fill(255, 255, 0);
    square(this.position.x, this.position.y, this.size);
  }

  void draw(){
    display();
  }

}
