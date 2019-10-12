package com.tectonic.input;

import com.tectonic.domain.Memory;
import com.tectonic.domain.RawBlock;
import com.tectonic.domain.VarInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryScanner {

  private final byte[] data;

  public MemoryScanner(final byte[] data) {
    this.data = data;
  }

  public static void main(String[] args) {
    byte[] bytes = {(byte)0B10101100, (byte)0B00000010};
    MemoryScanner memoryScanner = new MemoryScanner(bytes);
    VarInt varint = memoryScanner.decodeVarint(0);
    System.out.println((byte)0B10000000 & (byte) 0B11000000);
    System.out.println(((byte)0B10000000 & (byte) 0B10101100) == (byte)0B10000000);
    System.out.println(varint);

    byte[] b0 = {0};
    VarInt varint2 = new MemoryScanner(b0).decodeVarint(0);
    System.out.println(varint2);

    byte[] b7 = {7};
    VarInt varin7 = new MemoryScanner(b7).decodeVarint(0);
    System.out.println(varin7);

    byte[] b1 = {(byte)0b01111111};
    VarInt varin1 = new MemoryScanner(b1).decodeVarint(0);
    System.out.println(varin1); //127

    byte[] b3 = {(byte)0b10000000, (byte)0b00000001};
    VarInt varin3 = new MemoryScanner(b3).decodeVarint(0);
    System.out.println(varin3); //26

  }

  VarInt decodeVarint(final int offset) {
    byte moreMask = (byte) 0B10000000;
    byte varIntValueMask = (byte) 0B01111111;
    List<Byte> varIntValueBytes = new ArrayList<>();
    boolean hasMore = true;
    int curr = 0;
    while (hasMore) {
      hasMore = (data[offset + curr] & moreMask) == moreMask;
      varIntValueBytes.add((byte)(data[offset + curr] & varIntValueMask));
      curr++;
    }
    assert varIntValueBytes.size() <= 8 : "A varint has 8 bytes at most";
    int value = 0;
    for (int i = 0; i < varIntValueBytes.size(); i++) {
      value += ((long)varIntValueBytes.get(i)) << 7*i;
    }
    return new VarInt(value, varIntValueBytes.size());
  }

  public Memory scan() {

    final List<RawBlock> reachablesSorted = findReachableBlocksSorted();
    assert reachablesSorted.size() > 0 : "Must contain root block";
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
    assert offset == data.length : "Entire memory needs to be scanned";
    return unreachables;
  }

  private int totalBlocksLength(List<RawBlock> x, List<RawBlock> y) {
    return x.stream().mapToInt(b -> b.length).sum() +
            y.stream().mapToInt(b -> b.length).sum();
  }

  private RawBlock scanBlock(final int offset) {
    VarInt blockLength = decodeVarint(offset);

    if (blockLength.value == blockLength.length) { //empty block: no pointers or payload.
      assert blockLength.value == 1 : "Expecting the length of an empty block to be 1";
      return new RawBlock(offset, blockLength.value, Collections.emptyList(), null);
    }
    int scannedLength = blockLength.length;

    List<VarInt> pointers = new ArrayList<>();

    //scan pointers until we reach the zero byte, or the end of block (may not be a zero byte if there's no payload)
    while (scannedLength < blockLength.value) {
      VarInt pointer = decodeVarint(offset + scannedLength);
      scannedLength += pointer.length;
      if (pointer.value == 0) { //optional zero byte reached
        assert pointer.length == 1;
        break;
      }
      pointers.add(pointer);
    }
    if (scannedLength == blockLength.value) { //block has pointers but no payload
      return new RawBlock(offset, blockLength.value, pointers, null);

    } else if (scannedLength < blockLength.value) { //block has payload and (maybe) pointers
      return new RawBlock(offset, blockLength.value, pointers, scannedLength);

    } else {
      throw new IllegalStateException("Scanned more than block size for block at offset " + offset);
    }
  }

  //scan the data array BFS/DFS
  private List<RawBlock> findReachableBlocksSorted() {

    List<RawBlock> reachables = dfs();
    Collections.sort(reachables);
    return reachables;
  }


  private List<RawBlock> dfs() {
    Set<Integer> visitedOffsets = new HashSet<>();
    List<RawBlock> reachables = new ArrayList<>();
    dfsRecursive(0, visitedOffsets, reachables);
    return reachables;
  }

  private void dfsRecursive(int offset, Set<Integer> visitedOffsets, List<RawBlock> reachables) {
    visitedOffsets.add(offset);
    RawBlock currBlock = scanBlock(offset);
    reachables.add(currBlock);

    for (VarInt pointer : currBlock.getPointers()) {
      if (!visitedOffsets.contains(pointer.value)) {
        dfsRecursive(pointer.value, visitedOffsets, reachables);
      }
    }
  }


}
