package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.CRC32;

/**
 * Abstract client class. Class contains shared methods between UDP and TCP clients.
 */
public abstract class AbstractClient {

  protected String protocol;
  protected HashMap<String, String> hashMap;
  protected ClientLogger logger;

  /**
   * Constructor. Initializes a new key, value store
   * to auto populate the server key, value store with.
   */
  public AbstractClient() {
    this.logger = new ClientLogger("client.log");
    this.hashMap = new HashMap<String,String>();
    this.populateKeyValueStore();
  }
  /**
   * Method collectInput takes in a message from the user to pass to the server.server.
   * The method ensures the message is at least 1 character and less than 80.
   *
   * @return message string
   */
  public String collectInput() throws IOException {
    // get user input
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Enter message to send to server. Type 'q' to quit:");
    String message = input.readLine();
    // ensure message length
    while (message.length() > 80 | message.trim().isEmpty()) {
      System.out.println("Please keep message between 1 and 80 characters.");
      System.out.println("Enter a new message to send to server.server:");
      message = input.readLine();
    }
    return message;
  }

  /**
   * Method to extract checksum from header and return
   * checksum split from request.
   * @param packet from server
   * @return checksum and request
   */
  public String[] extractChecksum(String packet) {
    //try {
      String[] packetList =  packet.split(":");
      if (packetList.length != 2) {
        this.logger.logMessage("No header available for packet. Checksum not validated");
        System.out.println("No header available for packet. Checksum not validated");
        return new String[]{"","Datagram packet malformed."};
      }
      return packetList;
      /*
    } catch (Exception e) {
      this.logger.logMessage("No header available for packet. Checksum not validated");
      System.out.println("No header available for packet. Checksum not validated");
      return new String[]{"","Datagram packet malformed."};
    }

       */
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
   * Method computes a checksum for the message that will be passed
   * to the server.
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
   * Method to check if received checksum and calculated checksum are equal.
   * @param packetList parsed packet from server
   * @return true if equal, false if not
   */
  public boolean validateMessage(String[] packetList) {
    String calculatedChecksum = this.calculateChecksum(packetList[1]);
    return calculatedChecksum.equals(packetList[0]);
  }


  /**
   * Method to automatically populate client hashmap with dummy data.
   */
  private void populateKeyValueStore() {

    this.hashMap.put("name", "dominic");
    this.hashMap.put("job", "analyst");
    this.hashMap.put("industry", "ecommerce");
    this.hashMap.put("location", "boston");
    this.hashMap.put("degree", "CS");
  }

  /**
   * Method parses the args inputted by the user when establishing the client.client.
   * If the number of args is anything other than 1 or 2, a default port number will be used.
   *
   * @param args inputted by user
   * @return host, port number, in a string array
   */
  public static String[] parseArgs(String[] args) {
    String[] newArgs = new String[] {"localhost", "4999"};

    if (args.length == 3) {
      newArgs[0] = args[0];
      newArgs[1] = args[1];
    } else if (args.length == 2) {
      newArgs[1] = args[0];
    } else {
      System.out.println("Unknown number of args entered. Localhost, Port Number 4999, used.");
    }

    return newArgs;
  }
}
