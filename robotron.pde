Map map;

void setup () {
  fullScreen();
  background(0);
  cursor(CROSS);
  map = new Map();
}

void draw () {
  map.draw();
}
