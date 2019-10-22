class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/5;


  Partition partition;
  Line line;
  BSPNode left;
  BSPNode right;

  BSPNode(Partition partition) {
    this.partition = partition;
    this.left = null;
    this.right = null;
  }

  boolean split() {
    //split already occurred
    if(left != null || right != null) {
      return false;
    }

    boolean splitHorizontal = randomBoolean();

    if(partition.width > partition.height && partition.width / partition.height >=1.25) {
      splitHorizontal = false;
    } else if(partition.height > partition.width && partition.height / partition.width >= 1.25) {
      splitHorizontal = true;
    }

    int max = (splitHorizontal ? partition.height : partition.width) - MIN_PARTITION_SIZE;

    if(max <= MIN_PARTITION_SIZE) {
      return false;
    }

    int splitLocation = (int) random(MIN_PARTITION_SIZE, max);

    if(splitHorizontal) {
      this.left = new BSPNode(new Partition(partition.position.x, partition.position.y, partition.width, splitLocation));
      this.right = new BSPNode(new Partition(partition.position.x, partition.position.y + splitLocation, partition.width, partition.height - splitLocation));
    } else {
      this.left = new BSPNode(new Partition(partition.position.x, partition.position.y, splitLocation, partition.height));
      this.right = new BSPNode(new Partition(partition.position.x + splitLocation, partition.position.y, partition.width - splitLocation, partition.height));
    }
    return true;


  }

  boolean randomBoolean() {
    return random(1) > 0.5;
  }
}
