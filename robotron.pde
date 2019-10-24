Map map;
Player player;
boolean w, a, s, d;



void setup () {
  fullScreen();
  cursor(CROSS);
  map = new Map();
  player = new Player(displayWidth/2, displayHeight/2);
  w = a = s = d = false;
}

void draw () {
  background(0);
  map.draw();
  playerMove();
  player.draw();
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

// boolean validMove() {
//
// }
