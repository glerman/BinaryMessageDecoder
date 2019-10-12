package com.tectonic.input;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VarIntEncoderTest {

  @Test
  public void testRequiredVarIntBytes() throws Exception {

    Assert.assertEquals(1, VarIntEncoder.requiredVarIntBytes(0));
    Assert.assertEquals(1, VarIntEncoder.requiredVarIntBytes(0b0000_1000));
    Assert.assertEquals(2, VarIntEncoder.requiredVarIntBytes(0b1000_0100));
    Assert.assertEquals(2, VarIntEncoder.requiredVarIntBytes(0b0000_0100_1100_0100));
    Assert.assertEquals(3, VarIntEncoder.requiredVarIntBytes(0b0000_0100_0000_0100_1100_0100));
  }

  @Test
  public void testEncodeDecode() throws Exception {

    Assert.assertEquals(300, new MemoryScanner(VarIntEncoder.encodeVarInt(0b1010_1100_0000_0010)).decodeVarint(0).value);
  }

//  @Test
//  public void requiredVarIntBytes() {
//    final long val = 300;
//    List<Character> c = new ArrayList<>();
//    for (int i = 0; i < 64; i++) {
//      c.add(((byte)(val >> i) & 0B1) == 1 ? '1' : '0');
//    }
//    Collections.reverse(c);
//    c.forEach(System.out::print);
//  }
}
