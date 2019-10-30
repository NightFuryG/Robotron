class Robot {
  final int ROBOT_SIZE = 80;

  final float ORIENTATION_INCREMENT = PI/32;
  final float ROBOT_SPEED = displayWidth/1000;

  PVector position;
  PVector startPosition;
  PVector velocity;
  float orientation;
  int roomIndex;
  int size;
  /*
    Super Class representing base robot and behaviour
  */
  Robot(float x, float y, int roomIndex) {
    this.startPosition = new PVector(x, y);
    this.position = new PVector(x, y);
    this.velocity = new PVector(1,1);
    this.size = displayWidth/ROBOT_SIZE;
    this.orientation = random(-2*PI, 2*PI);
    this.roomIndex = roomIndex;
  }

  void draw () {

  }

  void draw(Player player) {

  }

  void draw(Human human) {

  }

  //wander randomly - code from example on studres and adjusted
  void wander(){
    ensureRobotInArea();
    velocity.x = cos(orientation);
    velocity.y = sin(orientation);
    velocity.mult(ROBOT_SPEED);

    position.add(velocity);

    orientation += random(0, ORIENTATION_INCREMENT) - random(0, ORIENTATION_INCREMENT);

    if(orientation > PI) {
      orientation -= 2*PI;
    } else if (orientation < - PI) {
      orientation += 2*PI;
    }
  }

  //ensures that the robot stays in bounds by checking colours and edges
  //inverses velocity if at boundary
  void ensureRobotInArea() {
    float cornerBounce = 1;

    if(!detectNotBlack(getLeftColor()) || detectLeftEdge()) {
      if (this.velocity.x < 0) {
        orientation += PI/2;
        this.velocity.x = -velocity.x;
      } else if (this.velocity.x >= 0) {
        orientation += PI/2;
        velocity.x = cornerBounce;
      }
    }
    if(!detectNotBlack(getRightColor()) || detectRightEdge()){
      if (this.velocity.x > 0) {
        orientation += PI/2;
        velocity.x = - velocity.x;
      } else if (this.velocity.x <= 0) {
        orientation += PI/2;
        velocity.x = -cornerBounce;
      }
    }
    if(!detectNotBlack(getUpColor()) || detectTopEdge()){
      if (this.velocity.y < 0) {
        orientation += PI/2;
        velocity.y = - velocity.y;
      } else if (this.velocity.y >= 0) {
        orientation += PI/2;
        velocity.y = cornerBounce;
      }
    }
    if(!detectNotBlack(getDownColor()) || detectBottomEdge()){
      if (this.velocity.y > 0) {
        orientation += PI/2;
        velocity.y = - velocity.y;
      } else if (this.velocity.y <= 0) {
        orientation += PI/2;
        velocity.y = - cornerBounce;
      }
    }
  }

    //detection methods.

     boolean detectBottomEdge() {
      int downY= (int) this.position.y + this.size;
      return downY >= displayHeight;
    }

     boolean detectLeftEdge(){
      int leftX = (int) this.position.x;
      return leftX <= 0;
    }

     boolean detectRightEdge(){
      int rightX = (int) this.position.x + this.size;
      return rightX >= displayWidth;
    }

     boolean detectTopEdge(){
      int topY = (int) this.position.y;
      return topY <= 0;

    }

     color getLeftColor() {
      int leftX = (int) this.position.x;
      int leftY = (int) this.position.y;
      color leftColor = get(leftX, leftY);
      return leftColor;
    }

     color getRightColor() {
      int rightX = (int) this.position.x + this.size;
      int rightY = (int) this.position.y;
      color rightColor = get(rightX, rightY);
      return rightColor;
    }

     color getUpColor() {
      int upX = (int) this.position.x;
      int upY = (int) this.position.y ;
      color upColor = get(upX, upY);
      return upColor;
    }

     color getDownColor() {
      int downX = (int) this.position.x;
      int downY= (int) this.position.y + this.size;
      color downColor = get(downX, downY);
      return downColor;
    }

    color getPositionColor() {
      int x = (int) this.position.x + this.size/2;
      int y = (int) this.position.y + this.size/2;
      color positionColor = get(x, y);
      return positionColor;
    }

    boolean detectNotBlack(color inColor){
      return inColor != BLACK;
    }
  }
