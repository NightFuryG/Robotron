final color BLACK = color(0),
            WHITE = color(255);

final int HUMAN_RADIUS_PROPORTION = 50;

Map map;
Player player;
boolean w, a, s, d;
ArrayList<Bullet> bullets;
ArrayList<Human> family;



void setup () {
  fullScreen();
  cursor(CROSS);
  smooth();
  map = new Map();
  player = spawnPlayer();
  w = a = s = d = false;
  bullets = new ArrayList();
  family = new ArrayList();
  spawnFamily();

}

void draw () {
  background(0);
  map.draw();
  ensurePlayerInArea();
  playerMove();
  player.draw();
  removeMissedBullets();
  drawBullets();
  drawFamily();
}



Player spawnPlayer() {
  Room firstRoom = map.rooms.get(0);
  int startX = (int) firstRoom.position.x + firstRoom.width/2;
  int startY = (int) firstRoom.position.y + firstRoom.height/2;
  return new Player(startX, startY);
}

void keyPressed() {
    if(key == 'w') {
      w = true;
    } else if (key == 's') {
      s = true;
    } else if (key == 'd') {
      d = true;
    } else if (key == 'a') {
      a = true;
    }
}

void keyReleased() {
  if(key == 'w') {
    w = false;
  } else if (key == 's') {
    s = false;
  } else if (key == 'd') {
    d = false;
  } else if (key == 'a') {
    a = false;
  }
}

void mousePressed(){
  bullets.add(new Bullet(player.position.x, player.position.y, mouseX, mouseY));
  System.out.println(bullets.size());
}

void playerMove() {
  if(w) {
    if(checkNotBlack(getUpColor())){
      player.move(1);
    }
  }
  if(s) {
    if(checkNotBlack(getDownColor())){
    player.move(2);
    }
  }
  if(d) {
    if(checkNotBlack(getRightColor())) {
      player.move(3);
    }
  }
  if(a) {
    if(checkNotBlack(getLeftColor())) {
      player.move(4);
    }
  }
}

void ensurePlayerInArea(){

  int cornerBounce = player.playerSize*10;

  if(!checkNotBlack(getLeftColor())) {
    if (player.velocity.x < 0) {
      player.velocity.x = -player.velocity.x;
    }
    else if (player.velocity.x >= 0) {
      player.velocity.x = cornerBounce;
    }
  }
  if(!checkNotBlack(getRightColor())){
    if (player.velocity.x > 0) {
      player.velocity.x = -player.velocity.x;
    } else if (player.velocity.x <= 0) {
        player.velocity.x = -cornerBounce;
    }
  }
  if(!checkNotBlack(getUpColor())){
    if (player.velocity.y < 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y >= 0) {
        player.velocity.y = cornerBounce;
    }
  }
  if(!checkNotBlack(getDownColor())){
    if (player.velocity.y > 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y <= 0) {
      player.velocity.y = -cornerBounce;
    }
  }
}

color getLeftColor() {
  int leftX = (int) player.position.x - player.playerSize/2;
  int leftY = (int) player.position.y;
  color leftColor = get(leftX, leftY);
  return leftColor;
}

color getRightColor() {
  int rightX = (int) player.position.x + player.playerSize/2;
  int rightY = (int) player.position.y;
  color rightColor = get(rightX, rightY);
  return rightColor;
}

color getUpColor() {
  int upX = (int) player.position.x;
  int upY = (int) player.position.y - player.playerSize/2;
  color upColor = get(upX, upY);
  return upColor;
}

color getDownColor() {
  int downX = (int) player.position.x;
  int downY= (int) player.position.y + player.playerSize/2;
  color downColor = get(downX, downY);
  return downColor;
}

boolean checkNotBlack(color inColor){
  return inColor != BLACK;
}

boolean checkWhite(color inColor) {
  return inColor == WHITE;
}

void drawBullets() {
  for(Bullet bullet : bullets) {
    bullet.draw();
  }
}

void drawFamily() {
  for(Human human : family) {
    human.draw();
  }
}

void removeMissedBullets() {
  for(Bullet bullet : new ArrayList<Bullet>(bullets)) {
    color detectedColor = get((int) bullet.position.x, (int) bullet.position.y);
    if(!checkNotBlack(detectedColor)) {
      bullets.remove(bullet);
    }
  }
}

void spawnFamily(){

  int randomRoomIndex;
  ArrayList<Integer> selectedRooms = new ArrayList();
  int humanCount = 0;
  int boundarySpace = displayWidth/HUMAN_RADIUS_PROPORTION;

  while(humanCount < 3) {

    randomRoomIndex = map.randomRoomIndex();

    if (!selectedRooms.contains(randomRoomIndex)) {
      Room randomRoom = map.rooms.get(randomRoomIndex);

      float x1 = (randomRoom.position.x +(2 * boundarySpace));
      float x2 = (randomRoom.position.x + randomRoom.width - (4 * boundarySpace));

      float y1 = (randomRoom.position.y + (2 * boundarySpace));
      float y2 = (randomRoom.position.y + randomRoom.height - (4 * boundarySpace));

      float randomX = random(x1, x2);
      float randomY = random(y1, y2);

      PVector randomPointInRoom = new PVector(randomX, randomY);
      spawnFamilyMember(humanCount, randomPointInRoom);
      humanCount++;
      selectedRooms.add(randomRoomIndex);

    }

  }
}

void spawnFamilyMember(int i, PVector randomPointInRoom){
    switch(i) {
      case 0:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'F'));
        break;
      case 1:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'M'));
        break;
      case 2:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'C'));
        break;
      default:
        break;
    }
}

void detectPlayerFamilyCollision(){
  
}
