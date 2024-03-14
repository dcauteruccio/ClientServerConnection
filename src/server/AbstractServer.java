package server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.zip.CRC32;

/**
 * Abstract server class. Class contains shared methods between UDP and TCP servers.
 */
public abstract class AbstractServer {

  protected HashMap<String, String> hashMap;
  protected ServerLogger logger; // logger for Server
  protected int port; // port opened for connection
  protected InetAddress clientAddress; // client address of received connection/message


  /**
   * Abstract Constructor.
   */
  public AbstractServer() {
    this.hashMap = new HashMap<String,String>();
    this.logger = new ServerLogger("server.log");
  }

  /**
   * Take in message from client
   * @param parsedMessage with commands and datagram
   * @return
   */
  public String keyValueRequest(String[] parsedMessage) {
    // one final check to ensure a valid request
    if (!parsedMessage[0].toUpperCase().equals("PUT") &&
            !parsedMessage[0].toUpperCase().equals("GET") &&
            !parsedMessage[0].toUpperCase().equals("DELETE")) {
      this.logger.logMessage("Received an invalid request, " + parsedMessage[0] +
              ", from Inet Address " + this.clientAddress);
      return "Received an invalid request, " + parsedMessage[0] +
              ", from Inet Address " + this.clientAddress;
    }
    // perform the given request
    if (parsedMessage[0].toUpperCase().equals("PUT")) {
      return this.put(parsedMessage[1], parsedMessage[2]);
    } else if (parsedMessage[0].toUpperCase().equals("GET")) {
      return this.get(parsedMessage[1]);
    } else if (parsedMessage[0].toUpperCase().equals("DELETE")) {
      return this.delete(parsedMessage[1]);
    }
    // to do
    this.logger.logMessage("Unable to perform " + parsedMessage[0] + "request.");
    return "Unable to perform " + parsedMessage[0] + "request.";
  }

  /**
   * Method puts key, value pair into store if not already exists.
   * If key already exists, it updates the value stored.
   * @param key to store
   * @param value to store
   * @return message of whether successful or not
   */
  public String put(String key, String value) {
    String key_lc = key.toLowerCase();
    this.logStandardRequestMethod("PUT", key_lc); // log request
    if (!this.hashMap.containsKey(key_lc)) { // if it doesn't exist already in store
      this.hashMap.put(key_lc, value);
      this.logger.logMessage("Response: New value for key, " + key_lc + ", added -> " + value);
      return ("New value for key, " + key_lc + ", added -> " + value);
    } else { // if it does exist
      String old = this.hashMap.get(key_lc);
      this.hashMap.replace(key_lc, value);
      this.logger.logMessage("Response: Old Value, " + old + ", for key, " + key_lc +
              ", replaced with new value, " + value + ".");
      return ("Old Value, " + old + ", for key, " + key_lc +
              ", replaced with new value, " + value + ".");
    }
  }

  /**
   * Return value for the given key, if exists.
   * @param key to get value for
   * @return value for key, or error message if key doesn't exist.
   */
  public String get(String key) {
    String key_lc = key.toLowerCase();
    this.logStandardRequestMethod("GET", key_lc);
    if (this.hashMap.containsKey(key_lc)) { // if it does exist
      String value = this.hashMap.get(key_lc);
      this.logger.logMessage("Response: " + value + " returned for key " + key_lc + ".");
      return value;
    } else { // if it doesn't exist in store
      this.logger.logMessage("Response: No key, " + key_lc + ", found in data store.");
      return "No key found in data store.";
    }
  }

  /**
   * Method to remove key from store, if exists.
   * If the key doesn't exist, a message stating that is returned.
   * @param key
   * @return message that key was deleted or didn't exist.
   */
  public String delete(String key) {
    String key_lc = key.toLowerCase();
    this.logStandardRequestMethod("DELETE", key_lc);
    if (this.hashMap.containsKey(key_lc)) { // if it does exist
      this.hashMap.remove(key_lc);
      this.logger.logMessage("Response: Key, " + key_lc + ", successfully removed from store.");
      return "Key successfully removed from store.";
    } else { // if it doesn't exist in store
      this.logger.logMessage("Response: Key, " + key_lc + ", did not exist in store.");
      return "Key did not exist in store.";
    }
  }

  /**
   * Method to process message from client and check
   * if it is appropriately formatted.
   * @param message from client
   * @return parsed message if valid
   */
  public String[] processMessage(String message) {
    if (!message.contains(",")) { // if no commas, then it's malformed
      String output = this.logStandardErrorMessage(message);
      return new String[]{"Datagram packet malformed.", output};
    }
    String[] split = message.split(",");
    if (split.length == 3) { // if there are 3 parts to request
      if (!split[0].toLowerCase().equals("put")) { // must be a put
        String output = this.logStandardErrorMessage(message);
        return new String[]{"Datagram packet malformed.", output};
      }
      split[0] = split[0].trim().toUpperCase();
      split[1] = split[1].trim().toLowerCase();
      split[2] = split[2].trim().toLowerCase();
      return split;
    }
    if (split.length == 2) { // there are 2 parts, must be get or delete
      if (!split[0].toLowerCase().equals("get") && !split[0].toLowerCase().equals("delete")) {
        String output = this.logStandardErrorMessage(message);
        return new String[]{"Datagram packet malformed.", output};
      }
      split[0] = split[0].trim().toUpperCase();
      split[1] = split[1].trim().toLowerCase();
      return split;
    }
    // all others are an error
    String output = this.logStandardErrorMessage(message);
    return new String[]{"Datagram packet malformed.", output};
  }

  public String processRequest(String[] parsedPacket) {
    String result = "";
    String[] processedMessage = this.processMessage(parsedPacket[1]);
    // check if the message is valid - this.validateMessage(parsedPacket)
    if (!processedMessage[0].equals("Datagram packet malformed.")) {
      //processedMessage = this.processMessage(parsedPacket[1]);
      result = this.keyValueRequest(processedMessage);
      System.out.println("Result of request: " + result);
    } else {
      System.out.println("Received malformed request of length " + parsedPacket[1].length() +
              "from address " + this.clientAddress + ", port " + this.port);
      this.logger.logMessage("Received malformed request of length " + parsedPacket[1].length() +
              "from address " + this.clientAddress + ", port " + this.port);
      result = "Datagram packet malformed.";
    }
    return result;
  }

  /**
   * Method to extract checksum from header and return
   * checksum split from request.
   * @param packet from client
   * @return checksum and request
   */
  public String[] extractChecksum(String packet) {
    try {
      String[] packetList =  packet.split(":");
      if (packetList.length != 2) {
        this.logger.logMessage("No header available for packet. Checksum not validated");
        System.out.println("No header available for packet. Checksum not validated");
        return new String[]{"","Datagram packet malformed."};
      }
      return packetList;

    } catch (Exception e) {
      this.logger.logMessage("No header available for packet. Checksum not validated");
      System.out.println("No header available for packet. Checksum not validated");
      return new String[]{"","Datagram packet malformed."};
    }
  }

  /**
   * Method calculates checksum of received message.
   * @param message to calculate checksum of
   * @return checksum
   */
  public String calculateChecksum(String message) {
    CRC32 crc = new CRC32();

    crc.update(message.getBytes());
    String checksum = Long.toString(crc.getValue());
    return checksum;
  }

  /**
   * Method to check if received checksum and calculated checksum are equal.
   * @param packetList parsed packet from client
   * @return true if equal, false if not
   */
  public boolean validateMessage(String[] packetList) {
    String calculatedChecksum = this.calculateChecksum(packetList[1]);
    return calculatedChecksum.equals(packetList[0]);
  }

  /**
   * Method computes a checksum for the message that will be passed
   * to the client.
   * @param input received from user
   * @return checksum value prepended to message
   * @throws IOException
   */
  public String getChecksum(String input) {
    String checksum = this.calculateChecksum(input);
    String packet = checksum + ":" + input;
    return packet;
  }

  /**
   * Helper method to log standard error message.
   * @param message received from client
   */
  private String logStandardErrorMessage(String message) {
    String note = "Received malformed request of length " + message.length() +
            " from address " + this.clientAddress + ", port " + this.port;
    this.logger.logMessage(note);
    return note;
  }

  /**
   * Helper method to log standard valid request method.
   * @param method requested
   * @param key requested
   */
  private void logStandardRequestMethod(String method, String key) {
    this.logger.logMessage("Received " + method + " request from Inet Address, " +
            this.clientAddress + ", on port, " + this.port + ", for key " + key + ".");
  }


  /* **************************************
   ********* Static Helper Method *********
   ***************************************/

  /**
   * Method parses the args inputted by the user when establishing the server.server.
   * If the number of args is anything other than 1, a default port number will be used.
   *
   * @param args inputted by user
   * @return port number
   */
  public static int parseArgs(String[] args) {
    int port = 4999;

    if (args.length == 1) {
      port = Integer.parseInt(args[0]);
    } else {
      System.out.println("Invalid number of args inputted. Default Port Number 4999 used.");
    }

    return port;
  }


}
