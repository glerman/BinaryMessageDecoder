package com.tectonic.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


//todo: rename to block
public class RawBlock implements Comparable<RawBlock> {
  public final int offset;
  public final Integer payloadOffset;
  public final Integer payloadLength; //todo: remove
  public final int length;
  private final List<VarInt> pointers;

  public RawBlock(final int offset, final VarInt lengthVarInt, final List<VarInt> pointers, final Integer payloadOffset) {
    this.offset = offset;
    this.length = lengthVarInt.value;
    this.pointers = pointers;
    this.payloadOffset = payloadOffset;
    payloadLength = payloadOffset == null ? null : length - payloadOffset;
    if (payloadOffset == null) {
      assert lengthVarInt.value == lengthVarInt.length + pointersLength() ||
              lengthVarInt.value == lengthVarInt.length + pointersLength() + 1;
    } else {
      assert lengthVarInt.value == lengthVarInt.length + pointersLength() + 1 + payloadLength;
    }
  }

  public int compareTo(final RawBlock other) {
    return offset - other.offset;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final RawBlock rawBlock = (RawBlock) o;
    return offset == rawBlock.offset &&
            length == rawBlock.length &&
            Objects.equals(payloadOffset, rawBlock.payloadOffset) &&
            Objects.equals(payloadLength, rawBlock.payloadLength) &&
            Objects.equals(pointers, rawBlock.pointers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, payloadOffset, payloadLength, length, pointers);
  }

  public List<VarInt> getPointers() {
    return ImmutableList.copyOf(pointers);
  }

  public List<Integer> getPointerIntegers() {
    return pointers.stream().map(varInt -> varInt.value).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("RawBlock{");
    sb.append("offset=").append(offset);
    sb.append(", length=").append(length);
    sb.append(", pointersLength=").append(pointersLength());
    sb.append(", payloadOffset=").append(payloadOffset);
    sb.append(", payloadLength=").append(payloadLength);
    sb.append(", pointers=").append(pointers);
    sb.append('}');
    return sb.toString();
  }

  private int pointersLength() {
    return pointers.stream().mapToInt(p -> p.length).sum();
  }
}
