class BSPNode {

  final int MIN_PARTITION_SIZE = displayWidth/6;
  final int CORRIDOR_SIZE = displayWidth/20;

  Partition partition;
  BSPNode leftChild;
  BSPNode rightChild;
  ArrayList<Room> corridors;


  BSPNode(Partition partition) {
    this.partition = partition;
    this.leftChild = null;
    this.rightChild = null;
  }

  boolean split() {
    //split already occurred
    if(leftChild != null || rightChild != null) {
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
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, partition.width, splitLocation));
      this.rightChild = new BSPNode(new Partition(partition.position.x, partition.position.y + splitLocation, partition.width, partition.height - splitLocation));
    } else {
      this.leftChild = new BSPNode(new Partition(partition.position.x, partition.position.y, splitLocation, partition.height));
      this.rightChild = new BSPNode(new Partition(partition.position.x + splitLocation, partition.position.y, partition.width - splitLocation, partition.height));
    }
    return true;
  }

  void createRooms() {

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
      roomSize = new PVector(random(0.75 * partition.width, 0.9 * partition.width), random(0.75 * partition.height, 0.9 * partition.height));
      roomPosition = new PVector(random(partition.position.x + 0.1 * partition.width, partition.position.x + 0.9 * partition.width - roomSize.x), random(partition.position.y + 0.1 * partition.height, partition.position.y + 0.9 * partition.height - roomSize.y));
      partition.room = new Room(roomPosition.x, roomPosition.y, roomSize.x, roomSize.y);
    }
  }

  Room getRoom() {
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

  void createCorridor(Room leftRoom, Room rightRoom) {
    corridors = new ArrayList();

    PVector pointA = new PVector(random(leftRoom.position.x + CORRIDOR_SIZE, leftRoom.position.x + leftRoom.width - 2*CORRIDOR_SIZE),
     random(leftRoom.position.y + CORRIDOR_SIZE, leftRoom.position.y + leftRoom.height - 2*CORRIDOR_SIZE));
    PVector pointB = new PVector(random(rightRoom.position.x + CORRIDOR_SIZE, rightRoom.position.x + rightRoom.width - 2*CORRIDOR_SIZE),
     random(rightRoom.position.y + CORRIDOR_SIZE, rightRoom.position.y + rightRoom.height - 2*CORRIDOR_SIZE));

    float w = pointB.x - pointA.x;
    float h = pointB.y - pointA.y;

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




  boolean randomBoolean() {
    return random(1) > 0.5;
  }
}
