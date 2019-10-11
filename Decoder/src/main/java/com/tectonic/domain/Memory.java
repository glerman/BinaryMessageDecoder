package com.tectonic.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;

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
}
