package com.tectonic.input;

import com.google.common.annotations.VisibleForTesting;
import com.tectonic.domain.Block;
import com.tectonic.domain.Memory;
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

    final List<Block> reachablesSorted = findReachableBlocksSorted();
    assert reachablesSorted.size() > 0 : "Must contain root block";
    assert reachablesSorted.stream().mapToInt(b -> b.length).sum() <= data.length : "Reachable blocks length can't be more than memory length";
    String message = findMessage(reachablesSorted);

    return new Memory(data, reachablesSorted, message);
  }

  private String findMessage(final List<Block> reachablesSorted) {
    Block currReachable;
    Block nextReachable;
    StringBuilder messageBuilder = new StringBuilder();
    //Find unused between reachable
    for(int i = 0; i < reachablesSorted.size() - 1; i++) {
      currReachable = reachablesSorted.get(i);
      nextReachable = reachablesSorted.get(i + 1);
      int expectedNextReachableOffset = currReachable.offset + currReachable.length;

      if (expectedNextReachableOffset < nextReachable.offset) {
        scanMessage(messageBuilder, expectedNextReachableOffset, nextReachable.offset);
      } else {
        assert expectedNextReachableOffset == nextReachable.offset;
      }
    }
    //Find unused after reachable
    int reachableBlocksLength = reachablesSorted.stream().mapToInt(block -> block.length).sum();
    int messageOffset = reachableBlocksLength + messageBuilder.length();
    scanMessage(messageBuilder, messageOffset, data.length);

    return messageBuilder.toString();
  }

  private void scanMessage(final StringBuilder sb, int messageOffset, int messageEnd) {
    while (messageOffset < messageEnd) {
      sb.append((char) data[messageOffset]);
      messageOffset++;
    }
  }

  private Block scanBlock(final int offset) {
    VarInt blockLength = decodeVarint(offset);
    assert blockLength.value >= 1 && blockLength.value <= data.length;

    if (blockLength.value == blockLength.length) { //empty block: no pointers or payload.
      assert blockLength.value == 1 : "Expecting the length of an empty block to be 1";
      return new Block(offset, blockLength, Collections.emptyList(), null);
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
      return new Block(offset, blockLength, pointers, null);

    } else if (scannedLength < blockLength.value) { //has payload
      return new Block(offset, blockLength, pointers, scannedLength);

    } else {
      throw new IllegalStateException("Scanned more than block size for block at offset " + offset);
    }
  }

  private List<Block> findReachableBlocksSorted() {

    List<Block> reachables = dfs();
    Collections.sort(reachables);
    return reachables;
  }

  private List<Block> dfs() {
    List<Block> reachables = new ArrayList<>();
    Stack<Integer> stack = new Stack<>();
    Set<Integer> visitedOffsets = new HashSet<>();
    stack.push(0);

    while (!stack.isEmpty()) {
      int offset = stack.pop();

      Block currBlock = scanBlock(offset);
      reachables.add(currBlock);

      for (Integer pointer : currBlock.getPointerIntegers()) {
        if (!visitedOffsets.contains(pointer)) {
          stack.push(pointer);
        }
        visitedOffsets.add(pointer);
      }
    }
    return reachables;
  }


}
