SET sourcePath="example-log.txt"
SET trimmedFilePath="example-log-modified.txt"
SET definitionsPath="default.json"
java -jar regexer.jar %sourcePath% %trimmedFilePath% %definitionsPath%