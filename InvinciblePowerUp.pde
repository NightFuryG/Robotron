class InvinciblePowerUp extends PowerUp {

  InvinciblePowerUp(float x, float y) {
    super(x, y);
  }

  void display() {
    fill(222,222,222);
    circle(this.position.x, this.position.y, this.size);
  }

  void draw() {
    display();
  }
}
