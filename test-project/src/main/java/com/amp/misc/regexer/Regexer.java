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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
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
    regexFile(log, statements, newLog);

  }

  public static void regexFile(String log, String definitions, String newLog) {
    Path logPath = Paths.get(log);
    Path statementsPath = Paths.get(definitions);
    Path newLogPath = Paths.get(newLog);
    if (!Files.exists(logPath)) {
      printInvalidFile(log);
      return;
    }
    if (!Files.exists(statementsPath)) {
      printInvalidFile(definitions);
      return;
    }

    List<RegexStatement> statements = parseJsonStatements(statementsPath);
    List<RegexResult> results = trimLog(logPath, newLogPath, statements);
    StringBuilder sb = new StringBuilder();
    for (RegexResult result : results) {
      sb.append(result.toString()).append(System.getProperty("line.separator"));
    }
    logger.info("\n" + sb.toString());
  }

  private static void printUsage() {
    String sampleJson = readResourceText("/sample.json");
    
    System.out.println("Regexer requires 3 arguments:"
        + "\n1.\tPath to the file you wish to process"
        + "\n2.\tPath and file name where the new trimmed file will be created"
        + "\n3.\tPath to the regex description file"
        + "\n"
        + "\nThe regex description file should be a json file in the following format:"
        + "\n" + sampleJson
        + "\n\nMultiple expressions can be provided for each descriptiveName and"
        + "\nmultiple groups may be named for each expression. Expression name and"
        + "\ngroupNames are optional. See default.json within this project for a "
        + "\nmore complete example.");
  }

  private static void printInvalidFile(String path) {
    logger.error("Invalid file specified: " + path);
  }

  /**
   * Reads the json file at the specified location and parses it into RegexStatements.
   * 
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
      if (statement == null) {
        break; // this happens when GSON deserializes if there is an extra comma at the end of a list
      }
      RegexResult result = new RegexResult(statement);

      for (RegexExpression expression : statement.getExpressions()) {
        Pattern p = Pattern.compile(expression.getRegex(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(logText);

        // for each match
        while (m.find()) {
          result.increment(expression); // increment our count of this match
          int i = 1;
          if (expression.getGroupNames() == null) {
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
      Path parent = newLogPath.getParent();
      if (parent != null) {
        Files.createDirectories(newLogPath.getParent());
      }
      Files.write(newLogPath, logText.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    catch (IOException e) {
      logger.error("Unable to write file {}", newLogPath.toString(), e);
    }
  }
  
  private static String readResourceText(String resourcePath){
    try {
      return IOUtils.toString(Regexer.class.getResourceAsStream(resourcePath));
    }
    catch (IOException e) {
      logger.error("Unable to read resource file {}", resourcePath, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * @param filePath Path to the file
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
   * @param filePath Path to the file
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
