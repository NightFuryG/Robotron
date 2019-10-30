class MeleeBot extends Robot {

  final float MAX_SPEED = displayWidth/750 ;
  final float MAX_ACCEL = 0.1f ;
  final float MAX_ROTATION = PI/4 ;

  boolean pursue;
  PVector linear;
  float rotation;
  PVector direction;
  PVector pursueTarget;

  MeleeBot(float x, float y, int roomIndex) {
    super(x , y, roomIndex);
    this.pursue = false;
    this.rotation = 0;
    this.linear = new PVector(0,0);
    this.direction = new PVector(0,0);
    this.pursueTarget = new PVector(0,0);
  }

  void update(Player player) {
    this.ensureRobotInArea();
    if(!pursue) {
      this.wander();
    } else {
      pursue(player);
    }
  }

  void display() {
    fill(255, 255, 0);
    square(this.position.x, this.position.y, this.size);
  }

  void draw(Player player){
    update(player);
    display();
  }

  void ensureInArea(){

    int cornerBounce = player.playerSize*10;

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
  }

  void pursue(Player player) {
    direction.x = player.position.x + player.playerSize/2 - this.position.x;
    direction.y = player.position.y + player.playerSize/2 - this.position.y;

    float distance = direction.mag();
    float speed = this.velocity.mag();
    float prediction = distance/speed;

    pursueTarget = player.velocity.copy();
    pursueTarget.mult(prediction);
    pursueTarget.add(player.position);

    Room room = map.rooms.get(player.roomIndex);
    int radius;

    if(room.width > height) {
      radius = room.height/2;
    } else {
      radius = room.width/2;
    }

    if(dist(this.position.x, this.position.y, player.position.x, player.position.y) < radius) {
      integrate(pursueTarget, 0);
    } else {
      integrate(player.position, 0);
    }
  }


  void integrate(PVector targetPos, float angular) {
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
