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

  //useful printmethod;
  void printRooms(ArrayList<Room> roomList){
    for(Room room : roomList) {
      System.out.println(room.position + " " + room.width + " " + room.height);
    }
    System.out.println();
  }
}
