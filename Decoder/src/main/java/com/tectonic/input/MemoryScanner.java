package com.tectonic.input;

import com.tectonic.domain.Block;
import com.tectonic.domain.Memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryScanner {

  private final byte[] data;

  public MemoryScanner(final byte[] data) {
    this.data = data;
  }

  public Memory scan() {

    final List<Block> reachables = findReachableBlocksOrdered();
    List<Block> unreachableBlocks = findUnreachableBlocks(reachables);
    new Memory(reachables, unreachableBlocks, reachables.get(0));

    throw new UnsupportedOperationException();
  }

  private List<Block> findUnreachableBlocks(final List<Block> reachables) {
    List<Block> unreachables = new ArrayList<>();
    Block curr;
    Block next;
    for(int i = 0; i < reachables.size() - 1; i++) {
      curr = reachables.get(i);
      next = reachables.get(i + 1);
      if (curr.getOffset() + curr.getLength() < next.getOffset()) {
        //todo
        //there are unreachable block(s) in between
        //add them to unreachable
      }
    }
    return unreachables;
  }


  //scan the data array BFS/DFS
  private List<Block> findReachableBlocksOrdered() {
    //todo: the Block object is problamatic for initial parsing - when we parse root we can't init since we
    //todo: don't have the pointer blocks yet - unless recursion will take care of that somehow
    List<Block> reachables = new ArrayList<>();
    Collections.sort(reachables);
    throw new UnsupportedOperationException();
  }


}
