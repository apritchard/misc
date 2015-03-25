package com.amp.misc.regexer;

import java.util.List;

public class RegexStatement {
  private String                descriptiveName;
  private List<RegexExpression> expressions;

  public String getDescriptiveName() {
    return descriptiveName;
  }

  public void setDescriptiveName(String descriptiveName) {
    this.descriptiveName = descriptiveName;
  }

  public List<RegexExpression> getExpressions() {
    return expressions;
  }

  public void setExpressions(List<RegexExpression> expressions) {
    this.expressions = expressions;
  }

  class RegexExpression {
    private String       name;
    private String       regex;
    private List<String> groupNames;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getRegex() {
      return regex;
    }

    public void setRegex(String regex) {
      this.regex = regex;
    }

    public List<String> getGroupNames() {
      return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
      this.groupNames = groupNames;
    }
  }
}
