package com.tectonic.domain;

import java.util.List;

public class Memory {

  private List<Block> reachableBlocks;
  private List<Block> unreachableBlocks;
//  private Block[] initBlockOrder; required?
  private Block root;

  public Memory(final List<Block> reachableBlocks, final List<Block> unreachableBlocks, final Block root) {
    this.reachableBlocks = reachableBlocks;
    this.unreachableBlocks = unreachableBlocks;
    this.root = root;
  }
}
