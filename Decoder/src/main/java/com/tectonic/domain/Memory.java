package com.tectonic.domain;

import java.util.List;

public class Memory {

  private List<RawBlock> reachableBlocks;
  private List<RawBlock> unreachableBlocks;
//  private Block[] initBlockOrder; required?
  private RawBlock root;

  public Memory(final List<RawBlock> reachableBlocks, final List<RawBlock> unreachableBlocks, final RawBlock root) {
    this.reachableBlocks = reachableBlocks;
    this.unreachableBlocks = unreachableBlocks;
    this.root = root;
  }
}
