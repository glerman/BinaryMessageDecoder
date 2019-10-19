package com.tectonic.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Memory {

  public final byte[] data;
  private final String message;
  private List<Block> reachableBlocks;
  public Block root;

  public Memory(final byte[] data, final List<Block> reachableBlocks, final String message) {
    this.data = data;
    this.reachableBlocks = reachableBlocks;
    this.root = reachableBlocks.get(0);
    this.message = message;
  }

  public List<Block> getReachableBlocks() {
    return reachableBlocks;
  }

  public int blockCount() {
    return reachableBlocks.size();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Memory memory = (Memory) o;
    return Arrays.equals(data, memory.data) &&
            Objects.equals(reachableBlocks, memory.reachableBlocks) &&
            Objects.equals(message, memory.message) &&
            Objects.equals(root, memory.root);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, reachableBlocks, root, message);
  }

  public String getMessage() {
    return message;
  }
}
