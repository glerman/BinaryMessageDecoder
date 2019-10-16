package com.tectonic.input;

import com.google.common.annotations.VisibleForTesting;
import com.tectonic.domain.Memory;
import com.tectonic.domain.RawBlock;
import com.tectonic.domain.VarInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class MemoryScanner {

  private final byte[] data;

  public MemoryScanner(final byte[] data) {
    this.data = data;
  }

  @VisibleForTesting
  VarInt decodeVarint(final int offset) {
    //find varint bytes
    byte moreMask = (byte) 0B1000_0000;
    byte varIntValueMask = (byte) 0B0111_1111;
    List<Byte> varIntValueBytes = new ArrayList<>();
    boolean hasMore = true;
    int currIdx = 0;
    while (hasMore) {
      byte currByte = data[offset + currIdx];
      hasMore = (currByte & moreMask) == moreMask;
      varIntValueBytes.add((byte)(currByte & varIntValueMask));
      currIdx++;
    }
    assert varIntValueBytes.size() > 0 && varIntValueBytes.size() <= 8 : "A varint has between 0 and 8 bytes";
    assert currIdx == varIntValueBytes.size();
    //extract number from bytes
    int value = 0;
    for (int i = 0; i < varIntValueBytes.size(); i++) {
      byte byteVal = varIntValueBytes.get(i);
      assert byteVal >=0;

      long shifted = ((long) byteVal) << 7 * i;
      assert shifted >=0;

      value += shifted;
    }
    return new VarInt(value, varIntValueBytes.size());
  }

  public Memory scan() {

//    List<RawBlock> l = new ArrayList<>();
//    Lists.newArrayList(24,69,120,156,170,211,214,226,237,248).forEach(offset -> l.add(scanBlock(offset)));

    final List<RawBlock> reachablesSorted = findReachableBlocksSorted();
    assert reachablesSorted.size() > 0 : "Must contain root block";
    assert reachablesSorted.stream().mapToInt(b -> b.length).sum() <= data.length : "Reachable blocks length can't be more than memory length";
    final List<RawBlock> unreachableBlocks = findUnreachableBlocks(reachablesSorted);

    return new Memory(data, reachablesSorted, unreachableBlocks, reachablesSorted.get(0));
  }

  private List<RawBlock> findUnreachableBlocks(final List<RawBlock> reachablesSorted) {
    List<RawBlock> unreachables = new ArrayList<>();
    RawBlock currReachable;
    RawBlock nextReachable;

    //Find unreachable between reachable
    for(int i = 0; i < reachablesSorted.size() - 1; i++) {
      currReachable = reachablesSorted.get(i);
      nextReachable = reachablesSorted.get(i + 1);
      int expectedNextReachableOffset = currReachable.offset + currReachable.length;

      if (expectedNextReachableOffset < nextReachable.offset) {
        RawBlock unreachable = scanBlock(expectedNextReachableOffset);
        unreachables.add(unreachable);
        int nextOffset = unreachable.offset + unreachable.length;

        while (nextOffset < nextReachable.offset) {
          unreachable = scanBlock(nextOffset);
          unreachables.add(unreachable);
          nextOffset = unreachable.offset + unreachable.length;
        }
        assert nextOffset == nextReachable.offset;
      } else {
        assert expectedNextReachableOffset == nextReachable.offset;
      }
    }
    //Find unreachable after reachable
    int offset = totalBlocksLength(reachablesSorted, unreachables);

    while (offset < data.length) {
      RawBlock unreachable = scanBlock(offset);
      unreachables.add(unreachable);
      offset += unreachable.length;
    }
    assert offset == data.length : "Entire memory needs to be scanned. Scanned " + offset;
    return unreachables;
  }

  private int totalBlocksLength(List<RawBlock> x, List<RawBlock> y) {
    return x.stream().mapToInt(b -> b.length).sum() +
            y.stream().mapToInt(b -> b.length).sum();
  }

  private RawBlock scanBlock(final int offset) {
    VarInt blockLength = decodeVarint(offset);
    assert blockLength.value >= 1 && blockLength.value <= data.length;

    if (blockLength.value == blockLength.length) { //empty block: no pointers or payload.
      assert blockLength.value == 1 : "Expecting the length of an empty block to be 1";
      return new RawBlock(offset, blockLength, Collections.emptyList(), null);
    }
    int scannedLength = blockLength.length;

    List<VarInt> pointers = new ArrayList<>();

    //scan pointers until we reach the zero byte, or the end of block (may not be a zero byte if there's no payload)
    while (scannedLength < blockLength.value) {
      VarInt pointer = decodeVarint(offset + scannedLength);
      assert pointer.value < data.length;

      scannedLength += pointer.length;
      if (pointer.value == 0) { //optional zero byte reached
        assert pointer.length == 1;
        break;
      }
      pointers.add(pointer);
    }
    if (scannedLength == blockLength.value) { //no payload
      return new RawBlock(offset, blockLength, pointers, null);

    } else if (scannedLength < blockLength.value) { //has payload
      return new RawBlock(offset, blockLength, pointers, scannedLength);

    } else {
      throw new IllegalStateException("Scanned more than block size for block at offset " + offset);
    }
  }

  private List<RawBlock> findReachableBlocksSorted() {

    List<RawBlock> reachables = dfsWithoutRecursion();
    Collections.sort(reachables);
    return reachables;
  }

//  private List<RawBlock> dfs() {
//    Set<Integer> visitedOffsets = new HashSet<>();
//    List<RawBlock> reachables = new ArrayList<>();
//    dfsRecursive(0, visitedOffsets, reachables);
//    return reachables;
//  }
//
//  private void dfsRecursive(int offset, Set<Integer> visitedOffsets, List<RawBlock> reachables) {
//    visitedOffsets.add(offset);
//    RawBlock currBlock = scanBlock(offset);
//    reachables.add(currBlock);
//
//    for (VarInt pointer : currBlock.getPointers()) {
//      if (!visitedOffsets.contains(pointer.value)) {
//        dfsRecursive(pointer.value, visitedOffsets, reachables);
//      }
//    }
//  }

  private List<RawBlock> dfsWithoutRecursion() {
    List<RawBlock> reachables = new ArrayList<>();
    Stack<Integer> stack = new Stack<>();
    Set<Integer> visitedOffsets = new HashSet<>();
    stack.push(0);

    while (!stack.isEmpty()) {
      int offset = stack.pop();


      RawBlock currBlock = scanBlock(offset);
      reachables.add(currBlock);

      for (VarInt pointer : currBlock.getPointers()) {
        if (!visitedOffsets.contains(pointer.value)) {
          stack.push(pointer.value);
        }
        visitedOffsets.add(pointer.value);
      }
    }
    return reachables;
  }


}
