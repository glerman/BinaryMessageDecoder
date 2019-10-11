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

  public static void main(String[] args) {
    byte[] bytes = {(byte)0B10101100, (byte)0B00000010};
    MemoryScanner memoryScanner = new MemoryScanner(bytes);
    int val = memoryScanner.decodeVarint();
    System.out.println((byte)0B10000000 & (byte) 0B11000000);
    System.out.println(((byte)0B10000000 & (byte) 0B10101100) == (byte)0B10000000);
    System.out.println(val);
  }

  class RawBlock {
    private final int offset;
    private final int length;
    private final List<Integer> pointerOffsets;
    private final byte[] payload;

    public RawBlock(final int offset, final int length, final List<Integer> pointerOffsets, final byte[] payload) {
      this.offset = offset;
      this.length = length;
      this.pointerOffsets = pointerOffsets;
      this.payload = payload;
    }
  }

  int decodeVarint() {
    byte moreMask = (byte) 0B10000000;
    byte varIntValueMask = (byte) 0B01111111;
    List<Byte> varIntValueBytes = new ArrayList<>();
    boolean hasMore = true;
    int curr = 0;
    while (hasMore) {
      hasMore = (data[curr] & moreMask) == moreMask;
      varIntValueBytes.add((byte)(data[curr] & varIntValueMask));
      curr++;
    }
    Collections.reverse(varIntValueBytes);
    int value = 0;
    for (int i = 0; i < varIntValueBytes.size(); i++) {
      value += ((int)varIntValueBytes.get(i)) << 8*i;
    }
    return value;
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
