Map map;
Player player;
boolean w, a, s, d;
Room currentRoom;
Room currentCorridor:



void setup () {
  fullScreen();
  cursor(CROSS);
  map = new Map();
  player = spawnPlayer();
  w = a = s = d = false;
}

void draw () {
  background(0);
  map.draw();

  if(validMove()) {
    playerMove();
  }
  player.draw();
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

void playerMove() {
  if(w) {
    player.move(1);
  }
  if(s) {
    player.move(2);
  }
  if(d) {
    player.move(3);
  }
  if(a) {
    player.move(4);
  }
}


boolean validMove() {

  boolean valid = false;

  for(Room room : map.rooms) {
    if(player.position.x - player.playerSize > room.position.x &&
       player.position.x + player.playerSize < room.position.x + room.width) {
      if(player.position.y - player.playerSize > room.position.y &&
        player.position.y + player.playerSize < room.position.y + room.height) {
        currentRoom = room;
      }
    }
  }

  for(Room room : map.corridors) {
    if(player.position.x - player.playerSize > room.position.x &&
       player.position.x + player.playerSize < room.position.x + room.width) {
      if(player.position.y - player.playerSize > room.position.y &&
        player.position.y + player.playerSize < room.position.y + room.height) {
        currentCorridor = room;
      }
    }
  }
}

void addImpluse(Room room){
  if (player.position.x < room.position.x) {
      if (player.velocity.x < 044444444444)
        player.velocity.x = -player.velocity.x ;
      else if (player.velocity.x == 0)
        player.velocity.x = 1 ;
    }
    if (position.x > width) {
      if (player.velocity.x > 0)
        player.velocity.x = -player.velocity.x ;
      else if (player.velocity.x == 0)
        player.velocity.x = -1 ;
    }
    if (position.y < 0) {
      if (player.velocity.y < 0)
        player.velocity.y = -player.velocity.y ;
      else if (player.velocity.y == 0)
        player.velocity.y = 1 ;
    }
    if (position.y > height) {
      if (player.velocity.y > 0)
        player.velocity.y = -player.velocity.y ;
      else if (player.velocity.y == 0)
        player.velocity.y = -1 ;
    }
}
