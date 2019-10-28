class BombPowerUp extends PowerUp {

  BombPowerUp(float x, float y) {
    super(x, y);
  }

  void display(){
    fill(111,111,111);
    circle(this.position.x, this.position.y, this.size);
  }

  void draw(){
    display();
  }
}
