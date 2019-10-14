package com.tectonic.domain;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Memory {

  public final byte[] data;
  private List<RawBlock> reachableBlocks;
  private List<RawBlock> unreachableBlocks;
//  private Block[] initBlockOrder; required?
  public RawBlock root;

  public Memory(final byte[] data, final List<RawBlock> reachableBlocks, final List<RawBlock> unreachableBlocks, final RawBlock root) {
    this.data = data;
    this.reachableBlocks = reachableBlocks;
    this.unreachableBlocks = unreachableBlocks;
    this.root = root;
  }

  public List<RawBlock> getUnreachableBlocks() {
    return ImmutableList.copyOf(unreachableBlocks);
  }

  public List<RawBlock> getReachableBlocks() {
    return reachableBlocks;
  }

  public int blockCount() {
    return reachableBlocks.size() + unreachableBlocks.size();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Memory memory = (Memory) o;
    return Arrays.equals(data, memory.data) &&
            Objects.equals(reachableBlocks, memory.reachableBlocks) &&
            Objects.equals(unreachableBlocks, memory.unreachableBlocks) &&
            Objects.equals(root, memory.root);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, reachableBlocks, unreachableBlocks, root);
  }
}
