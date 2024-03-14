package client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to log activity for the Server.
 * The class will automatically timestamp all messages in the log.
 */
public class ClientLogger {
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
  private String logFileName = "";

  // log any errors
  public ClientLogger(String logFileName) {
    this.logFileName = logFileName;
  }

  /**
   * Method to log messages to the server log.
   * @param message to log
   */
  public void logMessage(String message) {
    String timestamp = this.sdf.format(new Date(System.currentTimeMillis()));
    String logMessage = timestamp + ": " + message;

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
      writer.write(logMessage);
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      System.err.println("Error writing to log file: " + e.getMessage());
    }
  }
}
