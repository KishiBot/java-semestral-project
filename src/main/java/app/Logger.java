package app;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Logger
 */
public class Logger {
    private String logDir = null;
    private DateTimeFormatter dtf = null;
    private static final String latestLog = "log/latest.log";

    private class Color {
        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
    }

    public enum Severity {
        WARNING,
        EXCEPTION,
        INFO,
    }

    private static ArrayList<Log> logList = new ArrayList<Log>();
    private static ArrayList<String> finishedLogs = new ArrayList<>();

    /**
     * Saves log for writing when ready
     */
    public static void log(Severity _severity, String _classTag, String _message) {
        logList.add(new Log(_severity, _classTag, _message));
    }

    /**
     * Initializes date time and creates new log file
     */
    public void init() {
        // Get current time for log file name
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
        logDir = new String("log/" + dtf.format(LocalDateTime.now()) + ".log");

        // Create log directory if it doesn't exist
        try {
            if (!Files.exists(Paths.get("log/"))) {
                Files.createDirectories(Paths.get("log/"));
            }
        } catch (Exception e) {
            System.out.println("Could not create log directory");
        }

    }

    public void run() {

        // Main thread loop
        try {
            // If there is a log in logList, write it into log file
            while (!logList.isEmpty()) {
                Log log = logList.remove(0);
                FileWriter wr = new FileWriter(logDir, true);
                PrintWriter pr = new PrintWriter(wr);

                String col = null;
                switch(log.severity) {
                    case INFO:
                        col = Color.GREEN;
                        break;
                    case WARNING:
                        col = Color.YELLOW;
                        break;
                    case EXCEPTION:
                        col = Color.RED;
                        break;
                }

                String message = String.format("[%s%s%s] %s: %s\n", 
                        col,
                        log.severity,
                        Color.RESET,
                        log.classTag,
                        log.message);

                if (finishedLogs.contains(message)) continue;

                finishedLogs.add(message);
                pr.printf("[%s] %s",
                    dtf.format(LocalDateTime.now()),
                    message);

                pr.close();
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Could not write log: " + e);
            quit();
            return;
        }

    }

    /**
     * Creates latest.log file
     */
    public void quit() {
        run();

        // Create latest.log for ease of debugging
        try {
            Files.copy(Paths.get(logDir), Paths.get(latestLog), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(Color.RED + "Could not replace latest log file" + Color.RESET);
        }
    }
}
