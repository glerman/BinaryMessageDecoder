package com.tectonic.input;

import com.google.common.collect.Lists;
import com.tectonic.domain.Memory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MemoryScannerTest {

  @Test
  public void testTrivialMemory() throws Exception {

    byte[] rootBlock = TestUtil.encodeBlock(1, null, null, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(memory.data, rootBlock);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(Collections.emptyList(), memory.root.getPointers());
    Assert.assertNull(memory.root.payloadLength);
    Assert.assertNull(memory.root.payloadOffset);
  }

  @Test
  public void testTwoBlocksNoPayload() throws Exception {
    List<Integer> pointers = Lists.newArrayList(2);
    byte[] rootBlock = TestUtil.encodeBlock(2, pointers, null, false);
    byte[] secondBlock = TestUtil.encodeBlock(1, null, null, false);
    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, secondBlock));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(memory.data.length, rootBlock.length + secondBlock.length);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals(2, memory.blockCount());

    Assert.assertEquals(Collections.emptyList(), memory.getUnreachableBlocks());
    Assert.assertEquals(2, memory.getReachableBlocks().size());
  }

  @Test
  public void testMiddleBlockUnreachable() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(3, 12);
    byte[] rootBlock = TestUtil.encodeBlock(3, pointers, null, false);
    byte[] reachable1 = TestUtil.encodeBlock(2, null, null, true);
    byte[] unreachable = TestUtil.encodeBlock(7, null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(2, null, null, true);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(1, memory.getUnreachableBlocks().size());
    Assert.assertEquals(3, memory.getReachableBlocks().size());
  }
}
