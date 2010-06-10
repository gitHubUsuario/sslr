/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.impl.channel;

import static com.sonar.sslr.test.lexer.LexerMatchers.consume;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonar.channel.CodeReader;

import com.sonar.sslr.api.LexerOutput;

public class MultilineCommentChannelTest {

  private LexerOutput output = new LexerOutput();
  private MultilineCommentChannel channel;

  @Test
  public void testConsumCommentStartingWithOneCharacter() {
    channel = new MultilineCommentChannel("/*", "*/");
    assertThat(channel, consume(new CodeReader("/*/ my comment \n second line*/   word"), output));
    assertThat(output, hasComment("/*/ my comment \n second line*/"));
  }

  @Test
  public void testNotConsumWord() {
    channel = new MultilineCommentChannel("/*", "*/");
    assertThat(channel, not(consume(new CodeReader("word"), output)));
  }
}