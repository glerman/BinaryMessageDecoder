package com.tectonic.input;

import com.tectonic.decode.VarIntDecoder;
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
    VarInt blockLength = VarIntDecoder.decodeVarint(data, offset);
    assert blockLength.value >= 1 && blockLength.value <= data.length;

    if (blockLength.value == blockLength.length) { //empty block: no pointers or payload.
      assert blockLength.value == 1 : "Expecting the length of an empty block to be 1";
      return new Block(offset, blockLength, Collections.emptyList(), null);
    }
    int scannedLength = blockLength.length;

    List<VarInt> pointers = new ArrayList<>();

    //scan pointers until we reach the zero byte, or the end of block (may not be a zero byte if there's no payload)
    while (scannedLength < blockLength.value) {
      VarInt pointer = VarIntDecoder.decodeVarint(data,offset + scannedLength);
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
