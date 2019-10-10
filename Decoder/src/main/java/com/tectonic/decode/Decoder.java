package com.tectonic.decode;

import com.tectonic.domain.Memory;

public class Decoder {

  private final Memory memory;

  public Decoder(final Memory memory) {
    this.memory = memory;
  }

  public String decode() {
    //todo collect the payload of unreachable blocks and convert to a string
    throw new UnsupportedOperationException();
  }
}
