package com.tectonic.domain;

import java.util.List;
import java.util.Objects;

//todo: make getters immutable?
public class Block implements Comparable<Block> {

  private final int offset;
  private final int length;
  private final List<Block> pointers;
  private final byte[] payload;

  public Block(final int offset, final int length, final List<Block> pointers, final byte[] payload) {
    this.offset = offset;
    this.length = length;
    this.pointers = pointers;
    this.payload = payload;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public List<Block> getPointers() {
    return pointers;
  }

  public byte[] getPayload() {
    return payload;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Block block = (Block) o;
    return offset == block.offset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset);
  }

  @Override
  public int compareTo(final Block other) {
    return offset - other.offset;
  }
}
