class Player {

  final int PLAYER_SPEED = displayWidth/750;
  final int PLAYER_RADIUS = displayWidth/50;

  final PVector NORTH = new PVector(0,-PLAYER_SPEED),
                SOUTH = new PVector(0, PLAYER_SPEED),
                EAST =  new PVector(PLAYER_SPEED, 0),
                WEST = new PVector(-PLAYER_SPEED, 0);


  PVector position;
  PVector velocity;
  int playerSize;
  int playerSpeed;

  Player(int x, int y) {
    this.position = new PVector(x, y);
    this.velocity = new PVector(0,0);
    this.playerSize = PLAYER_RADIUS;
    this.playerSpeed = PLAYER_SPEED;
  }

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

  void update() {
    velocity.limit(2*PLAYER_SPEED);
    velocity.x *= 0.90;
    velocity.y *= 0.90;
    position.add(velocity);

  }

  void display() {
    fill(255,0,0);
    circle(position.x, position.y, playerSize);
  }


  void draw(){
    update();
    display();
  }



}
