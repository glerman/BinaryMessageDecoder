package com.tectonic.domain;

import java.util.Objects;

public class VarInt {

  public final int value;
  public final int length;

  public VarInt(final int value, final int length) {
    this.value = value;
    this.length = length;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final VarInt varInt = (VarInt) o;
    return value == varInt.value &&
            length == varInt.length;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, length);
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
