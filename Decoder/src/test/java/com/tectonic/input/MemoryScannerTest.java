package com.tectonic.input;

import com.google.common.collect.Lists;
import com.tectonic.decode.MessageExtractor;
import com.tectonic.domain.Memory;
import com.tectonic.domain.RawBlock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MemoryScannerTest {

  @Test
  public void testTrivialMemory() throws Exception {
    byte[] payload = "hello".getBytes();
    byte[] rootBlock = TestUtil.encodeBlock(7, null, payload, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(memory.data, rootBlock);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(Collections.emptyList(), memory.root.getPointers());
    Assert.assertEquals(5, memory.root.payloadLength.intValue());
    Assert.assertEquals(2, memory.root.payloadOffset.intValue());
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
  public void testTwoBlocksWithPayload() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(8);
    byte[] rootBlock = TestUtil.encodeBlock(8, pointers, payload, false);
    byte[] secondBlock = TestUtil.encodeBlock(8, pointers, payload, false);
    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, secondBlock));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(memory.data.length, rootBlock.length + secondBlock.length);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals(2, memory.blockCount());

    Assert.assertEquals(Collections.emptyList(), memory.getUnreachableBlocks());
    Assert.assertEquals(2, memory.getReachableBlocks().size());

    RawBlock actualSecondBlock = memory.getReachableBlocks().get(1);
    Assert.assertEquals(pointers, actualSecondBlock.getPointerIntegers());
    Assert.assertEquals(8, actualSecondBlock.offset);
    Assert.assertEquals(5, actualSecondBlock.payloadLength.intValue());
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

    RawBlock actualUnreachable = memory.getUnreachableBlocks().get(0);
    Assert.assertEquals(5, actualUnreachable.payloadLength.intValue());
    Assert.assertTrue(actualUnreachable.getPointers().isEmpty());
  }


  @Test
  public void testMiddleBlockUnreachableWithPayloads() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(9, 24);
    byte[] rootBlock = TestUtil.encodeBlock(9, pointers, payload, false);
    byte[] reachable1 = TestUtil.encodeBlock(8, null, payload, false);
    byte[] unreachable = TestUtil.encodeBlock(7, null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(7, null, payload, false);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(1, memory.getUnreachableBlocks().size());
    RawBlock actualUnreachable = memory.getUnreachableBlocks().get(0);
    Assert.assertEquals(7, actualUnreachable.length);
    Assert.assertEquals(17, actualUnreachable.offset);
    Assert.assertEquals(2, actualUnreachable.payloadOffset.intValue());
    Assert.assertEquals(5, actualUnreachable.payloadLength.intValue());
    Assert.assertEquals(3, memory.getReachableBlocks().size());
  }

  @Test
  public void testMessageDecode() throws Exception {

    byte[] payload = "hello".getBytes();
    Assert.assertEquals(5, payload.length);
    List<Integer> pointers = Lists.newArrayList(3, 12);
    byte[] rootBlock = TestUtil.encodeBlock(3, pointers, null, false);
    byte[] reachable1 = TestUtil.encodeBlock(2, null, null, true);
    byte[] unreachable = TestUtil.encodeBlock(7, null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(2, null, null, true);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();


    String message = new MessageExtractor(memory).extract();
    Assert.assertEquals("hello", message);
  }
}
