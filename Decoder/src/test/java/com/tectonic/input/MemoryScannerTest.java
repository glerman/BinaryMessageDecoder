package com.tectonic.input;

import com.google.common.collect.Lists;
import com.tectonic.decode.MessageExtractor;
import com.tectonic.domain.Memory;
import com.tectonic.domain.RawBlock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemoryScannerTest {

  @Test
  public void testTrivialBlock() throws Exception {
    byte[] rootBlock = TestUtil.encodeBlock(null, null, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(1, memory.getReachableBlocks().size());
    Assert.assertTrue(memory.getUnreachableBlocks().isEmpty());
    Assert.assertEquals(1, memory.root.length);
    Assert.assertEquals(0, memory.root.offset);
    Assert.assertNull(memory.root.payloadOffset);
    Assert.assertNull(memory.root.payloadLength);
    Assert.assertTrue(memory.root.getPointers().isEmpty());
  }


  @Test
  public void testTrivialBlockWithZeroByte() throws Exception {
    byte[] rootBlock = TestUtil.encodeBlock(null, null, true);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(1, memory.getReachableBlocks().size());
    Assert.assertTrue(memory.getUnreachableBlocks().isEmpty());
    Assert.assertEquals(2, memory.root.length);
    Assert.assertEquals(0, memory.root.offset);
    Assert.assertNull(memory.root.payloadOffset);
    Assert.assertNull(memory.root.payloadLength);
    Assert.assertTrue(memory.root.getPointers().isEmpty());
  }


  @Test
  public void testPayLoadBock() throws Exception {
    byte[] payload = "hello".getBytes();
    byte[] rootBlock = TestUtil.encodeBlock(null, payload, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(memory.data, rootBlock);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(Collections.emptyList(), memory.root.getPointers());
    Assert.assertEquals(5, memory.root.payloadLength.intValue());
    Assert.assertEquals(2, memory.root.payloadOffset.intValue());

    byte[] actualPayload = Arrays.copyOfRange(rootBlock, memory.root.payloadOffset, rootBlock.length);
    Assert.assertTrue(Arrays.equals(payload, actualPayload));
  }

  @Test
  public void testTwoBlocksNoPayload() throws Exception {
    List<Integer> pointers = Lists.newArrayList(2);
    byte[] rootBlock = TestUtil.encodeBlock(pointers, null, false);
    byte[] secondBlock = TestUtil.encodeBlock(null, null, false);
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
    byte[] rootBlock = TestUtil.encodeBlock(pointers, payload, false);
    byte[] secondBlock = TestUtil.encodeBlock(pointers, payload, false);
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
    byte[] payload = "hello".getBytes();
    List<Integer> pointers = Lists.newArrayList(3, 12);
    byte[] rootBlock = TestUtil.encodeBlock(pointers, null, false);
    byte[] reachable1 = TestUtil.encodeBlock(null, null, true);
    byte[] unreachable = TestUtil.encodeBlock(null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(null, null, true);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(1, memory.getUnreachableBlocks().size());
    Assert.assertEquals(3, memory.getReachableBlocks().size());

    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    RawBlock actualUnreachable = memory.getUnreachableBlocks().get(0);
    Assert.assertEquals(5, actualUnreachable.payloadLength.intValue());
    Assert.assertTrue(actualUnreachable.getPointers().isEmpty());

    Assert.assertEquals("hello", new MessageExtractor(memory).extract());
  }


  @Test
  public void testMiddleBlockUnreachableWithPayloads() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(9, 23);
    byte[] rootBlock = TestUtil.encodeBlock(pointers, payload, false);
    byte[] reachable1 = TestUtil.encodeBlock(null, payload, false);
    byte[] unreachable = TestUtil.encodeBlock(null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(null, payload, false);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(1, memory.getUnreachableBlocks().size());
    RawBlock actualUnreachable = memory.getUnreachableBlocks().get(0);
    Assert.assertEquals(7, actualUnreachable.length);
    Assert.assertEquals(16, actualUnreachable.offset);
    Assert.assertEquals(2, actualUnreachable.payloadOffset.intValue());
    Assert.assertEquals(5, actualUnreachable.payloadLength.intValue());
    Assert.assertEquals(3, memory.getReachableBlocks().size());
  }

  @Test
  public void testMessageDecode() throws Exception {

    byte[] payload = "hello".getBytes();
    Assert.assertEquals(5, payload.length);
    List<Integer> pointers = Lists.newArrayList(3, 12);
    byte[] rootBlock = TestUtil.encodeBlock(pointers, null, false);
    byte[] reachable1 = TestUtil.encodeBlock(null, null, true);
    byte[] unreachable = TestUtil.encodeBlock(null, payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(null, null, true);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();


    String message = new MessageExtractor(memory).extract();
    Assert.assertEquals("hello", message);
  }

  @Test
  public void testSelfReferencingBlock() throws Exception {

    byte[] payload = "hello".getBytes();

    byte[] rootBlock = TestUtil.encodeBlock(Lists.newArrayList(6, 19), new byte[2], false);
    byte[] reachable1 = TestUtil.encodeBlock(Lists.newArrayList(6, 19), null, true);
    byte[] unreachable = TestUtil.encodeBlock(Lists.newArrayList(6, 19), payload, false);
    byte[] reachable2 = TestUtil.encodeBlock(Lists.newArrayList(6, 19, 19), null, false);

    byte[] data = TestUtil.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();
    Assert.assertEquals(23, memory.data.length);
    Assert.assertEquals(3, memory.getReachableBlocks().size());
    Assert.assertEquals(1, memory.getUnreachableBlocks().size());
    Assert.assertEquals(Lists.newArrayList(6, 19), memory.root.getPointerIntegers());
    Assert.assertEquals(Lists.newArrayList(6, 19), memory.getReachableBlocks().get(1).getPointerIntegers());
    Assert.assertEquals(Lists.newArrayList(6, 19, 19), memory.getReachableBlocks().get(2).getPointerIntegers());
    RawBlock actualUnreachable = memory.getUnreachableBlocks().get(0);
    Assert.assertEquals(Lists.newArrayList(6, 19), actualUnreachable.getPointerIntegers());
    Assert.assertTrue(Arrays.equals(payload, Arrays.copyOfRange(data, actualUnreachable.offset + actualUnreachable.payloadOffset, actualUnreachable.offset + actualUnreachable.length)));
  }

  @Test
  public void name() throws Exception {
    String s = "Hello, world!";


    for (char c : s.toCharArray())
      System.out.print(c + " ");

    System.out.println();
    for (char c : s.toCharArray())
      System.out.print((byte)c + " ");
  }
}
