package com.amp.misc.regexer;

import java.util.HashMap;
import java.util.Map;

import com.amp.misc.regexer.RegexStatement.RegexExpression;

public class RegexResult {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private RegexStatement regexStatement;
  private Map<RegexExpression, ExpressionResult> refCounts = new HashMap<>();

  public RegexResult(RegexStatement regexStatement) {
    this.regexStatement = regexStatement;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(regexStatement.getDescriptiveName()).append(LINE_SEPARATOR);
    for (RegexExpression expression : regexStatement.getExpressions()) {
      String name = expression.getName();
      if (name == null || name.isEmpty()) {
        name = expression.getGroupNames() == null  ? "Match(es)" : expression.getGroupNames().toString();
      }
      if (!refCounts.containsKey(expression)) {
        sb.append(name).append(" not found");
        sb.append(LINE_SEPARATOR);
      }
      else {
        ExpressionResult result = refCounts.get(expression);

        sb.append(result.getOccurrences()).append(" ").append(name).append(LINE_SEPARATOR);
        if (expression.getGroupNames() == null) {
          continue;
        }
        for (String groupName : expression.getGroupNames()) {
          sb.append("\t").append(groupName).append(": ").append(result.getRefValues().get(groupName));
          sb.append(LINE_SEPARATOR);
        }
      }
    }
    return sb.toString();
  }

  public void increment(RegexExpression expression) {
    if (!refCounts.containsKey(expression)) {
      refCounts.put(expression, new ExpressionResult());
    }
    refCounts.get(expression).increment();
  }

  public void addRef(RegexExpression expression, String refName, String refValue) {
    if (!refCounts.containsKey(expression)) {
      refCounts.put(expression, new ExpressionResult());
    }
    refCounts.get(expression).addRef(refName, refValue);
  }

  private class ExpressionResult {
    private int occurrences;
    private Map<String, Map<String, Integer>> refValues = new HashMap<>();

    public void increment() {
      occurrences++;
    }

    public void addRef(String refName, String refValue) {
      if (!refValues.containsKey(refName)) {
        refValues.put(refName, new HashMap<String, Integer>());
      }

      if (!refValues.get(refName).containsKey(refValue)) {
        refValues.get(refName).put(refValue, 0);
      }

      int newInt = refValues.get(refName).get(refValue) + 1;
      refValues.get(refName).put(refValue, newInt);
    }

    public int getOccurrences() {
      return occurrences;
    }

    public Map<String, Map<String, Integer>> getRefValues() {
      return refValues;
    }
  }

}
