class Map {

  BSPTree tree;
  ArrayList<Room> rooms;

  Map() {
    tree = new BSPTree();
    rooms = new ArrayList();
    addRooms();
  }

  void draw(){
    for(Room room : rooms) {
      room.draw();
    }
  }

  void addRooms(){
    for(BSPNode node : tree.nodes) {
      if(node.partition.room != null) {
        rooms.add(node.partition.room);
      }
      if(node.corridors != null) {
        rooms.addAll(node.corridors);
      }
    }

    System.out.println(rooms.size());
  }



}
