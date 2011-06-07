/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.dsl.internal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.dsl.DslException;
import com.sonar.sslr.dsl.bytecode.Bytecode;
import com.sonar.sslr.dsl.bytecode.ControlFlowInstruction;
import com.sonar.sslr.dsl.bytecode.ProcedureDefinition;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.matcher.RuleDefinition;

public class Compiler {

  private Parser<Grammar> parser;
  private String source;
  private File sourceFile;

  private MutablePicoContainer pico = new DefaultPicoContainer();
  private Map<AstNode, Object> adapterByAstNode = new HashMap<AstNode, Object>();

  private Compiler() {
  }

  public static Compiler create(Parser<Grammar> parser, String source) {
    Compiler compiler = new Compiler();
    compiler.parser = parser;
    compiler.source = source;
    return compiler;
  }

  public static Compiler create(Parser<Grammar> parser, File sourceFile) {
    Compiler compiler = new Compiler();
    compiler.parser = parser;
    compiler.sourceFile = sourceFile;
    return compiler;
  }

  public Bytecode compile() {
    AstNode ast = null;
    if (source != null) {
      ast = parser.parse(source);
    } else {
      ast = parser.parse(sourceFile);
    }
    parser = null; // to garbage the tree of matchers
    instantiateAndInjectAdapters(ast);
    Bytecode bytecode = new Bytecode();
    feedBytecode(ast, bytecode);
    return bytecode;
  }

  private void feedBytecode(AstNode astNode, Bytecode bytecode) {
    Object adapter = adapterByAstNode.get(astNode);
    bytecode.startControlFlowInstruction(adapter);
    if (astNode.hasChildren()) {
      for (AstNode child : astNode.getChildren()) {
        feedBytecode(child, bytecode);
      }
    }
    bytecode.endControlFlowInstruction(adapter);
    bytecode.addInstruction(adapter);
  }

  private void instantiateAndInjectAdapters(AstNode astNode) {
    instanciateAdapter(astNode);
    if (astNode.getChildren() != null) {
      for (AstNode child : astNode.getChildren()) {
        instantiateAndInjectAdapters(child);
      }
    }

    Object adapterInstance = adapterByAstNode.get(astNode);
    if (adapterInstance != null) {
      injectAdapterIntoNearestParentWithAdapter(astNode, adapterInstance);
    }
  }

  private void injectAdapterIntoNearestParentWithAdapter(AstNode astNode, Object adapterInstance) {
    if (astNode.getParent() != null) {
      if (adapterByAstNode.containsKey(astNode.getParent())) {
        String ruleName = astNode.getName();
        Object parentAdapterInstance = adapterByAstNode.get(astNode.getParent());
        String[] methodNames = { "add" + Character.toUpperCase(ruleName.charAt(0)) + ruleName.substring(1),
            "set" + Character.toUpperCase(ruleName.charAt(0)) + ruleName.substring(1), "add", "set" };
        if (callMethod(parentAdapterInstance, adapterInstance, methodNames)) {
          return;
        }

        if ( !(parentAdapterInstance instanceof ControlFlowInstruction) && !(parentAdapterInstance instanceof ProcedureDefinition)) {
          throw new DslException("Unable to inject '" + adapterInstance.getClass().getName() + " into "
              + parentAdapterInstance.getClass().getName());
        }
      }
      injectAdapterIntoNearestParentWithAdapter(astNode.getParent(), adapterInstance);
    }
  }

  private boolean callMethod(Object object, Object parameter, String... methodNames) {
    for (Method method : object.getClass().getMethods()) {
      for (String methodName : methodNames) {
        if (method.getName().equals(methodName)) {
          try {
            method.invoke(object, parameter);
            return true;
          } catch (Exception e) {
            if ( !(object instanceof ControlFlowInstruction) && !(object instanceof ProcedureDefinition)) {
              throw new DslException("Unable to call method '" + object.getClass().getName() + "." + methodName + "("
                  + parameter.getClass().getName() + ")", e);
            }
          }
        }
      }
    }
    return false;
  }

  private void instanciateAdapter(AstNode astNode) {
    if (astNode.getType() instanceof RuleDefinition) {
      RuleDefinition rule = (RuleDefinition) astNode.getType();
      if (rule.getAdapter() != null) {
        Object adapterInstance;
        if ( !(rule.getAdapter() instanceof Class)) {
          adapterInstance = rule.getAdapter();
        } else if (astNodeHasOnlyOneLeafChild(astNode) && isAdapterWithStringConstructor((Class) rule.getAdapter())) {
          adapterInstance = newInstance((Class) rule.getAdapter(), astNode.getTokenValue());
        } else {
          if (pico.getComponent(rule.getAdapter()) == null) {
            pico.addComponent(rule.getAdapter());
          }
          adapterInstance = pico.getComponent(rule.getAdapter());
        }
        adapterByAstNode.put(astNode, adapterInstance);
        return;
      }
    }
    return;
  }

  private Object newInstance(Class adapterClass, String tokenValue) {
    try {
      Constructor stringConstructor = adapterClass.getConstructor(String.class);
      return stringConstructor.newInstance(tokenValue);
    } catch (Exception e) {
      throw new DslException("Unable to instantiate adapter '" + adapterClass.getName() + "' with String parameter", e);
    }
  }

  private boolean astNodeHasOnlyOneLeafChild(AstNode astNode) {
    return astNode.getChildren().size() == 1 && !astNode.getChild(0).hasChildren();
  }

  private boolean isAdapterWithStringConstructor(Class adapterClass) {
    try {
      adapterClass.getConstructor(String.class);
    } catch (NoSuchMethodException e) {
      return false;
    }
    return true;
  }

  public void inject(Object component) {
    pico.addComponent(component);
  }
}
