/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.sslr.internal.text;

import org.sonar.sslr.text.Text;
import org.sonar.sslr.text.TextCharSequence;
import org.sonar.sslr.text.TextLocation;

public class PlainText extends AbstractText implements TextCharSequence {

  private final char[] chars;

  public PlainText(char[] chars) {
    this.chars = chars;
  }

  public Text subText(int start, int end) {
    return new SubText(this, start, end);
  }

  @Override
  protected int getTransformationDepth() {
    return 0;
  }

  public Text getText() {
    return this;
  }

  public int length() {
    return chars.length;
  }

  @Override
  public void toCharArray(int srcPos, char[] dest, int destPos, int length) {
    System.arraycopy(chars, srcPos, dest, destPos, length);
  }

  public TextCharSequence sequence() {
    return this;
  }

  public char charAt(int index) {
    return chars[index];
  }

  public TextCharSequence subSequence(int from, int to) {
    return subText(from, to).sequence();
  }

  public TextLocation getLocation(int index) {
    return null;
  }

}
