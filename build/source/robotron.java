import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class robotron extends PApplet {



final int BLACK = color(0),
            WHITE = color(255);

final int HUMAN_RADIUS_PROPORTION = 50;

final int TEXT_POSITION = 50,
          TEXT_SIZE = 100;

final int NEW_LIFE = 1000;

final int INVINCIBLE_DURATION = 10000;

final int BASE_MULTIPLIER = 10,
          OBSTACLE_MULTIPLIER = 2;

//intialise Variables
Map map;
Player player;
boolean w, a, s, d;
ArrayList<Bullet> bullets;
ArrayList<Human> family;
ArrayList<Obstacle> obstacles;
ArrayList<Robot> robots;
ArrayList<SeekBot> seekBots;
ArrayList<PVector> spawns;
ArrayList<PowerUp> powerUps;
ArrayList<MeleeBot> meleeBots;
int score;
int size;
int wave;
int newLife;
int lives;
boolean alive;
boolean startScreen;
boolean bombPowerUp;
boolean invinciblePowerUp;
int startTime;
boolean powerupstarted;
int invincibleDuration;

//sound
Minim minim;
AudioSample shootSound;
AudioSample hitSound;
AudioSample newWaveSound;
AudioPlayer gameOverSound;
AudioPlayer bombSound;
AudioPlayer invincibleSound;

public void setup () {
  
  cursor(CROSS);
  

  minim = new Minim(this);
  shootSound = minim.loadSample("data/shoot.mp3");
  hitSound = minim.loadSample("data/hit.mp3");
  newWaveSound = minim.loadSample("data/newWave.mp3");
  gameOverSound = minim.loadFile("data/gameOver.mp3");
  bombSound = minim.loadFile("data/bomb.mp3");
  invincibleSound = minim.loadFile("data/i.mp3");


  map = new Map();
  w = a = s = d = false;
  score = 0;
  wave = 1;
  newLife = 1;
  lives = 5;
  player = spawnPlayer(lives);
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
  meleeBots = new ArrayList<MeleeBot>();
  seekBots = new ArrayList<SeekBot>();
  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  spawnPowerUps();
  checkRooms();
}

public void draw () {

    //display text top left
    background(0);
    pushStyle();
    textAlign(CENTER);
    fill(255);
    textSize(displayWidth/TEXT_SIZE);
    text("Wave: " + wave, displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Lives: " + player.lives, 3.3f * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));
    text("Score: " + score, 5.7f * displayWidth/TEXT_POSITION, displayWidth/(TEXT_POSITION*2));

    popStyle();
    //game logic
    if(alive) {
      if(startScreen) {
        //start screen
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
        meleeBotPursue();
        rangedBotFire();
        player.draw();
        drawBullets();
        drawFamily();
        drawObstacles();
        drawRobots();
        drawPowerUps();
        detectPlayerFamilyCollision();
        detectSeekBotFamilyCollision();
        detectEnemyCollision();
        detectBulletCollision();
        detectPlayerPowerUpCollision();
        checkPowerUps();
        checkNewLife();
        player.lives = lives;
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
      gameOverSound.play();
    }
}

//start a new wave
public void newWave(){
  if(checkWaveEnd()) {
    map = new Map();
    reset();
    wave++;
    newWaveSound.trigger();
  }
}

//reset Variables
public void reset(){
  bullets.clear();
  family.clear();
  obstacles.clear();
  robots.clear();
  spawns.clear();
  powerUps.clear();
  seekBots.clear();
  meleeBots.clear();
  player = spawnPlayer(lives);
  spawnFamilyAndSeekBots();
  spawnObstacles();
  spawnRobots();
  spawnPowerUps();
  alive = true;
  bombPowerUp = false;
  invinciblePowerUp = false;
}

//start a new game
public void newGame(){
  map = new Map();
  lives = 5;
  newLife = 1;
  score = 0;
  wave = 1;
  reset();
  alive = true;
}

//Melee bot pursue player
public void meleeBotPursue() {
  for(Robot robot : robots) {
    if(robot instanceof MeleeBot) {
      if(robot.roomIndex == player.roomIndex) {
          ((MeleeBot)robot).pursue = true;
      } else {
        ((MeleeBot)robot).pursue = false;
        }
    }
  }
}

//check player is alive
public boolean checkNotDead() {
  if(lives > 0 || invinciblePowerUp) {
    return true;
  }
  return false;
}

//check if enough points earned for a new life
public void checkNewLife() {
  if(score >= newLife * NEW_LIFE) {
    player.lives++;
    newLife++;
  }
}

//check if power up active
public void checkPowerUps() {
  if(bombPowerUp) {
    activateBomb();
    bombSound.play();
  }
  if(invinciblePowerUp) {
    if (!powerupstarted) {
      startTime = millis();
      powerupstarted = true;
      invincibleSound.play();
    } else {
      activeInvincible(startTime);
    }
  }
}

//activate Invincibility
public void activeInvincible(int startTime) {

  if(!(millis() < startTime + INVINCIBLE_DURATION)) {
    invinciblePowerUp = false;
    powerupstarted = false;
  } else {
    invinciblePowerUp = true;
  }
}

//activate Bomb
public void activateBomb() {

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

//spawn player
public Player spawnPlayer(int lives) {
  Room firstRoom = map.rooms.get(0);
  int startX = (int) firstRoom.position.x + firstRoom.width/2;
  int startY = (int) firstRoom.position.y + firstRoom.height/2;
  return new Player(startX, startY, lives);
}

// determine which room player is in
public void updatePlayerRoom(){
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

// determine which room robot is in
public void updateRobotRoom() {
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

// movement
public void keyPressed() {
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

// movement
public void keyReleased() {
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

// firing
public void mousePressed(){
  if(!startScreen) {
    if(alive) {
      bullets.add(new Bullet(player.position.x, player.position.y, mouseX, mouseY, false));
      shootSound.trigger();
    } else {
      newGame();
    }
  } else {
    startScreen = false;
  }
}

// enemy fire
public void rangedBotFire(){
  for(Robot robot : robots) {
    if(robot instanceof RangedBot) {
      if(robot.roomIndex == player.roomIndex)
        if(wave % 5 != 0) {
          if(frameCount % 40 == 0) {
            bullets.add(new Bullet(robot.position.x + robot.size/2, robot.position.y + robot.size/2, player.position.x, player.position.y, true));
          }
        } else {
            if(frameCount % 10 == 0)
              bullets.add(new Bullet(robot.position.x + robot.size/2, robot.position.y + robot.size/2, player.position.x, player.position.y, true));
          }
      }
    }
}

// movement
public void playerMove() {
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

// edge detection
public void ensurePlayerInArea(){

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

// check methods
public boolean checkBottomEdge() {
  int downY= (int) player.position.y + player.playerSize/2;
  return downY >= displayHeight;
}

public boolean checkLeftEdge(){
  int leftX = (int) player.position.x - player.playerSize/2;
  return leftX <= 0;
}

public boolean checkRightEdge(){
  int rightX = (int) player.position.x + player.playerSize/2;
  return rightX >= displayWidth;
}

public boolean checkTopEdge(){
  int topY = (int) player.position.y - player.playerSize/2;
  return topY <= 0;
}

public int getLeftColor() {
  int leftX = (int) player.position.x - player.playerSize/2;
  int leftY = (int) player.position.y;
  int leftColor = get(leftX, leftY);
  return leftColor;
}

public int getRightColor() {
  int rightX = (int) player.position.x + player.playerSize/2;
  int rightY = (int) player.position.y;
  int rightColor = get(rightX, rightY);
  return rightColor;
}

public int getUpColor() {
  int upX = (int) player.position.x;
  int upY = (int) player.position.y - player.playerSize/2;
  int upColor = get(upX, upY);
  return upColor;
}

public int getDownColor() {
  int downX = (int) player.position.x;
  int downY= (int) player.position.y + player.playerSize/2;
  int downColor = get(downX, downY);
  return downColor;
}

public boolean checkNotBlack(int inColor){
  return inColor != BLACK;
}

//draw
public void drawBullets() {
  for(Bullet bullet : bullets) {
    bullet.draw();
  }
}

//draw
public void drawFamily() {
  for(int i = 0; i < family.size(); i++) {
    if(family.get(i).flee) {
      for(int j = 0; j < seekBots.size(); j++){
        if(family.get(i).member == seekBots.get(j).member) {
          family.get(i).draw(seekBots.get(j));
        }
      }
    } else {
      family.get(i).draw();
    }
  }
}

// check end of wave
public boolean checkWaveEnd(){
  return ((robots.size() == 0 && seekBots.size() == 0)? true : false);
}

// remove missed bullets
public void removeMissedBullets() {
  for(Bullet bullet : new ArrayList<Bullet>(bullets)) {
    int detectedColor = get((int) bullet.position.x, (int) bullet.position.y);
    if(!checkNotBlack(detectedColor)) {
      bullets.remove(bullet);
    }
  }
}

//collsion
public void detectSeekBotFamilyCollision(){
  for(Human human : new ArrayList<Human>(family)) {
    float humanX = human.position.x;
    float humanY = human.position.y;
    float humanSize = human.humanSize;

    for(SeekBot seekBot : new ArrayList<SeekBot>(seekBots)) {
      float seekBotSize = seekBot.size;
      float seekBotX = seekBot.position.x + seekBotSize/2;
      float seekBotY = seekBot.position.y + seekBotSize/2;

      if(human.member == seekBot.member) {
        if(dist(humanX, humanY, seekBotX, seekBotY) < humanSize/2 + seekBotSize/2) {
          seekBots.remove(seekBot);
          family.remove(human);
          robots.add(new MeleeBot(humanX, humanY, seekBot.roomIndex));
          robots.add(new MeleeBot(humanX, humanY, seekBot.roomIndex));
          robots.add(new RangedBot(humanX, humanY, seekBot.roomIndex));
          robots.add(new RangedBot(humanX, humanY, seekBot.roomIndex));
        }
      }
    }
  }
}

//spawn methods
public void spawnFamilyAndSeekBots(){

  int randomRoomIndex;
  ArrayList<Integer> selectedRooms = new ArrayList();
  int humanCount = 0;

  while(humanCount < 3) {

    randomRoomIndex = map.randomRoomIndex();
    if (!selectedRooms.contains(randomRoomIndex)) {


      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      PVector seekBotSpawnPoint = inverseRandomPointInRoom(randomRoomIndex, randomPointInRoom);

      if(checkSpawnLocation(randomPointInRoom) && checkSpawnLocation(seekBotSpawnPoint)) {
        spawnFamilyMember(humanCount, randomPointInRoom, humanCount);

        if(checkCentralRoomSpawn(randomPointInRoom, randomRoomIndex)) {
          seekBots.add(spawnSeekBot(seekBotSpawnPoint, randomRoomIndex, humanCount));
          spawns.add(seekBotSpawnPoint);
        } else {
          seekBots.add(spawnDefaultSeekBot(randomRoomIndex, humanCount));
        }
        humanCount++;
        selectedRooms.add(randomRoomIndex);
        spawns.add(randomPointInRoom);

      }

    }

  }
}

public SeekBot spawnSeekBot(PVector seekBotSpawnPoint, int roomIndex, int humanCount) {
  SeekBot seekBot = new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2, roomIndex, humanCount, 'Y');
  switch(humanCount) {
    case 0:
      seekBot = new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2, roomIndex, humanCount, 'F');
      break;
    case 1:
      seekBot = new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2, roomIndex, humanCount, 'M');
      break;
    case 2:
      seekBot = new SeekBot(seekBotSpawnPoint.x - size/2, seekBotSpawnPoint.y - size/2, roomIndex, humanCount, 'C');
      break;
    default:
      break;
  }
  return seekBot;
}

public SeekBot spawnDefaultSeekBot(int randomRoomIndex, int humanCount){
  int spawnRadius = size;
  Room room = map.rooms.get(randomRoomIndex);
  float roomX = room.position.x + spawnRadius;
  float roomY = room.position.y + spawnRadius;
  PVector spawnLocation = new PVector(roomX, roomY);
  spawns.add(spawnLocation);

  return spawnSeekBot(spawnLocation, randomRoomIndex, humanCount);
}

// spawn radius
public boolean checkCentralRoomSpawn(PVector spawnLocation, int randomRoomIndex) {
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

//spawn check
public boolean checkSpawnLocation(PVector spawnLocation) {
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

// get inverse room position
public PVector inverseRandomPointInRoom(int index, PVector familyPosition) {

  Room room = map.rooms.get(index);

  float inverseX = 2*room.position.x + room.width - familyPosition.x;
  float inverseY = 2*room.position.y + room.height - familyPosition.y;

  PVector seekBotSpawnPosition = new PVector(inverseX, inverseY);

  return seekBotSpawnPosition;
}

//spawn family
public void spawnFamilyMember(int i, PVector randomPointInRoom, int humanCount){
    switch(i) {
      case 0:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'F', humanCount));
        break;
      case 1:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'M', humanCount));
        break;
      case 2:
        family.add(new Human(randomPointInRoom.x, randomPointInRoom.y, 'C', humanCount));
        break;
      default:
        break;
    }
}

//collisons
public void detectPlayerFamilyCollision(){
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

      for(SeekBot seekBot : seekBots) {
        if(seekBot.member == human.member) {
          seekBot.pursue = false;
        }
      }
      family.remove(human);
    }
  }
}

//collision
public void detectPlayerPowerUpCollision(){
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

//collsion
public void detectEnemyCollision() {
  float playerX = player.position.x;
  float playerY = player.position.y;
  int playerRadius = player.playerSize;

  if(obstacles.size() > 0) {
    playerObstacleCollision(playerX, playerY, playerRadius);
  }
  if(robots.size() > 0) {
    playerRobotCollision(playerX, playerY, playerRadius);
  }
  if(seekBots.size() > 0) {
    playerSeekBotCollision(playerX, playerY, playerRadius);
  }
}

//collision
public void playerObstacleCollision(float playerX, float playerY, int playerSize){
  for(Obstacle obstacle : new ArrayList<Obstacle>(obstacles)) {
    float obstacleX = obstacle.position.x;
    float obstacleY = obstacle.position.y;
    int obstacleSize = obstacle.size;

    if(playerX > obstacleX - obstacleSize && playerX < obstacleX + obstacleSize) {
      if(playerY > obstacleY - obstacleSize && playerY < obstacleY + obstacleSize) {
        obstacles.remove(obstacle);
        if(!invinciblePowerUp) {
          lives--;
          resetPosition();
        }
      }
    }
  }
}

//reset player
public void resetPosition(){
    Room spawnRoom = map.rooms.get(0);
    player.position.x = spawnRoom.position.x + spawnRoom.width/2;
    player.position.y = spawnRoom.position.y + spawnRoom.height/2;
    player.velocity.x = 0;
    player.velocity.y = 0;
    player.lives = lives;
    hitSound.trigger();
}

//collision
public void playerRobotCollision(float playerX, float playerY, int playerSize){
  for(Robot robot : new ArrayList<Robot>(robots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(playerX - playerSize/2 < robotX + robotSize && playerX + playerSize/2 > robotX) {
      if(playerY - playerSize/2 < robotY + robotSize && playerY + playerSize/2 > robotY) {
        robots.remove(robot);
        if(!invinciblePowerUp) {
          lives--;
          resetPosition();
        }
      }
    }
  }
}

//collsion
public void playerSeekBotCollision(float playerX, float playerY, int playerSize){
  for(SeekBot robot : new ArrayList<SeekBot>(seekBots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(playerX - playerSize/2 < robotX + robotSize && playerX + playerSize/2 > robotX) {
      if(playerY - playerSize/2 < robotY + robotSize && playerY + playerSize/2 > robotY) {
        for(int i = 0; i < family.size(); i ++) {
          if(robot.member == family.get(i).member) {
            family.get(i).flee = false;
          }
        }
        seekBots.remove(robot);
        if(!invinciblePowerUp) {
          lives--;
          resetPosition();
        }
      }
    }
  }
}

//collsion
public void detectBulletCollision(){
  for(Bullet bullet : new ArrayList<Bullet>(bullets)){

    if(bullet.enemy) {
      bulletPlayerCollision(bullet);
    }

    if(obstacles.size() > 0) {
      bulletObstacleCollision(bullet);
    }

    if(robots.size() > 0) {
      bulletRobotCollision(bullet);
    }
  }
}

public void bulletPlayerCollision(Bullet bullet) {
  float bulletX = bullet.position.x;
  float bulletY = bullet.position.y;
  float playerX = player.position.x;
  float playerY = player.position.y;
  float playerSize = player.playerSize;

  if(dist(bulletX, bulletY, playerX, playerY) < playerSize/2) {

    if(!invinciblePowerUp) {
      lives--;
      resetPosition();
    }

    bullets.remove(bullet);
  }
}

public void bulletHumanCollision(Bullet bullet) {
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

public void bulletPowerUpCollision(Bullet bullet) {
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

public void bulletObstacleCollision(Bullet bullet) {
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

public void bulletRobotCollision(Bullet bullet) {
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

  for(SeekBot robot : new ArrayList<SeekBot>(seekBots)) {
    float robotX = robot.position.x;
    float robotY = robot.position.y;
    int robotSize = robot.size;

    if(bulletX > robotX - robotSize && bulletX < robotX + robotSize) {
      if(bulletY > robotY - robotSize && bulletY < robotY + robotSize) {
        if(!bullet.enemy) {

          for(Human human : family) {
            if(human.member == robot.member) {
              human.flee = false;
            }
          }

          seekBots.remove(robot);
          bullets.remove(bullet);
          score += 20;
        }
      }
    }
  }
}

public void spawnObstacles(){

  for(int i = 1; i < map.rooms.size(); i++) {
    for(int j = 0; j < OBSTACLE_MULTIPLIER + wave; j++) {
      PVector randomPointInRoom = randomPointInRoom(i);
      if(checkSpawnLocation(randomPointInRoom)) {
      spawns.add(randomPointInRoom);
      obstacles.add(new Obstacle(randomPointInRoom.x, randomPointInRoom.y));
    }
  }
}
}

public void spawnRobots() {
  int randomRoomIndex;
  int robotCount = 0;

  while(robotCount < BASE_MULTIPLIER + wave*2) {

      randomRoomIndex = map.randomRoomIndex();
      PVector randomPointInRoom = randomPointInRoom(randomRoomIndex);
      if(checkSpawnLocation(randomPointInRoom)) {
        if(robotCount % 2 == 1) {
          MeleeBot meleeBot = new MeleeBot(randomPointInRoom.x, randomPointInRoom.y, randomRoomIndex);
          robots.add(meleeBot);
          spawns.add(randomPointInRoom);
          robotCount++;
        } else {
          robots.add(new RangedBot(randomPointInRoom.x, randomPointInRoom.y, randomRoomIndex));
          spawns.add(randomPointInRoom);
          robotCount++;
        }
    }
  }
}

public void spawnPowerUps(){
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

public void drawRobots(){
  for(Robot robot : robots) {
    if(robot instanceof MeleeBot) {
      robot.draw(player);
    }
    robot.draw();
  }

  for(int i = 0; i < seekBots.size(); i++) {
    if(seekBots.get(i).pursue == true) {
      for(int j = 0; j < family.size(); j++){
        if(seekBots.get(i).member == family.get(j).member) {
          seekBots.get(i).draw(family.get(j));
        }
      }
    } else {
      seekBots.get(i).draw();
    }
  }
}

public void drawPowerUps(){
  for(PowerUp powerUp : powerUps){
    powerUp.draw();
  }
}

public PVector randomPointInRoom(int randomRoomIndex) {
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

public void drawObstacles() {
  for(Obstacle obstacle : obstacles) {
    obstacle.draw();
  }
}

public void printSpawns() {
  for(PVector spawn : spawns) {
    System.out.println(spawn);
  }
}

public void checkRooms() {
  for(Room room : map.rooms) {
    if(room.position.x + room.width > displayWidth) {
    }
    if(room.position.y + room.height > displayHeight) {
    }
  }
}

public void seekbotandfamily(){
  for(Human human : family) {
    System.out.println(human.seekBotIndex);
  }
  System.out.println();

  for(SeekBot seekBot : seekBots) {
    System.out.println(seekBot.familyIndex);
  }
}
class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/6;
  final int CORRIDOR_SIZE = displayWidth/30;

  Partition partition;
  BSPNode leftChild;
  BSPNode rightChild;
  ArrayList<Room> corridors;

  /*
  This class acts as the node of the BSP Tree
  */
  BSPNode(Partition partition) {
    this.partition = partition;
    this.leftChild = null;
    this.rightChild = null;
  }

  /*
    Splits each node's partition into two smaller partitions
    which are used for its child nodes.
  */
  public boolean split() {
    //split already occurred
    if(leftChild != null || rightChild != null) {
      return false;
    }

    boolean splitHorizontal = randomBoolean();

    //determine the ratio between height and width
    if(partition.width > partition.height && partition.width / partition.height >=1.25f) {
      splitHorizontal = false;
    } else if(partition.height > partition.width && partition.height / partition.width >= 1.25f) {
      splitHorizontal = true;
    }

    //determine the maximum size that a room can be after the split
    int max = (splitHorizontal ? partition.height : partition.width) - MIN_PARTITION_SIZE;

    //exit if too small
    if(max <= MIN_PARTITION_SIZE) {
      return false;
    }

    //random location between smallest and largest room size
    int splitLocation = (int) random(MIN_PARTITION_SIZE, max);

    //assign child nodes by creating two new nodes containing the split partitions
    if(splitHorizontal) {
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, partition.width, splitLocation));
      this.rightChild = new BSPNode(new Partition(partition.position.x, partition.position.y + splitLocation, partition.width, partition.height - splitLocation));
    } else {
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, splitLocation, partition.height));
      this.rightChild = new BSPNode(new Partition(partition.position.x + splitLocation, partition.position.y, partition.width - splitLocation, partition.height));
    }
    return true;
  }

  /*
    Create a randomly sized and positioned room.
    Creates corridors between rooms.
  */
  public void createRooms() {

    if(leftChild != null || rightChild != null) {
      if(leftChild != null) {
        leftChild.createRooms();
      }
      if(rightChild != null) {
        rightChild.createRooms();
      }
      if(leftChild != null && rightChild != null) {
        createCorridor(leftChild.getRoom(), rightChild.getRoom());
      }


    } else {
      PVector roomSize;
      PVector roomPosition;
      //room size between 75 and 90 percent of partition size
      roomSize = new PVector(random(0.75f * partition.width, 0.9f * partition.width), random(0.75f * partition.height, 0.9f * partition.height));
      roomPosition = new PVector(random(partition.position.x + 0.1f * partition.width, partition.position.x + 0.9f * partition.width - roomSize.x), random(partition.position.y + 0.1f * partition.height, partition.position.y + 0.9f * partition.height - roomSize.y));
      partition.room = new Room(roomPosition.x, roomPosition.y, roomSize.x, roomSize.y);
    }
  }

  //get leaf nodes
  public Room getRoom() {
    if(partition.room != null) {
      return partition.room;
    } else {

      Room leftRoom = null;
      Room rightRoom = null;

      if(leftChild != null) {
        leftRoom = leftChild.getRoom();
      }
      if(rightChild != null) {
        rightRoom = rightChild.getRoom();
      }
      if(leftRoom == null && rightRoom == null) {
        return null;
      } else if (rightRoom == null) {
        return leftRoom;
      } else if (leftRoom == null) {
        return rightRoom;
      } else if (randomBoolean()) {
        return leftRoom;
      } else {
        return rightRoom;
      }
    }
  }
  //Create a corridor between two random points in left and right room
  public void createCorridor(Room leftRoom, Room rightRoom) {
    corridors = new ArrayList();

    PVector pointA = new PVector(random(leftRoom.position.x + CORRIDOR_SIZE, leftRoom.position.x + leftRoom.width - 2*CORRIDOR_SIZE),
     random(leftRoom.position.y + CORRIDOR_SIZE, leftRoom.position.y + leftRoom.height - 2*CORRIDOR_SIZE));
    PVector pointB = new PVector(random(rightRoom.position.x + CORRIDOR_SIZE, rightRoom.position.x + rightRoom.width - 2*CORRIDOR_SIZE),
     random(rightRoom.position.y + CORRIDOR_SIZE, rightRoom.position.y + rightRoom.height - 2*CORRIDOR_SIZE));

    float w = pointB.x - pointA.x;
    float h = pointB.y - pointA.y;


    //create two corridors that intercept 
    if(w < 0) {
      if(h < 0) {
        if(randomBoolean()) {
          corridors.add(new Room(pointB.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        }
      } else if (h > 0) {
        if(randomBoolean()) {
          corridors.add(new Room(pointB.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        } else {
          corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
          corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
        }
      } else {
        corridors.add(new Room(pointB.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
      }
    } else if (w > 0) {
        if (h < 0) {
          if (randomBoolean()){
            corridors.add(new Room(pointA.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          }
      } else if (h > 0) {
          if (randomBoolean()) {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointB.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          } else {
            corridors.add(new Room(pointA.x, pointB.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
            corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
          }
        } else {
            corridors.add(new Room(pointA.x, pointA.y, abs(w) + CORRIDOR_SIZE, CORRIDOR_SIZE));
        }
    } else {
      if (h < 0) {
        corridors.add(new Room(pointB.x, pointB.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
      } else if (h > 0) {
        corridors.add(new Room(pointA.x, pointA.y, CORRIDOR_SIZE, abs(h) + CORRIDOR_SIZE));
      }
    }
  }




  public boolean randomBoolean() {
    return random(1) > 0.5f;
  }
}
class BSPTree {

  final int MAX_PARTITION_SIZE = displayWidth/2;

  ArrayList<BSPNode> nodes;

  /*
    Binary Space Partition Tree Structure
    Nodes all contained within an ArrayList
  */
  BSPTree() {
    nodes = new ArrayList();
    Partition base = new Partition(0,0, displayWidth, displayHeight);
    nodes.add(new BSPNode(base));
    generateNodes();
  }

  //generate nodes until no more rooms can be created
  //create rooms and corridors for each leaf node
  public void generateNodes() {
    boolean split = true;

    while(split) {
      split = false;
      for(BSPNode node : new ArrayList<BSPNode>(nodes)) {
        if(node.leftChild == null && node.rightChild == null) {
          if(node.partition.width > MAX_PARTITION_SIZE || node.partition.height > MAX_PARTITION_SIZE ||random75() ) {
            if(node.split()){
              nodes.add(node.leftChild);
              nodes.add(node.rightChild);
              split = true;
            }
          }
        }
      }
    }

    nodes.get(0).createRooms();
  }




  public void printNodes() {
    for(BSPNode node : nodes) {
      System.out.println(node.partition.position + " " + node.partition.height + " " + node.partition.width );
      System.out.println();
    }
  }


  public boolean random75() {
    return random(1) > 0.25f;
  }


}
class BombPowerUp extends PowerUp {

  BombPowerUp(float x, float y) {
    super(x, y);
  }

  public void display(){
    fill(0, 153, 153);
    circle(this.position.x, this.position.y, this.size);
    fill(0);
    textAlign(CENTER, CENTER);
    text('B', position.x, position.y);
  }

  public void draw(){
    display();
  }
}
class Bullet {

  final int topSpeed = displayWidth/300;
  final int bulletSize = displayWidth/300;

  PVector position;
  PVector destination;
  PVector direction;
  PVector velocity;
  PVector acceleration;
  boolean enemy;

  //Bullet act as a simple projectile towards a target
  Bullet(float startX, float startY, float endX, float endY, boolean enemy) {
    this.position = new PVector(startX, startY);
    this.destination = new PVector(endX, endY);
    this.velocity = new PVector(0,0);
    this.direction = calculateDirection();
    this.acceleration = calculateAcceleration();
    this.enemy = enemy;
  }

  //calculate direction of travel
  public PVector calculateDirection() {
    return PVector.sub(destination, position);
  }

  //calculate acceleration
  public PVector calculateAcceleration() {
    PVector a = this.direction.normalize();
    a = this.direction.mult(0.5f);
    return a;
  }

  public void update(){
    velocity.add(acceleration);
    velocity.limit(topSpeed);
    position.add(velocity);
  }

  public void display(){
    stroke(100);
    if(enemy) {
      fill(255, 0, 255);
    } else {
      fill(0,255,255);
    }

    circle(position.x, position.y, bulletSize);
  }

  public void draw(){
    update();
    display();
  }
}
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

  public void integrate(PVector linear) {
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

  public void flee(SeekBot seekBot) {
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


  public void update(SeekBot seekBot) {
    flee(seekBot);
  }

  public void update(){

  }

  public void display(){
    fill(0,255,255);
    circle(position.x, position.y, humanSize);
    fill(0);
    textSize(12);
    textAlign(CENTER, CENTER);
    text(member, position.x, position.y);
  }

  public void draw(SeekBot seekBot) {
    update(seekBot);
    display();
  }

  public void draw() {
    update();
    display();
  }
  public boolean detectBottomEdge() {
   int downY= (int) this.position.y + this.humanSize;
   return downY >= displayHeight;
  }

  public boolean detectLeftEdge(){
   int leftX = (int) this.position.x - this.humanSize;
   return leftX <= 0;
  }

  public boolean detectRightEdge(){
   int rightX = (int) this.position.x + this.humanSize;
   return rightX >= displayWidth;
  }

  public boolean detectTopEdge(){
   int topY = (int) this.position.y - this.humanSize;
   return topY <= 0;

  }

  public int getLeftColor() {
   int leftX = (int) this.position.x - this.humanSize;
   int leftY = (int) this.position.y;
   int leftColor = get(leftX, leftY);
   return leftColor;
  }

  public int getRightColor() {
   int rightX = (int) this.position.x + this.humanSize;
   int rightY = (int) this.position.y;
   int rightColor = get(rightX, rightY);
   return rightColor;
  }

  public int getUpColor() {
   int upX = (int) this.position.x;
   int upY = (int) this.position.y - this.humanSize;
   int upColor = get(upX, upY);
   return upColor;
  }

  public int getDownColor() {
   int downX = (int) this.position.x;
   int downY= (int) this.position.y + this.humanSize;
   int downColor = get(downX, downY);
   return downColor;
  }

  public boolean detectNotBlack(int inColor){
   return inColor != BLACK;
  }
}
class InvinciblePowerUp extends PowerUp {

  InvinciblePowerUp(float x, float y) {
    super(x, y);
  }

  public void display() {
    fill(0, 153, 153);
    circle(this.position.x, this.position.y, this.size);
    fill(0);
    textAlign(CENTER, CENTER);
    text('I', position.x, position.y);
  }

  public void draw() {
    display();
  }
}
class Map {

  BSPTree tree;
  ArrayList<Room> rooms;
  ArrayList<Room> corridors;

  Map() {
    tree = new BSPTree();
    rooms = new ArrayList();
    corridors = new ArrayList();
    addRooms();
  }

  public void draw(){
    for(Room room : rooms) {
      if(rooms.get(0) == room) {
        pushStyle();
        strokeWeight(8);
        stroke( 0,255, 255);
        rect(room.position.x, room.position.y, room.width, room.height);
        popStyle();
      }
      room.draw();
    }
    for(Room corridor : corridors) {
      corridor.draw();
    }
  }

  public void addRooms(){
    for(BSPNode node : tree.nodes) {
      if(node.partition.room != null) {
        rooms.add(node.partition.room);
      }
      if(node.corridors != null) {
        corridors.addAll(node.corridors);
      }
    }
  }

  public int randomRoomIndex() {
    return (int) random(1, rooms.size());
  }

  //useful printmethod;
  public void printRooms(ArrayList<Room> roomList){
    for(Room room : roomList) {
      System.out.println(room.position + " " + room.width + " " + room.height);
    }
    System.out.println();
  }
}
class MeleeBot extends Robot {

  final float MAX_SPEED = displayWidth/750 ;
  final float MAX_ACCEL = 0.1f ;
  final float MAX_ROTATION = PI/4 ;

  boolean pursue;
  PVector linear;
  float rotation;
  PVector direction;
  PVector pursueTarget;

  //Robot that attacks by colliding with player
  MeleeBot(float x, float y, int roomIndex) {
    super(x , y, roomIndex);
    this.pursue = false;
    this.rotation = 0;
    this.linear = new PVector(0,0);
    this.direction = new PVector(0,0);
    this.pursueTarget = new PVector(0,0);
  }

  public void update(Player player) {
    this.ensureRobotInArea();
    if(!pursue) {
      this.wander();
    } else {
      pursue(player);
    }
  }

  public void display() {
    fill(179, 0, 179);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(Player player){
    update(player);
    display();
  }
  //ensure robot stays in area
  public void ensureInArea(){

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

  //pursure code from studres
  public void pursue(Player player) {
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

  //integrate from studres
  public void integrate(PVector targetPos, float angular) {
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
class Obstacle {

  final int OBSTACLE_SIZE = 80;
  final float ROTATION_SPEED = 0.1f;

  PVector position;
  int size;
  float theta;
  float spin;

  Obstacle(float x, float y){
    this.size = displayWidth/OBSTACLE_SIZE;
    this.position = new PVector(x + size/2,y + size/2);
    this.spin = ROTATION_SPEED;
    this.theta = 0;
  }

  public void update(){
    this.theta += this.spin;
  }

  public void display(){
    pushStyle();
    rectMode(CENTER);
    fill( 26, 0, 0);

    pushMatrix();
    translate(position.x, position.y);
    rotate(theta);
    rect(0,0,size, size);
    popMatrix();
    popStyle();
  }

  public void draw(){
    update();
    display();
  }
}
class Partition {
  PVector position;
  int height;
  int width;
  Room room;
  /*
  Represents a space is split into and the area a room may
  be created in
  */
  Partition(float x, float y, int width, int height) {
    this.position = new PVector(x, y);
    this.width = width;
    this.height = height;
    this.room = null;
  }

  public void setRoom(Room room) {
    this.room = room;
  }

  public void draw() {
    fill(255);
    if(room != null) {
      room.draw();
    }
    }
}
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
  public void move(int i) {
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
  public void update() {
    velocity.limit(2*PLAYER_SPEED);
    velocity.x *= 0.90f;
    velocity.y *= 0.90f;
    position.add(velocity);

  }

  public void display() {
    fill(0,255, 255);
    circle(position.x, position.y, playerSize);
  }


  public void draw(){
    update();
    display();
  }
}
class PowerUp {

  final int POWERUP_SIZE = 80;

  PVector position;
  int size;

  PowerUp(float x, float y) {
    this.position = new PVector(x, y);
    this.size = displayWidth/POWERUP_SIZE;
  }

  public void draw(){}
}
class RangedBot extends Robot {

  RangedBot(float x, float  y, int roomIndex) {
    super(x, y, roomIndex);
  }

  public void update(){
    this.ensureRobotInArea();
    this.wander();
  }

  public void display() {
    fill( 255, 0, 255);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(){
    update();
    display();
  }
}
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

  public void draw () {

  }

  public void draw(Player player) {

  }

  public void draw(Human human) {

  }

  //wander randomly - code from example on studres and adjusted
  public void wander(){
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
  public void ensureRobotInArea() {
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

     public boolean detectBottomEdge() {
      int downY= (int) this.position.y + this.size;
      return downY >= displayHeight;
    }

     public boolean detectLeftEdge(){
      int leftX = (int) this.position.x;
      return leftX <= 0;
    }

     public boolean detectRightEdge(){
      int rightX = (int) this.position.x + this.size;
      return rightX >= displayWidth;
    }

     public boolean detectTopEdge(){
      int topY = (int) this.position.y;
      return topY <= 0;

    }

     public int getLeftColor() {
      int leftX = (int) this.position.x;
      int leftY = (int) this.position.y;
      int leftColor = get(leftX, leftY);
      return leftColor;
    }

     public int getRightColor() {
      int rightX = (int) this.position.x + this.size;
      int rightY = (int) this.position.y;
      int rightColor = get(rightX, rightY);
      return rightColor;
    }

     public int getUpColor() {
      int upX = (int) this.position.x;
      int upY = (int) this.position.y ;
      int upColor = get(upX, upY);
      return upColor;
    }

     public int getDownColor() {
      int downX = (int) this.position.x;
      int downY= (int) this.position.y + this.size;
      int downColor = get(downX, downY);
      return downColor;
    }

    public int getPositionColor() {
      int x = (int) this.position.x + this.size/2;
      int y = (int) this.position.y + this.size/2;
      int positionColor = get(x, y);
      return positionColor;
    }

    public boolean detectNotBlack(int inColor){
      return inColor != BLACK;
    }
  }
class Room {
  PVector position;
  int height;
  int width;

  /*
  Represents the playable room for the game.
  */
  Room(float x, float y, float width, float height) {
    this.position = new PVector(x, y);
    this.width = (int) width;
    this.height = (int) height;
  }

  public void draw() {
    fill(255);
    noStroke();
    rect(position.x, position.y, width, height);
  }

  public void printDetails(){
    System.out.println("Left Edge: " + position.x);
    System.out.println("Right Edge: " + (position.x + width));
    System.out.println("Top Edge: " + position.y);
    System.out.println("Botoom Edge: " + (position.y + height));
  }
}
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

  public void update(Human human) {
    ensureRobotInArea();
    pursue(human);
  }

  public void update() {
    wander();
  }

  public void display() {
    fill(102, 0, 102);
    square(this.position.x, this.position.y, this.size);
  }

  public void draw(Human human){
    update(human);
    display();
  }

  public void draw() {
    update();
    display();
  }

  public void pursue(Human human) {
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


  public void integrate(PVector targetPos, float angular) {
    velocity.limit(0.5f);
    this.position.add(this.velocity);


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
  public void settings() {  fullScreen();  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "robotron" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
