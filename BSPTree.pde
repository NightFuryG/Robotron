class BSPTree {

  final int MAX_PARTITION_SIZE = displayWidth/2;

  ArrayList<BSPNode> nodes;

  BSPTree() {
    nodes = new ArrayList();
    Partition base = new Partition(0,0, displayWidth, displayHeight);
    nodes.add(new BSPNode(base));
    generateNodes();
  }

  void generateNodes() {
    boolean split = true;

    printNodes();

    while(split) {
      split = false;
      for(BSPNode node : new ArrayList<BSPNode>(nodes)) {
        if(node.left == null && node.right == null) {
          if(node.partition.width > MAX_PARTITION_SIZE || node.partition.height > MAX_PARTITION_SIZE ||random75() ) {
            if(node.split()){
              nodes.add(node.left);
              nodes.add(node.right);
              split = true;
            }
          }
        }
      }
    }
  }

  void printNodes() {
    for(BSPNode node : nodes) {
      System.out.println(node.partition.position + " " + node.partition.height + " " + node.partition.width );
      System.out.println();
    }
  }

  void draw() {
    for(BSPNode node : nodes) {
      node.partition.draw();
    }
  }

  boolean random75() {
    return random(1) > 0.25;
  }


}
