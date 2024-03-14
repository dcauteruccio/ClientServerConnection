package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Class to represent a UDP Client for sending messages.
 */
public class ClientUDP extends AbstractClient {

  private DatagramSocket clientSocket;
  private InetAddress address; // server address
  private int port; // server port

  /**
   * Constructor. Initializes a new Datagram socket upon being called.
   * @throws SocketException
   */
  public ClientUDP() throws SocketException {
    super();
    this.initializeSocket();
  }

  /**
   * Method to initialize a Datagram socket for the UDP Client
   * to send and receive messages from.
   * @throws SocketException
   */
  public void initializeSocket() throws SocketException {
    this.clientSocket = new DatagramSocket();
    this.clientSocket.setSoTimeout(15000); // set timeout for 15 seconds
  }


  /**
   * Method to get an input message from the user to send to server.
   * Message should be in the form: request type, key, value.
   * Request type should be either: put, get, delete.
   * If message does not follow that format, an error message will be returned from the server.
   * @param message to be sent
   * @return message to be sent
   * @throws IOException
   */
  public String sendPacket(String message) throws IOException {
    String packet = this.getChecksum(message); // add checksum as header

    byte[] sendData = packet.getBytes();
    // send the packet to the given server address and port
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
            this.address, this.port);
    this.clientSocket.send(sendPacket);
    String log = "Request sent to server: " + message;
    this.logger.logMessage(log);
    return message;
  }

  /**
   * Method to receive responses from the server.
   * @param receivedMessage from server
   * @param length length of message
   * @return the decoded string of the message
   * @throws IOException
   */
  public String receiveData(byte[] receivedMessage, int length)  throws IOException {
    DatagramPacket receivePacket = new DatagramPacket(receivedMessage, length);
    try {
      this.clientSocket.receive(receivePacket);
    } catch (java.net.SocketTimeoutException e) { // timeout if no response received
    String error = "Server unresponsive, timeout mechanism executed";
    System.out.println(error);
    this.logger.logMessage(error);
    return error;
    }
    String result = new String(receivePacket.getData(), 0, receivePacket.getLength());
    String[] parsedMessage = this.extractChecksum(result);
    if (this.validateMessage(parsedMessage)) { // handle any malformed replies
      String log = "Return message received from server: " + parsedMessage[1];
      this.logger.logMessage(log);
    } else {
      String log = "Packet received from server malformed.";
      this.logger.logMessage(log);
    }
    return parsedMessage[1];
  }

  /**
   * Method to communicate continuously with server until
   * user shuts it down.
   * @throws IOException
   */
  public void communicateWithServer() throws IOException {
    String message = "";
    Boolean flag = true;
    while (flag) {
      // new message and send to server
      message = this.sendPacket(this.collectInput());
      if (message.toLowerCase().equals("q")) { // kill server and client
        flag = false;
      }
      // receive reply
      byte[] receiveMessage = new byte[1024];
      //try {
        String result = this.receiveData(receiveMessage, receiveMessage.length);
        System.out.println("Result-> " + result);
      //} catch (java.net.SocketTimeoutException e) { // timeout if no response received
      //  String error = "Server unresponsive, timeout mechanism executed";
      //  System.out.println(error);
      //  this.logger.logMessage(error);
      //}
    }
  }

  /**
   * Method to prepopulate server keyValue store with data.
   * @throws IOException
   */
  public void prePopulateServer() throws IOException {
    String[] requests = new String[] {"put, class, CS6650", "put, semester, Spring2024",
            "put, professor, Saripalli", "put, program, MSCS", "put, university, Northeastern"};
    byte[] receiveMessage = new byte[1024];
    String result = "";
    for (int i=0;i<5;i++) {
      try {
        this.sendPacket(requests[i]);
        result = this.receiveData(receiveMessage, receiveMessage.length);
        // print response to log
        System.out.println(result);
      } catch (Exception e) {
        this.logger.logMessage("Unable to send pre-populated data.");
      }
    }

  }

  /**
   * Method to perform 5 put, get, and delete requests automatically.
   */
  public void autoPopulateServer() {
    byte[] receiveMessage = new byte[1024];
    String result = "";
    // Perform 5 Put Requests
    for (String key : this.hashMap.keySet()) {
      try { // put requests
        this.sendPacket("put, " + key + ", " + this.hashMap.get(key));
        result = this.receiveData(receiveMessage, receiveMessage.length);
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        // print to log
      }
      try { // get requests
        this.sendPacket("get, " + key);
        result = this.receiveData(receiveMessage, receiveMessage.length);
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        // print to log
      }
      try { // delete requests
        this.sendPacket("delete, " + key);
        result = this.receiveData(receiveMessage, receiveMessage.length);
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        // print to log
      }
    }
  }

  /**
   * Method sets the host and port number to send requests to.
   * @param host string representation of host
   * @param port number
   */
  public void setHostAndPort(String host, int port) {
    // allow either localhost or inet address to be entered
    try {
      if (host.equals("localhost")) {
        this.address = InetAddress.getByName(host);
      } else {
        byte[] add = host.getBytes();
        this.address = InetAddress.getByAddress(add);
      }
    } catch(UnknownHostException e) {
      System.out.println("Unable to get host.");
    }
    // if an int was not entered for port
    try {
      this.port = port;
    } catch (Exception e) {
      System.out.println("Unable to parse port number.");
    }
  }

  /**
   * Main driver method for ClientUDP class.
   * @param args inputted
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // parse input args
    String[] newArgs = AbstractClient.parseArgs(args);
    String host = newArgs[0];
    int port = Integer.parseInt(newArgs[1]);
    String message = "";
    // initialize client and set host and port to send to
    ClientUDP client = new ClientUDP();
    client.setHostAndPort(host, port);
    // pre-populate keyValue store
    client.prePopulateServer();
    // perform 5 pre-defined put, get, delete requests
    client.autoPopulateServer();
    // communicate with server via user input
    client.communicateWithServer();
  }
}
