package com.amp.misc.regexer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Regexer {
  private static final Logger logger = LoggerFactory.getLogger(Regexer.class);
  private static final Charset ENCODING = StandardCharsets.UTF_8; //assume UTF_8 for now
  
  public static void main(String[] args) {
//    test();
    String logPath = "C:/temp/qa-automated-tests/example-log.txt";
    String newLogPath = "C:/temp/qa-automated-tests/example-log-trimmed.txt";
    String statementsPath = "C:/temp/qa-automated-tests/default.txt";
    List<RegexResult> results = regexFile(logPath, statementsPath, newLogPath);
    StringBuilder sb = new StringBuilder();
    for(RegexResult result : results){
      sb.append(result.toString()).append(System.getProperty("line.separator"));
    }
    logger.info("\n" + sb.toString());
  }
  
  private static void test(){
//    String str = "2015-03-20 15:28:08,054 [http-8832-54] INFO  util.HibernateUtil - ^^^^Closing Hibernate Session of this thread.^^^^" +
//              "\r\n" + "2015-03-20 15:28:08,078 [http-8832-54] INFO  util.HibernateUtil - ^^^^Opening new Hibernate Session for this thread.^^^^";
//    String regex = "^.*] INFO.*Hibernate Session.*\\r?\\n";
//    
//    Pattern p = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
//    Matcher m = p.matcher(str);
//    int count = 0;
//    while(m.find()){
//      count++;
//    }
//    
//    logger.info("Matches: " + count);
    
  }

  public static List<RegexResult> regexFile(String logPath, String statementsPath, String newLogPath) {
    List<RegexStatement> statements = parseStatements(statementsPath);
    List<RegexResult> results = trimLog(logPath, newLogPath, statements);
    return results;
  }
  
  /**
   * Reads file at specified location and parses into RegexStatement. Expects
   * format following format for each record:
   * descriptiveName
   * regex
   * comma-delimited reference names for any groups in the regex
   * blank line
   * 
   * @param statementsPath
   * @return
   */
  private static List<RegexStatement> parseStatements(String statementsPath) {
    //could replace this with JSON or some kind of less ambiguous marked up file,
    //but then we'd also have to escape all the regexes.. lazy approach for now
    
    List<RegexStatement> statements = new ArrayList<>();
    Queue<String> l = new LinkedList<>(readFileLines(statementsPath));
    
    while(!l.isEmpty()){
      String descriptiveName = l.poll();
      String regex = l.poll();
      String refs = l.poll();
      List<String> refList = new ArrayList<>();
      if(refs != null && !refs.trim().isEmpty()){
        refList.addAll(Arrays.asList(refs.split(",")));
      }
      statements.add(new RegexStatement(descriptiveName, regex, refList));
      //spin through blank lines
      while(l.peek() != null && l.peek().trim().isEmpty()){
        l.poll();
      }
    }
    return statements;
  }

  /**
   * Creates a new, trimmed log file that has all the regex matches removed
   * 
   * @param logPath Path to the raw logfile
   * @param newLogPath Path to write the edited log
   * @param statements List of regex statements to process
   * @return One RegexResult for each statement
   */
  private static List<RegexResult> trimLog(String logPath, String newLogPath, List<RegexStatement> statements) {
    String logText = readFileText(logPath);
    List<RegexResult> results = new ArrayList<>();
    for(RegexStatement statement : statements){
      RegexResult result = new RegexResult(statement);
      
      Pattern p = Pattern.compile(statement.getRegex(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(logText);
      
      //for each match
      while(m.find()){
        result.increment(); //increment our count of this match
        int i = 1;
        //then save all the matched groups (if any)
        for(String refName : statement.getRefNames()){
          result.addRef(refName, m.group(i++));
        }
      }
      logText = m.replaceAll(""); //then replace all matches with empty string
      results.add(result);
    }
    writeFile(logText, newLogPath);
    return results;
  }

  private static void writeFile(String logText, String newLogPath) {
    try {
      Files.write(Paths.get(newLogPath), logText.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (IOException e) {
      logger.error("Unable to write file {}", newLogPath, e);
    }
  }

  /**
   * @param filePath Path to the file
   * @return String containing entire contents of file
   */
  private static String readFileText(String filePath) {
    byte[] fileData = null;
    try {
      fileData = Files.readAllBytes(Paths.get(filePath));
    }
    catch (IOException e) {
      logger.error("Unable to read file {}", filePath, e);
      throw new RuntimeException(e);
    }
    String text = new String(fileData, ENCODING);
    return text;
  }
  
  /**
   * @param filePath Path to the file
   * @return List of Strings, one per line in the file
   */
  private static List<String> readFileLines(String filePath){
    try {
      return Files.readAllLines(Paths.get(filePath), ENCODING);
    }
    catch (IOException e) {
      logger.error("Unable to read file {}", filePath, e);
      throw new RuntimeException(e);
    }
  }

}
