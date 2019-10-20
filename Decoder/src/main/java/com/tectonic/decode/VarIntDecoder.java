package com.tectonic.decode;

import com.tectonic.domain.VarInt;

public class VarIntDecoder {

  public static VarInt decodeVarint(final byte[] data, final int offset) {

    byte moreMask = (byte) 0B1000_0000;
    byte varIntValueMask = (byte) 0B0111_1111;
    boolean hasMore = true;
    int currIdx = 0;
    int value = 0;

    while (hasMore) {
      byte currByte = data[offset + currIdx];
      hasMore = (currByte & moreMask) == moreMask;
      byte valueByte = (byte) (currByte & varIntValueMask);
      value += ((long) valueByte) << 7 * currIdx;
      currIdx++;
    }
    assert currIdx > 0 && currIdx <= 8 : "A varint has between 0 and 8 bytes";
    return new VarInt(value, currIdx);
  }
}
