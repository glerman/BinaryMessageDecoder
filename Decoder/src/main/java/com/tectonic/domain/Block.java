package com.tectonic.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Block implements Comparable<Block> {

  public final int offset;
  public final Integer payloadOffset;
  public final int length;
  private final List<VarInt> pointers;

  public Block(final int offset, final VarInt lengthVarInt, final List<VarInt> pointers, final Integer payloadOffset) {
    this.offset = offset;
    this.length = lengthVarInt.value;
    this.pointers = pointers;
    this.payloadOffset = payloadOffset;

    if (payloadOffset == null) {
      assert lengthVarInt.value == lengthVarInt.length + pointersLength() ||
              lengthVarInt.value == lengthVarInt.length + pointersLength() + 1;
    } else {
      int payloadLength = length - payloadOffset;
      assert lengthVarInt.value == lengthVarInt.length + pointersLength() + 1 + payloadLength;
    }
  }

  public int compareTo(final Block other) {
    return offset - other.offset;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Block block = (Block) o;
    return offset == block.offset &&
            length == block.length &&
            Objects.equals(payloadOffset, block.payloadOffset) &&
            Objects.equals(pointers, block.pointers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, payloadOffset, length, pointers);
  }

  public List<VarInt> getPointers() {
    return pointers;
  }

  public List<Integer> getPointerIntegers() {
    return pointers.stream().map(varInt -> varInt.value).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Block{");
    sb.append("offset=").append(offset);
    sb.append(", length=").append(length);
    sb.append(", pointersLength=").append(pointersLength());
    sb.append(", payloadOffset=").append(payloadOffset);
    sb.append(", pointers=").append(pointers);
    sb.append('}');
    return sb.toString();
  }

  private int pointersLength() {
    return pointers.stream().mapToInt(p -> p.length).sum();
  }
}
