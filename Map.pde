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

  void draw(){
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

  void addRooms(){
    for(BSPNode node : tree.nodes) {
      if(node.partition.room != null) {
        rooms.add(node.partition.room);
      }
      if(node.corridors != null) {
        corridors.addAll(node.corridors);
      }
    }
  }

  int randomRoomIndex() {
    return (int) random(1, rooms.size());
  }

  //useful printmethod;
  void printRooms(ArrayList<Room> roomList){
    for(Room room : roomList) {
      System.out.println(room.position + " " + room.width + " " + room.height);
    }
    System.out.println();
  }
}
