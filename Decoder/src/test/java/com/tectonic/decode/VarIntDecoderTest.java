package com.tectonic.decode;

import com.tectonic.domain.VarInt;
import org.junit.Assert;
import org.junit.Test;

public class VarIntDecoderTest {

  @Test
  public void testEncodeDecode() throws Exception {

    Assert.assertEquals(
            new VarInt(0, 1),
            VarIntDecoder.decodeVarint(new byte[]{0, 2, 3}, 0)
    );
    Assert.assertEquals(
            new VarInt(2, 1),
            VarIntDecoder.decodeVarint(new byte[]{1, 2, 3}, 1)
    );
    Assert.assertEquals(
            new VarInt(0, 1),
            VarIntDecoder.decodeVarint(new byte[]{1, 2, 0}, 2)
    );
    Assert.assertEquals(
            new VarInt(300, 2),
            VarIntDecoder.decodeVarint(new byte[]{(byte)0b1010_1100, (byte)0b0000_0010, 17}, 0)
    );
  }

}
