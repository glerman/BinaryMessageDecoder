package com.tectonic.decode;

import com.tectonic.domain.Memory;
import com.tectonic.domain.RawBlock;

public class MessageExtractor {

  private final Memory memory;

  public MessageExtractor(final Memory memory) {
    this.memory = memory;
  }

  public String extract() {
    StringBuilder message = new StringBuilder();
    memory.getUnreachableBlocks().forEach(block -> message.append(extractBlockPayload(block)));
    return message.toString();
  }

  //todo: we copy the payload. must we?
  private char[] extractBlockPayload(final RawBlock block) {
    char[] payload = new char[block.payloadLength];
    for (int i = block.payloadOffset; i< block.payloadLength; i++) {
      payload[i] = (char) memory.data[block.payloadOffset + i];
    }
    return payload;
  }
}
