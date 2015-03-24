package com.amp.misc.regexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexResult {
  private static final Logger logger = LoggerFactory.getLogger(RegexResult.class);
  
  private RegexStatement      regexStatement;
  private int                 occurrences;
  private Map<String, Map<String, Integer>> refValues;

  public RegexResult(RegexStatement regexStatement) {
    this.regexStatement = regexStatement;
    refValues = new HashMap<>();
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(regexStatement.getDescriptiveName()).append(System.getProperty("line.separator"));
    sb.append("Occurrences: ").append(occurrences).append(System.getProperty("line.separator"));
    for(Entry<String, Map<String, Integer>> entry : refValues.entrySet()){
      sb.append(entry);
      sb.append(System.getProperty("line.separator"));
    }
    return sb.toString();
  }
  
  public void addRef(String refName, String refValue){
    if(!refValues.containsKey(refName)){
      refValues.put(refName, new HashMap<String, Integer>());
    }
    
    if(!refValues.get(refName).containsKey(refValue)){
      refValues.get(refName).put(refValue, 0);
    }
    
    int newInt = refValues.get(refName).get(refValue) + 1;
    refValues.get(refName).put(refValue, newInt);
  }
  
  public void increment(){
    occurrences++;
  }

  public RegexStatement getRegexStatement() {
    return regexStatement;
  }

  public int getOccurrences() {
    return occurrences;
  }

  public Map<String, Map<String, Integer>> getRefValues() {
    return refValues;
  }

}
