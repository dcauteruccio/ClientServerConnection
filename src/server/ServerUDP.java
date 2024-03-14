package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

/**
 * Class to represent a UDP Server.
 */
public class ServerUDP extends AbstractServer {

  private DatagramSocket serverSocket;
  private byte[] receiveData;
  private InetAddress address; // client address
  private int portOrigin;

  /**
   * Constructor.
   */
  public ServerUDP() {
    super();
  }

  /**
   * Method initializes a new socket and waits for a client.
   * @param port of socket
   * @throws IOException
   */
  public void initializeUDPServerSocket(int port) throws SocketException {
    // to do
    this.serverSocket = new DatagramSocket(port);
    this.receiveData = new byte[1024];
    //return new DatagramSocket(port);
  }


  /**
   * Method to take in a request from the client, extract out the checksum,
   * and return the parsed packet.
   *
   * @return parsed packet with checksum header and request separated
   * @throws IOException
   */
  public String[] receiveData() throws IOException {
    this.receiveData = new byte[1024];
    // receive
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    this.serverSocket.receive(receivePacket);
    // log address and origin port
    this.address = receivePacket.getAddress();
    this.portOrigin = receivePacket.getPort();
    // decode
    String packet = new String(receivePacket.getData(), 0, receivePacket.getLength());
    String[] parsedPacket = this.extractChecksum(packet);
    System.out.println("Message Received: " + parsedPacket[1]);

    return parsedPacket;
  }

  /**
   * Method to send a response to the client based on their request.
   * @param result message from client
   * @return result
   * @throws IOException
   */
  public String sendPacket(String result) throws IOException {
    this.logger.logMessage("Sending to client: " + result);

    String packagedResult = this.getChecksum(result);
    byte[] sendData = packagedResult.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
            this.address, this.portOrigin);
    this.serverSocket.send(sendPacket);
    return result;
  }

  /**
   * Method to run accepting multiple inputs from a given client.
   * @throws IOException
   */
  public void acceptNotesFromClient() throws IOException, InterruptedException {
    String result = "";
    Boolean flag = true;

    while (flag) {
      String[] parsedPacket = this.receiveData(); // get message and extract checksum
      if (parsedPacket[1].toLowerCase().equals("q")) { // kill if receive q
        flag = false;
        result = "Quit requested. Server shutting down.";
        System.out.println(result);
        this.logger.logMessage(result);
      } else {
        if (this.validateMessage(parsedPacket)) { // confirm checksum matches
          result = this.processRequest(parsedPacket); // confirm request is ok and process
        } else {
          result = "Datagram packet malformed.";
        }
      }
      this.logger.logMessage(result);
      this.sendPacket(result); // send
    }
  }

  /**
   * Method to close socket.
   * @throws IOException
   */
  public void closeAll() throws IOException {
    this.serverSocket.close();
  }

  /**
   * Main driver method for ServerUDP class.
   * @param args from user
   * @throws IOException
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    // parse input args
    int port = AbstractServer.parseArgs(args);
    // initialize UDP server
    ServerUDP serverUDP = new ServerUDP();
    serverUDP.initializeUDPServerSocket(port);
    // communicate with client
    serverUDP.acceptNotesFromClient();
    // close
    serverUDP.closeAll();
  }


}
