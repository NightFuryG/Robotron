class Human {

  final int HUMAN_SIZE = displayWidth/80;
  final float MAX_SPEED = displayWidth/1000;
  final float MAX_ACCEL = 0.1f;
  final float MAX_ROTATION = PI/4;
  final float ORIENTATION_INCREMENT = PI/8 ;
  final int SEP_THRESHOLD = displayWidth/20;

  PVector position;
  PVector velocity;
  PVector linear;
  PVector acceleration;
  PVector direction;
  float orientation;
  int humanSize;
  int seekBotIndex;
  char member;
  boolean flee;

  Human(float x, float y, char member, int seekBotIndex){
    this.position = new PVector(x, y);
    this.humanSize = HUMAN_SIZE;
    this.member = member;
    this.velocity = new PVector(0,0);
    this.orientation = 0;
    this.acceleration = new PVector(0,0);
    this.direction = new PVector(0,0);
    this.seekBotIndex = seekBotIndex;
    this.flee = true;
  }

  void integrate(PVector linear) {
    velocity.limit(MAX_SPEED);
    position.add(velocity);

    float cornerBounce = MAX_SPEED;

    if(!detectNotBlack(getLeftColor()) || detectLeftEdge()) {
      if (this.velocity.x < 0) {
        this.velocity.x = -this.velocity.x;
      } else if (this.velocity.x >= 0) {
        this.velocity.x = cornerBounce;
      }
    }
    if(!detectNotBlack(getRightColor()) || detectRightEdge()){
      if (this.velocity.x > 0) {
        this.velocity.x = -this.velocity.x;
      } else if (this.velocity.x <= 0) {
        this.velocity.x = -cornerBounce;
      }
    }
    if(!detectNotBlack(getUpColor()) || detectTopEdge()){
      if (this.velocity.y < 0) {
        this.velocity.y = -this.velocity.y;
      } else if (this.velocity.y >= 0) {
        this.velocity.y = cornerBounce;
      }
    }
    if(!detectNotBlack(getDownColor()) || detectBottomEdge()){
      if (this.velocity.y > 0) {
        orientation += PI;
        this.velocity.y = -this.velocity.y;
      } else if (this.velocity.y <= 0) {
        orientation = 2*PI;
        this.velocity.y = -cornerBounce;
      }

    }


    if (linear.mag() > MAX_ACCEL) {
      linear.normalize() ;
      linear.mult(MAX_ACCEL) ;
    }
    velocity.add(linear) ;
    if (velocity.mag() > MAX_SPEED) {
      velocity.normalize() ;
      velocity.mult(MAX_SPEED) ;
    }


    float targetOrientation = atan2(velocity.y, velocity.x) ;

    // Will take a frame extra at the PI boundary
    if (abs(targetOrientation - orientation) <= ORIENTATION_INCREMENT) {
      orientation = targetOrientation ;
      return ;
    }

    // if it's less than me, then how much if up to PI less, decrease otherwise increase
    if (targetOrientation < orientation) {
      if (orientation - targetOrientation < PI) orientation -= ORIENTATION_INCREMENT ;
      else orientation += ORIENTATION_INCREMENT ;
    }
    else {
     if (targetOrientation - orientation < PI) orientation += ORIENTATION_INCREMENT ;
     else orientation -= ORIENTATION_INCREMENT ;
    }

    // Keep in bounds
    if (orientation > PI) orientation -= 2*PI ;
    else if (orientation < -PI) orientation += 2*PI ;
  }

  void flee(SeekBot seekBot) {
    acceleration.x = 0;
    acceleration.y = 0;

    direction.x = this.position.x - seekBot.position.x - seekBot.size/2;
    direction.y = this.position.y - seekBot.position.y - seekBot.size/2;

    float distance = direction.mag();
    if(distance < SEP_THRESHOLD) {
      direction.normalize();
      direction.mult((MAX_ACCEL * (SEP_THRESHOLD - distance) / SEP_THRESHOLD));
      acceleration.add(direction);
    } else {
      velocity.x = cos(orientation);
      velocity.y = sin(orientation);
      velocity.mult(MAX_SPEED);
      orientation += random(0, ORIENTATION_INCREMENT) - random(0, ORIENTATION_INCREMENT);
    }
    integrate(acceleration);
  }


  void update(SeekBot seekBot) {
    flee(seekBot);
  }

  void update(){

  }

  void display(){
    fill(0,255,255);
    circle(position.x, position.y, humanSize);
    fill(0);
    textAlign(CENTER, CENTER);
    text(member, position.x, position.y);
  }

  void draw(SeekBot seekBot) {
    update(seekBot);
    display();
  }

  void draw() {
    update();
    display();
  }
  boolean detectBottomEdge() {
   int downY= (int) this.position.y + this.humanSize;
   return downY >= displayHeight;
  }

  boolean detectLeftEdge(){
   int leftX = (int) this.position.x - this.humanSize;
   return leftX <= 0;
  }

  boolean detectRightEdge(){
   int rightX = (int) this.position.x + this.humanSize;
   return rightX >= displayWidth;
  }

  boolean detectTopEdge(){
   int topY = (int) this.position.y - this.humanSize;
   return topY <= 0;

  }

  color getLeftColor() {
   int leftX = (int) this.position.x - this.humanSize;
   int leftY = (int) this.position.y;
   color leftColor = get(leftX, leftY);
   return leftColor;
  }

  color getRightColor() {
   int rightX = (int) this.position.x + this.humanSize;
   int rightY = (int) this.position.y;
   color rightColor = get(rightX, rightY);
   return rightColor;
  }

  color getUpColor() {
   int upX = (int) this.position.x;
   int upY = (int) this.position.y - this.humanSize;
   color upColor = get(upX, upY);
   return upColor;
  }

  color getDownColor() {
   int downX = (int) this.position.x;
   int downY= (int) this.position.y + this.humanSize;
   color downColor = get(downX, downY);
   return downColor;
  }

  boolean detectNotBlack(color inColor){
   return inColor != BLACK;
  }
}
