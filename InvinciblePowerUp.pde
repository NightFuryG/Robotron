class InvinciblePowerUp extends PowerUp {

  InvinciblePowerUp(float x, float y) {
    super(x, y);
  }

  void display() {
    fill(0, 153, 153);
    circle(this.position.x, this.position.y, this.size);
    fill(0);
    textAlign(CENTER, CENTER);
    text('I', position.x, position.y);
  }

  void draw() {
    display();
  }
}
