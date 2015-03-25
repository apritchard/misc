package com.amp.misc.regexer;

import java.util.List;

/**
 * RegexStatement maps directly to input json file and includes
 * information about what regex should be evaluated.
 * @author apritchard
 *
 */
public class RegexStatement {
  private String                descriptiveName;
  private List<RegexExpression> expressions;
  private boolean delete;

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

  public boolean isDelete() {
    return delete;
  }

  public void setDelete(boolean delete) {
    this.delete = delete;
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
