class BombPowerUp extends PowerUp {

  BombPowerUp(float x, float y) {
    super(x, y);
  }

  void display(){
    fill(0, 153, 153);
    circle(this.position.x, this.position.y, this.size);
    fill(0);
    textAlign(CENTER, CENTER);
    text('B', position.x, position.y);
  }

  void draw(){
    display();
  }
}
