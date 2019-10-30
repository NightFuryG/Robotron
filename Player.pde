class Player {

  final int PLAYER_SPEED = displayWidth/750,
            PLAYER_RADIUS = displayWidth/80,
            PLAYER_LIVES = 3;


  /*
    Class representing the superhuman
  */
  PVector position;
  PVector velocity;
  int playerSize;
  int playerSpeed;
  int lives;
  int roomIndex;

  Player(int x, int y, int lives) {
    this.position = new PVector(x, y);
    this.velocity = new PVector(0,0);
    this.playerSize = PLAYER_RADIUS;
    this.playerSpeed = PLAYER_SPEED;
    this.lives = lives;
    this.roomIndex = 0;
  }

  //move in the direction ordered by the player
  //has incremental velocity and not instantaneous
  void move(int i) {
    switch (i) {
      case 1:
        velocity.y -= PLAYER_SPEED;
        break;
      case 2:
        velocity.y += PLAYER_SPEED;
        break;
      case 3:
        velocity.x += PLAYER_SPEED;
        break;
      case 4:
        velocity.x -= PLAYER_SPEED;
        break;
      default:
        break;
    }
  }

  //move the player up to its max speed
  //if key is keyReleased, momentum keeps player moving
  //draw/friction causes player to stop
  void update() {
    velocity.limit(2*PLAYER_SPEED);
    velocity.x *= 0.90;
    velocity.y *= 0.90;
    position.add(velocity);

  }

  void display() {
    fill(0,255, 255);
    circle(position.x, position.y, playerSize);
  }


  void draw(){
    update();
    display();
  }
}
