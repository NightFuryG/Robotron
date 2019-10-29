final color BLACK = color(0),
            WHITE = color(255);

final int HUMAN_RADIUS_PROPORTION = 50;

final int TEXT_POSITION = 50,
          TEXT_SIZE = 100;

final int NEW_LIFE = 1000;

final int INVINCIBLE_DURATION = 10000;

Map map;
Player player;
boolean w, a, s, d;
ArrayList<Bullet> bullets;
ArrayList<Human> family;
ArrayList<Obstacle> obstacles;
ArrayList<Robot> robots;
ArrayList<PVector> spawns;
ArrayList<PowerUp> powerUps;
int score;
int size;
int wave;
int newLife;
boolean alive;
boolean startScreen;
boolean bombPowerUp;
boolean invinciblePowerUp;
int startTime;
boolean powerupstarted;
int invincibleDuration;


void setup () {
  fullScreen();
  cursor(CROSS);
  smooth();
  map = new Map();
  player = spawnPlayer();
  w = a = s = d = false;
  score = 0;
  wave = 0;
  newLife = 1;
  size = displayWidth/HUMAN_RADIUS_PROPORTION;
  alive = true;
  startScreen = true;
  bombPowerUp = false;
  invinciblePowerUp = false;
  bullets = new ArrayList<Bullet>();
  family = new ArrayList<Human>();
  obstacles = new ArrayList<Obstacle>();
  robots = new ArrayList<Robot>();
  spawns = new ArrayList<PVector>();
  powerUps = new ArrayList<PowerUp>();

  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  spawnPowerUps();
  checkRooms();
}

void draw () {
    background(0);
    pushStyle();
    textAlign(CENTER);
    fill(255);
    textSize(displayWidth/TEXT_SIZE);
    text("Wave: " + wave, displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Lives: " + player.lives, 3.3 * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Score: " + score, 5.7 * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));

    popStyle();

    if(alive) {
      if(startScreen) {
        pushStyle();
        textAlign(CENTER);
        textSize(64);
        fill(0 ,255, 255);
        text("ROBOTRON", displayWidth/2, displayHeight/2);
        textSize(24);
        text("Click to start", displayWidth/2, 3*displayHeight/4);
        popStyle();
      } else {
        map.draw();
        ensurePlayerInArea();
        playerMove();
        updatePlayerRoom();
        updateRobotRoom();
        removeMissedBullets();
        rangedBotFire();
        player.draw();
        drawBullets();
        drawFamily();
        drawObstacles();
        drawRobots();
        drawPowerUps();
        detectPlayerFamilyCollision();
        detectEnemyCollision();
        detectBulletCollision();
        detectPlayerPowerUpCollision();
        checkPowerUps();
        checkNewLife();
        newWave();
        alive = checkNotDead();
      }
    } else {
      pushStyle();
      textAlign(CENTER);
      textSize(64);
      fill(255, 0 ,255);
      text("GAME OVER", displayWidth/2, displayHeight/2);
      textSize(24);
      text("Click to Restart", displayWidth/2, 3*displayHeight/4);
      popStyle();
    }
}

void newWave(){
  if(checkWaveEnd()) {
    map = new Map();
    reset();
    wave++;
  }
}

void reset(){
  bullets.clear();
  family.clear();
  obstacles.clear();
  robots.clear();
  spawns.clear();
  player = spawnPlayer();
  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  spawnPowerUps();
  alive = true;
  bombPowerUp = false;
  invinciblePowerUp = false;
}

void newGame(){
  map = new Map();
  player.lives = 3;
  newLife = 1;
  score = 0;
  wave = 0;
  reset();
  alive = true;

}

boolean checkNotDead() {
  if(player.lives > 0 || invinciblePowerUp) {
    return true;
  }
  return false;
}

void checkNewLife() {
  if(score >= newLife * NEW_LIFE) {
    player.lives++;
  }
}

void checkPowerUps() {
  if(bombPowerUp) {
    activateBomb();
  }
  if(invinciblePowerUp) {
    if (!powerupstarted) {
      startTime = millis();
      powerupstarted = true;
    }
    activeInvincible(startTime);
  }
}

void activeInvincible(int startTime) {
  int lives = player.lives;

  if(!(millis() < startTime + INVINCIBLE_DURATION)) {
    invinciblePowerUp = false;
    System.out.println("hello katei");
  }
}

void activateBomb() {

  int countA = 0;
  int countB = 0;

  for(Robot robot : new ArrayList<Robot>(robots)) {
    if(countA < robots.size()/2 + 1) {
      robots.remove(robot);
      countA++;
    }
  }

  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    if(countB < obstacles.size()/2 + 1) {
      obstacles.remove(obstacle);
      countB++;
    }
  }

  bombPowerUp = false;
}



Player spawnPlayer() {
  Room firstRoom = map.rooms.get(0);
  int startX = (int) firstRoom.position.x + firstRoom.width/2;
  int startY = (int) firstRoom.position.y + firstRoom.height/2;
  return new Player(startX, startY);
}

void updatePlayerRoom(){
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerSize = player.playerSize;

  for(Room room : map.rooms) {
    float roomX = room.position.x;
    float roomY = room.position.y;
    int roomWidth = room.width;
    int roomHeight = room.height;
    if(playerX > roomX && playerX < roomX + roomWidth){
	    if(playerY > roomY && playerY < roomY + roomHeight){
        player.roomIndex = map.rooms.indexOf(room);
      }
    }
  }
}

void updateRobotRoom() {
  for(Robot robot : robots) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    for(Room room : map.rooms) {
      float roomX = room.position.x;
      float roomY = room.position.y;
      int roomWidth = room.width;
      int roomHeight = room.height;
      if(robotX > roomX && robotX < roomX + roomWidth){
  	    if(robotY > roomY && robotY < roomY + roomHeight){
          robot.roomIndex = map.rooms.indexOf(room);
        }
      }
    }
  }
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
  if(!startScreen) {
    if(alive) {
      bullets.add(new Bullet(player.position.x, player.position.y, mouseX, mouseY, false));
    } else {
      newGame();
    }
  } else {
    startScreen = false;
  }
}

void rangedBotFire(){
  for(Robot robot : robots) {
    if(robot instanceof RangedBot) {
      if(robot.roomIndex == player.roomIndex)
        if(frameCount % 40 == 0)
          bullets.add(new Bullet(robot.position.x + robot.size/2, robot.position.y + robot.size/2, player.position.x, player.position.y, true));
    }
  }
}

void playerMove() {
  if(w) {
    if(checkNotBlack(getUpColor()) && !checkTopEdge()){
      player.move(1);
    }
  }
  if(s) {
    if(checkNotBlack(getDownColor()) && !checkBottomEdge()){
    player.move(2);
    }
  }
  if(d) {
    if(checkNotBlack(getRightColor()) && !checkRightEdge()) {
      player.move(3);
    }
  }
  if(a) {
    if(checkNotBlack(getLeftColor()) && !checkLeftEdge()) {
      player.move(4);
    }
  }
}

void ensurePlayerInArea(){

  int cornerBounce = player.playerSize*10;

  if(!checkNotBlack(getLeftColor()) || checkLeftEdge()) {
    if (player.velocity.x < 0) {
      player.velocity.x = -player.velocity.x;
    }
    else if (player.velocity.x >= 0) {
      player.velocity.x = cornerBounce;
    }
  }
  if(!checkNotBlack(getRightColor()) || checkRightEdge()){
    if (player.velocity.x > 0) {
      player.velocity.x = -player.velocity.x;
    } else if (player.velocity.x <= 0) {
        player.velocity.x = -cornerBounce;
    }
  }
  if(!checkNotBlack(getUpColor()) || checkTopEdge()){
    if (player.velocity.y < 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y >= 0) {
        player.velocity.y = cornerBounce;
    }
  }
  if(!checkNotBlack(getDownColor()) || checkBottomEdge()){
    if (player.velocity.y > 0) {
      player.velocity.y = -player.velocity.y;
    } else if (player.velocity.y <= 0) {
      player.velocity.y = -cornerBounce;
    }
  }
}



boolean checkBottomEdge() {
  int downY= (int) player.position.y + player.playerSize/2;
  return downY >= displayHeight;
}

boolean checkLeftEdge(){
  int leftX = (int) player.position.x - player.playerSize/2;
  return leftX <= 0;
}

boolean checkRightEdge(){
  int rightX = (int) player.position.x + player.playerSize/2;
  return rightX >= displayWidth;
}

boolean checkTopEdge(){
  int topY = (int) player.position.y - player.playerSize/2;
  return topY <= 0;

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

boolean checkWaveEnd(){
  return (robots.size() == 0 ? true : false);
}

void removeMissedBullets() {
  for(Bullet bullet : new ArrayList<Bullet>(bullets)) {
    color detectedColor = get((int) bullet.position.x, (int) bullet.position.y);
    if(!checkNotBlack(detectedColor)) {
      bullets.remove(bullet);
    }
  }
}

void spawnFamilyAndSeekBots(){

  int randomRoomIndex;
  ArrayList<Integer> selectedRooms = new ArrayList();
  int humanCount = 0;

  while(humanCount < 3) {

    randomRoomIndex = map.randomRoomIndex();
    if (!selectedRooms.contains(randomRoomIndex)) {


      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      PVector seekBotSpawnPoint = inverseRandomPointInRoom(randomRoomIndex, randomPointInRoom);

      if(checkSpawnLocation(randomPointInRoom) && checkSpawnLocation(seekBotSpawnPoint)) {
        spawnFamilyMember(humanCount, randomPointInRoom);

        if(checkCentralRoomSpawn(randomPointInRoom, randomRoomIndex)) {
          robots.add(spawnSeekBot(seekBotSpawnPoint, randomRoomIndex));
          spawns.add(seekBotSpawnPoint);
        } else {
          robots.add(spawnDefaultSeekBot(randomRoomIndex));
        }
        humanCount++;
        selectedRooms.add(randomRoomIndex);
        spawns.add(randomPointInRoom);

      }

    }

  }
}

SeekBot spawnSeekBot(PVector seekBotSpawnPoint, int roomIndex) {
  return new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2, roomIndex);
}

SeekBot spawnDefaultSeekBot(int randomRoomIndex){
  int spawnRadius = size;
  Room room = map.rooms.get(randomRoomIndex);
  float roomX = room.position.x + spawnRadius;
  float roomY = room.position.y + spawnRadius;
  PVector spawnLocation = new PVector(roomX, roomY);
  spawns.add(spawnLocation);

  return spawnSeekBot(spawnLocation, randomRoomIndex);

}

boolean checkCentralRoomSpawn(PVector spawnLocation, int randomRoomIndex) {
  float spawnRadius = displayWidth/HUMAN_RADIUS_PROPORTION;
  Room room = map.rooms.get(randomRoomIndex);
  PVector spawn = new PVector(room.position.x + room.width/2, room.position.y + room.height/2);

  if(spawnLocation.x > spawn.x - spawnRadius && spawnLocation.x < spawn.x + spawnRadius) {
    if(spawnLocation.y > spawn.y - spawnRadius && spawnLocation.y < spawn.y + spawnRadius) {
      return false;
    }
  }
  return true;
}




boolean checkSpawnLocation(PVector spawnLocation) {
  float spawnRadius = displayWidth/(HUMAN_RADIUS_PROPORTION/2);

  if(spawns.size() == 0) {
    return true;
  }

  for(PVector spawn : spawns) {
    if(spawnLocation.x > spawn.x - spawnRadius && spawnLocation.x < spawn.x + spawnRadius) {
      if(spawnLocation.y > spawn.y - spawnRadius && spawnLocation.y < spawn.y + spawnRadius) {
        return false;
      }
    }
  }
  return true;
}


PVector inverseRandomPointInRoom(int index, PVector familyPosition) {

  Room room = map.rooms.get(index);

  float inverseX = 2*room.position.x + room.width - familyPosition.x;
  float inverseY = 2*room.position.y + room.height - familyPosition.y;

  PVector seekBotSpawnPosition = new PVector(inverseX, inverseY);

  return seekBotSpawnPosition;
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

//ADDSCORE
void detectPlayerFamilyCollision(){
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  for(Human human : new ArrayList<Human>(family)) {
    float humanX = human.position.x;
    float humanY = human.position.y;
    int humanRadius = human.humanSize;
    if(dist(playerX, playerY, humanX, humanY) < playerRadius/2 + humanRadius/2) {
      if(human.member == 'F') {
        score += 100;
      } else if (human.member == 'M'){
        score += 75;
      } else {
        score += 50;
      }
      family.remove(human);
    }
  }
}

void detectPlayerPowerUpCollision(){
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  for(PowerUp powerUp : new ArrayList<PowerUp>(powerUps)){
    float powerUpX = powerUp.position.x;
    float powerUpY = powerUp.position.y;
    int powerUpRadius = powerUp.size;

    if(dist(playerX, playerY, powerUpX, powerUpY) < playerRadius/2 + powerUpRadius/2){
      if(powerUp instanceof BombPowerUp) {
        bombPowerUp = true;
        powerUps.remove(powerUp);
      } else {
        invinciblePowerUp = true;
        powerUps.remove(powerUp);
      }
    }
  }
}

void detectEnemyCollision() {
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  if(obstacles.size() > 0) {
    playerObstacleCollision(playerX, playerY, playerRadius);
  }
  if(robots.size() > 0) {
    playerRobotCollision(playerX, playerY, playerRadius);
  }
}

void playerObstacleCollision(float playerX, float playerY, int playerSize){
  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    float obstacleX = obstacle.position.x;
    float obstacleY = obstacle.position.y;
    int obstacleSize = obstacle.size;

    if(playerX > obstacleX - obstacleSize && playerX < obstacleX + obstacleSize) {
      if(playerY > obstacleY - obstacleSize && playerY < obstacleY + obstacleSize) {
        obstacles.remove(obstacle);
        if(!invinciblePowerUp) {
          player.lives--;
        }

      }
    }
  }
}

void playerRobotCollision(float playerX, float playerY, int playerSize){
  for(Robot robot : new ArrayList<Robot>(robots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(playerX - playerSize/2 < robotX + robotSize && playerX + playerSize/2 > robotX) {
      if(playerY - playerSize/2 < robotY + robotSize && playerY + playerSize/2 > robotY) {
        robots.remove(robot);
        if(!invinciblePowerUp) {
          player.lives--;
        }
      }
    }
  }
}

void detectBulletCollision(){
  for(Bullet bullet : new ArrayList<Bullet>(bullets)){

    if(obstacles.size() > 0) {
      bulletObstacleCollision(bullet);
    }

    if(robots.size() > 0) {
      bulletRobotCollision(bullet);
    }

    if(family.size() > 0) {
      bulletHumanCollision(bullet);
    }

    if(powerUps.size() > 0) {
      bulletPowerUpCollision(bullet);
    }
  }
}

void bulletHumanCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(Human human : new ArrayList<Human>(family)) {
    float humanX = human.position.x;
    float humanY = human.position.y;
    int humanSize = human.humanSize;

    if(dist(bulletX, bulletY, humanX, humanY) < humanSize/2) {
      family.remove(human);
      bullets.remove(bullet);
    }
  }
}

void bulletPowerUpCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(PowerUp powerUp : new ArrayList<PowerUp>(powerUps)) {
    float powerUpX = powerUp.position.x;
    float powerUpY = powerUp.position.y;
    int powerUpSize = powerUp.size;

    if(dist(bulletX, bulletY, powerUpX, powerUpY) < powerUpSize/2) {
      powerUps.remove(powerUp);
      bullets.remove(bullet);
    }
  }
}

void bulletObstacleCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    float obstacleX = obstacle.position.x;
    float obstacleY = obstacle.position.y;
    int obstacleSize = obstacle.size;

    if(bulletX > obstacleX - obstacleSize && bulletX < obstacleX + obstacleSize) {
      if(bulletY > obstacleY - obstacleSize && bulletY < obstacleY + obstacleSize) {
        obstacles.remove(obstacle);
        bullets.remove(bullet);
        score += 5;
      }
    }
  }

}

void bulletRobotCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;

  for(Robot robot : new ArrayList<Robot>(robots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(bulletX > robotX - robotSize && bulletX < robotX + robotSize) {
      if(bulletY > robotY - robotSize && bulletY < robotY + robotSize) {
        if(!bullet.enemy) {
          robots.remove(robot);
          bullets.remove(bullet);
          score += 10;
        }
      }
    }
  }
}

void spawnObstacles(){
  int randomRoomIndex;
  int obstacleCount = 0;

  while(obstacleCount < 10) {

      randomRoomIndex = map.randomRoomIndex();
      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      if(checkSpawnLocation(randomPointInRoom)) {
        spawns.add(randomPointInRoom);
        obstacles.add(new Obstacle(randomPointInRoom.x, randomPointInRoom.y));
        obstacleCount++;
       }
    }
}

void spawnRobots() {
  int randomRoomIndex;
  int robotCount = 0;

  while(robotCount < 10) {

      randomRoomIndex = map.randomRoomIndex();
      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      if(checkSpawnLocation(randomPointInRoom)) {
        if(robotCount % 2 == 1) {
          robots.add(new MeleeBot(randomPointInRoom.x, randomPointInRoom.y, randomRoomIndex));
          spawns.add(randomPointInRoom);
          robotCount++;
        } else {
          robots.add(new RangedBot(randomPointInRoom.x, randomPointInRoom.y, randomRoomIndex));
          System.out.println("HI");
          spawns.add(randomPointInRoom);
          robotCount++;
        }


    }
  }
}

void spawnPowerUps(){
  int randomRoomIndex;
  int powerUpCount = 0;

  while(powerUpCount < 2) {
    randomRoomIndex = map.randomRoomIndex();
    PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
    if(checkSpawnLocation(randomPointInRoom)) {
      if(powerUpCount == 0) {
        powerUps.add(new BombPowerUp(randomPointInRoom.x, randomPointInRoom.y));
        spawns.add(randomPointInRoom);
        powerUpCount++;
      } else {
        powerUps.add(new InvinciblePowerUp(randomPointInRoom.x, randomPointInRoom.y));
        spawns.add(randomPointInRoom);
        powerUpCount++;
      }
    }
  }
}

void drawRobots(){
  for(Robot robot : robots) {
    robot.draw();
  }
}

void drawPowerUps(){
  for(PowerUp powerUp : powerUps){
    powerUp.draw();
  }
}



PVector randomPointInRoom(int randomRoomIndex) {
  int boundarySpace = displayWidth/HUMAN_RADIUS_PROPORTION;
  Room randomRoom = map.rooms.get(randomRoomIndex);

  float x1 = (randomRoom.position.x +(boundarySpace));
  float x2 = (randomRoom.position.x + randomRoom.width - (2 * boundarySpace));

  float y1 = (randomRoom.position.y + (boundarySpace));
  float y2 = (randomRoom.position.y + randomRoom.height - (2 * boundarySpace));

  float randomX = random(x1, x2);
  float randomY = random(y1, y2);

  return new PVector(randomX, randomY);
}

void drawObstacles() {
  for(Obstacle obstacle : obstacles) {
    obstacle.draw();
  }
}

void printSpawns() {
  for(PVector spawn : spawns) {
    System.out.println(spawn);
  }
}

void checkRooms() {
  for(Room room : map.rooms) {
    if(room.position.x + room.width > displayWidth) {
      System.out.println("bad x");
    }
    if(room.position.y + room.height > displayHeight) {
      System.out.println("bad y");
    }
  }
}
