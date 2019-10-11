package com.tectonic.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class RawBlock implements Comparable<RawBlock> {
  public final int offset;
  public final int payloadOffset;
  public final int length;
  private final List<Integer> pointers;

  public RawBlock(final int offset, final int length, final List<Integer> pointers, final int payloadOffset) {
    this.offset = offset;
    this.length = length;
    this.pointers = pointers;
    this.payloadOffset = payloadOffset;
  }

  public int compareTo(final RawBlock other) {
    return offset - other.offset;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final RawBlock rawBlock = (RawBlock) o;
    return offset == rawBlock.offset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset);
  }

  public List<Integer> getPointers() {
    return ImmutableList.copyOf(pointers);
  }
}
