class Human {

  final int HUMAN_SIZE = displayWidth/80;

  PVector position;
  int humanSize;
  char member;

  Human(float x, float y, char member){
    this.position = new PVector(x, y);
    this.humanSize = HUMAN_SIZE;
    this.member = member;
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

  void draw() {
    update();
    display();
  }

}
