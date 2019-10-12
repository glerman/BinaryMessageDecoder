package com.tectonic.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class RawBlock implements Comparable<RawBlock> {
  public final int offset;
  public final Integer payloadOffset;
  public final Integer payloadLength;
  public final int length;
  private final List<VarInt> pointers;

  public RawBlock(final int offset, final int length, final List<VarInt> pointers, final Integer payloadOffset) {
    this.offset = offset;
    this.length = length;
    this.pointers = pointers;
    this.payloadOffset = payloadOffset;
    payloadLength = payloadOffset == null ? null : length - payloadOffset;
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

  public List<VarInt> getPointers() {
    return ImmutableList.copyOf(pointers);
  }

//  @Override
//  public String toString() {
//    final StringBuilder sb = new StringBuilder("RawBlock{");
//    sb.append("offset=").append(offset);
//    sb.append(", payloadOffset=").append(payloadOffset);
//    sb.append(", payloadLength=").append(payloadLength);
//    sb.append(", length=").append(length);
//    sb.append(", pointers=").append(pointers);
//    sb.append('}');
//    return sb.toString();
//  }
}
