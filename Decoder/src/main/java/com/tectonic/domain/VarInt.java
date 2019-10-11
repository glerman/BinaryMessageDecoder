package com.tectonic.domain;

public class VarInt {

  public final int value;
  public final int length;

  public VarInt(final int value, final int length) {
    this.value = value;
    this.length = length;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("VarInt{");
    sb.append("value=").append(value);
    sb.append(", length=").append(length);
    sb.append('}');
    return sb.toString();
  }
}
