package com.tectonic.input;


public class VarIntEncoder {

  public static byte[] encodeVarInt(final long x) {
    byte moreMask = (byte) 0b1000_0000;
    byte noMoreMask = (byte) 0b0111_1111;
    byte valMask = (byte) 0b0111_1111;
    int bytesCount = requiredVarIntBytes(x);
    byte[] varInt = new byte[bytesCount];

    for (int i = 0; i < bytesCount; i++) {
      byte b = (byte)(((byte) (x >> (7 * i))) & valMask);
      byte modified = i < bytesCount - 1 ? (byte)(moreMask | b) : (byte)(noMoreMask & b);
      varInt[i] = modified;
    }
    return varInt;
  }

  static int requiredVarIntBytes(final long val) {
    if (val == 0) return 1;
    int i = 0;
    for (; i < 64; i++) {
      if (val >> i <= 0) {
        break;
      }
    }
    return 1 + (i-1)/7;
  }
}
