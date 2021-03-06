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
package org.sonar.sslr.internal.ast.select;

import org.sonar.sslr.ast.AstSelect;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

import java.util.Iterator;
import java.util.List;

/**
 * {@link AstSelect} which contains exactly one element.
 */
public class SingleAstSelect implements AstSelect {

  private final AstNode node;

  public SingleAstSelect(AstNode node) {
    this.node = node;
  }

  public AstSelect children() {
    if (node.getNumberOfChildren() == 1) {
      return new SingleAstSelect(node.getFirstChild());
    } else if (node.getNumberOfChildren() > 1) {
      return new ListAstSelect(node.getChildren());
    } else {
      return AstSelectFactory.empty();
    }
  }

  public AstSelect children(AstNodeType type) {
    if (node.getNumberOfChildren() == 1) {
      AstNode result = node.getChildren().get(0);
      if (result.getType() == type) {
        return new SingleAstSelect(result);
      }
      return AstSelectFactory.empty();
    } else if (node.getNumberOfChildren() > 1) {
      List<AstNode> result = Lists.newArrayList();
      // Don't use "getChildren(type)", because under the hood it will create an array of types and new List to keep the result
      for (AstNode child : node.getChildren()) {
        // Don't use "is(type)", because under the hood it will create an array of types
        if (child.getType() == type) {
          result.add(child);
        }
      }
      return AstSelectFactory.create(result);
    } else {
      return AstSelectFactory.empty();
    }
  }

  public AstSelect children(AstNodeType... types) {
    if (node.getNumberOfChildren() == 1) {
      AstNode result = node.getChildren().get(0);
      if (result.is(types)) {
        return new SingleAstSelect(result);
      }
      return AstSelectFactory.empty();
    } else if (node.getNumberOfChildren() > 1) {
      List<AstNode> result = Lists.newArrayList();
      // Don't use "getChildren(type)", because it will create new List to keep the result
      for (AstNode child : node.getChildren()) {
        if (child.is(types)) {
          result.add(child);
        }
      }
      return AstSelectFactory.create(result);
    } else {
      return AstSelectFactory.empty();
    }
  }

  public AstSelect nextSibling() {
    return AstSelectFactory.select(node.getNextSibling());
  }

  public AstSelect previousSibling() {
    return AstSelectFactory.select(node.getPreviousSibling());
  }

  public AstSelect parent() {
    return AstSelectFactory.select(node.getParent());
  }

  public AstSelect firstAncestor(AstNodeType type) {
    AstNode result = node.getParent();
    while (result != null && result.getType() != type) {
      result = result.getParent();
    }
    return AstSelectFactory.select(result);
  }

  public AstSelect firstAncestor(AstNodeType... types) {
    AstNode result = node.getParent();
    while (result != null && !result.is(types)) {
      result = result.getParent();
    }
    return AstSelectFactory.select(result);
  }

  public AstSelect descendants(AstNodeType type) {
    return AstSelectFactory.create(node.getDescendants(type));
  }

  public AstSelect descendants(AstNodeType... types) {
    return AstSelectFactory.create(node.getDescendants(types));
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean isNotEmpty() {
    return true;
  }

  public AstSelect filter(AstNodeType type) {
    return node.getType() == type ? this : AstSelectFactory.empty();
  }

  public AstSelect filter(AstNodeType... types) {
    return node.is(types) ? this : AstSelectFactory.empty();
  }

  public AstSelect filter(Predicate<AstNode> predicate) {
    return predicate.apply(node) ? this : AstSelectFactory.empty();
  }

  public int size() {
    return 1;
  }

  public AstNode get(int index) {
    if (index == 0) {
      return node;
    }
    throw new IndexOutOfBoundsException();
  }

  public Iterator<AstNode> iterator() {
    return Iterators.singletonIterator(node);
  }

}
