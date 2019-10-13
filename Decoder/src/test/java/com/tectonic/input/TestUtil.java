package com.tectonic.input;


import java.util.List;

@SuppressWarnings("ForLoopReplaceableByForEach")
class TestUtil {

  static byte[] encodeVarInt(final long x) {
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

  static byte[] encodeBlock(final int length, List<Integer> pointers, byte[] payload, boolean forceZeroByte) {
    int currOffset = 0;
    byte[] block = new byte[length];
    byte[] blockLengthVarInt = encodeVarInt(length);

    //Populate length
    for (int i = 0; i < blockLengthVarInt.length; i++) {
      block[currOffset++] = blockLengthVarInt[i];
    }
    //Populate pointers if not empty
    if (pointers != null && pointers.size() > 0) {
      for (int pointer : pointers) {
        byte[] pointerVarInt = encodeVarInt(pointer);

        for (int i = 0; i < pointerVarInt.length; i++) {
          block[currOffset++] = pointerVarInt[i];
        }
      }
    }
    //Populate payload if not empty
    if (payload != null && payload.length > 0) {
      for (int i = 0; i< payload.length; i++) {
        block[currOffset++] = payload[i];
      }
    } else if (forceZeroByte) {
      block[currOffset] = 0;
    }
    return block;
  }

  static byte[] encodeMemory(final List<byte[]> blocks) {
    int size = blocks.stream().mapToInt(block -> block.length).sum();
    byte[] data = new byte[size];
    int currOffset = 0;

    for (int i=0; i < blocks.size(); i++) {
      byte[] block = blocks.get(i);
      for (int j = 0; j < block.length; j++) {
        data[currOffset++] = block[j];
      }
    }
    return data;
  }


}
