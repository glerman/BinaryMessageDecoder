package com.tectonic;

import com.tectonic.domain.Memory;
import com.tectonic.input.InputReader;
import com.tectonic.input.MemoryScanner;

import java.io.IOException;

public class Runner {

  public static void main(String[] args) throws IOException {

    String file = Runner.class.getClassLoader().getResource("hello.bin").getFile();
    byte[] data = new InputReader(file).read();
    MemoryScanner memoryScanner = new MemoryScanner(data);
    Memory memory = memoryScanner.scan();

    System.out.println("And the message is:");
    System.out.println(memory.getMessage());
  }
}
