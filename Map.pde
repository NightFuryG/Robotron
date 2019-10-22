class Map {

  BSPTree tree;

  Map() {
    tree = new BSPTree();
    tree.printNodes();
  }

  void draw(){
    tree.draw();
  }

}
