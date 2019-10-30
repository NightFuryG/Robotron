class SeekBot extends Robot {

  final float MAX_SPEED = displayWidth/1500;
  final float MAX_ACCEL = 0.1f;
  final float MAX_ROTATION = PI/4;

  PVector linear;
  float rotation;
  PVector direction;
  PVector pursueTarget;
  int familyIndex;
  char member;
  boolean pursue;

  SeekBot (float x, float  y, int roomIndex, int familyIndex, char member) {
    super(x, y, roomIndex);
    this.rotation = 0;
    this.linear = new PVector(0,0);
    this.direction = new PVector(0,0);
    this.pursueTarget = new PVector(0,0);
    this.familyIndex = familyIndex;
    this.pursue = true;
    this.member = member;
  }

  void update(Human human) {
    pursue(human);
  }

  void update() {

  }

  void display() {
    fill(102, 0, 102);
    square(this.position.x, this.position.y, this.size);
  }

  void draw(Human human){
    update(human);
    display();
    System.out.println("reached");
  }

  void draw() {
    update();
    display();
  }

  void pursue(Human human) {
    direction.x = human.position.x + human.humanSize/2 - this.position.x;
    direction.y = human.position.y + human.humanSize/2 - this.position.y;

    float distance = direction.mag();
    float speed = this.velocity.mag();
    float prediction = distance/speed;

    pursueTarget = human.velocity.copy();
    pursueTarget.mult(prediction);
    pursueTarget.add(human.position);

    Room room = map.rooms.get(this.roomIndex);
    int radius;

    integrate(pursueTarget, 0);
  }


  void integrate(PVector targetPos, float angular) {
    velocity.limit(0.5);
    this.position.add(this.velocity);

    int cornerBounce = 1;

    if(!checkNotBlack(getLeftColor()) || checkLeftEdge()) {
      if (this.velocity.x < 0) {
        this.velocity.x = -this.velocity.x;
      }
      else if (this.velocity.x >= 0) {
        this.velocity.x = cornerBounce;
      }
    }
    if(!checkNotBlack(getRightColor()) || checkRightEdge()){
      if (this.velocity.x > 0) {
        this.velocity.x = -this.velocity.x;
      } else if (this.velocity.x <= 0) {
          this.velocity.x = -cornerBounce;
      }
    }
    if(!checkNotBlack(getUpColor()) || checkTopEdge()){
      if (this.velocity.y < 0) {
        this.velocity.y = -this.velocity.y;
      } else if (this.velocity.y >= 0) {
          this.velocity.y = cornerBounce;
      }
    }
    if(!checkNotBlack(getDownColor()) || checkBottomEdge()){
      if (this.velocity.y > 0) {
        this.velocity.y = -this.velocity.y;
      } else if (this.velocity.y <= 0) {
        this.velocity.y = -cornerBounce;
      }
    }

    orientation += rotation ;
    if (orientation > PI) orientation -= 2*PI ;
    else if (orientation < -PI) orientation += 2*PI ;
    linear.x = targetPos.x - position.x ;
    linear.y = targetPos.y - position.y ;

    linear.normalize() ;
    linear.mult(MAX_ACCEL) ;
    velocity.add(linear) ;
    if (velocity.mag() > MAX_SPEED) {
      velocity.normalize() ;
      velocity.mult(MAX_SPEED) ;
    }

    rotation += angular ;
    if (rotation > MAX_ROTATION) rotation = MAX_ROTATION ;
    else if (rotation  < -MAX_ROTATION) rotation = -MAX_ROTATION ;
  }
}
