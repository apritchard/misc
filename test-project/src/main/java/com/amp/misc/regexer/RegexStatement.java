package com.amp.misc.regexer;

import java.util.List;

public class RegexStatement {
  private String       descriptiveName;
  private String       regex;
  private List<String> refNames;

  public RegexStatement(String descriptiveName, String regex, List<String> refNames) {
    this.descriptiveName = descriptiveName;
    this.regex = regex;
    this.refNames = refNames;
  }
  
  public String getDescriptiveName() {
    return descriptiveName;
  }

  public String getRegex() {
    return regex;
  }

  public List<String> getRefNames() {
    return refNames;
  }
}
