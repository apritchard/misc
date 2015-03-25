package com.amp.misc.regexer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import com.amp.misc.regexer.RegexStatement.RegexExpression;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Regexer {
  private static final Logger  logger   = LoggerFactory.getLogger(Regexer.class);
  private static final Charset ENCODING = StandardCharsets.UTF_8;                // assume UTF_8 for now

  public static void main(String[] args) {
    if (args.length != 3) {
      printUsage();
      return;
    }
    String log = args[0];
    String newLog = args[1];
    String statements = args[2];
    execute(log, statements, newLog);

  }

  private static void test() {
    String logPath = "C:/temp/qa-automated-tests/example-log.txt";
    String newLogPath = "C:/temp/qa-automated-tests/example-log-trimmed.txt";
    String statementsPath = "C:/temp/qa-automated-tests/default.txt";
    execute(logPath, statementsPath, newLogPath);
  }

  private static void execute(String log, String statements, String newLog) {
    Path logPath = Paths.get(log);
    Path statementsPath = Paths.get(statements);
    Path newLogPath = Paths.get(newLog);
    if (!Files.exists(logPath)) {
      printInvalidFile(log);
      return;
    }
    if (!Files.exists(statementsPath)) {
      printInvalidFile(statements);
      return;
    }

    List<RegexResult> results = regexFile(logPath, statementsPath, newLogPath);
    StringBuilder sb = new StringBuilder();
    for (RegexResult result : results) {
      sb.append(result.toString()).append(System.getProperty("line.separator"));
    }
    logger.info("\n" + sb.toString());
  }

  private static void printUsage() {
    System.out.println("Regexer requires 3 arguments:"
        + "\n1.\tPath to the file you wish to process"
        + "\n2.\tPath and file name where the new trimmed file will be created"
        + "\n3.\tPath to the regex description file"
        + "\n"
        + "\nThe regex description file should be in the following format:");
  }

  private static void printInvalidFile(String path) {
    logger.error("Invalid file specified: " + path);
  }

  public static List<RegexResult> regexFile(Path logPath, Path statementsPath, Path newLogPath) {
    List<RegexStatement> statements = parseJsonStatements(statementsPath);
    List<RegexResult> results = trimLog(logPath, newLogPath, statements);
    return results;
  }

  /**
   * Reads the json file at the specified location and parses it into RegexStatements.
   * @param statementsPath
   * @return
   */
  private static List<RegexStatement> parseJsonStatements(Path statementsPath) {
    Gson gson = new Gson();
    String definitionString = readFileText(statementsPath);
    Type type = new TypeToken<List<RegexStatement>>() {
    }.getType();
    return gson.fromJson(definitionString, type);
  }

  /**
   * Creates a new, trimmed log file that has all the regex matches removed
   * 
   * @param logPath
   *          Path to the raw logfile
   * @param newLogPath
   *          Path to write the edited log
   * @param statements
   *          List of regex statements to process
   * @return One RegexResult for each statement
   */
  private static List<RegexResult> trimLog(Path logPath, Path newLogPath, List<RegexStatement> statements) {
    String logText = readFileText(logPath);
    List<RegexResult> results = new ArrayList<>();
    for (RegexStatement statement : statements) {
      if(statement == null) {
        break; //this happens when GSON deserializes if there is an extra comma at the end of a list
      }
      RegexResult result = new RegexResult(statement);

      for (RegexExpression expression : statement.getExpressions()) {
        Pattern p = Pattern.compile(expression.getRegex(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(logText);

        // for each match
        while (m.find()) {
          result.increment(expression); // increment our count of this match
          int i = 1;
          if(expression.getGroupNames() == null){
            continue;
          }
          // then save all the matched groups (if any)
          for (String refName : expression.getGroupNames()) {
            result.addRef(expression, refName, m.group(i++));
          }
        }
        logText = m.replaceAll(""); // then replace all matches with empty string
      }
      results.add(result);
    }
    writeFile(logText, newLogPath);
    return results;
  }

  private static void writeFile(String logText, Path newLogPath) {
    try {
      Files.createDirectories(newLogPath.getParent());
      Files.write(newLogPath, logText.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (IOException e) {
      logger.error("Unable to write file {}", newLogPath.toString(), e);
    }
  }

  /**
   * @param filePath
   *          Path to the file
   * @return String containing entire contents of file
   */
  private static String readFileText(Path filePath) {
    byte[] fileData = null;
    try {
      fileData = Files.readAllBytes(filePath);
    }
    catch (IOException e) {
      logger.error("Unable to read file {}", filePath.toString(), e);
      throw new RuntimeException(e);
    }
    String text = new String(fileData, ENCODING);
    return text;
  }

  /**
   * @param filePath
   *          Path to the file
   * @return List of Strings, one per line in the file
   */
  private static List<String> readFileLines(Path filePath) {
    try {
      return Files.readAllLines(filePath, ENCODING);
    }
    catch (IOException e) {
      logger.error("Unable to read file {}", filePath.toString(), e);
      throw new RuntimeException(e);
    }
  }

}
