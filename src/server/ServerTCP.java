package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

// Helper Class for Server
public class ServerTCP extends AbstractServer {

  private ServerSocket socket;
  private Socket s;
  private InputStream sIn;
  private DataInputStream dis;
  private OutputStream sout;
  private DataOutputStream dos;

  /**
   * Constructor.
   */
  public ServerTCP() {
    super();
  }

  /**
   * Method initializes a new socket and waits for a client.
   * @param port of socket
   * @throws IOException
   */
  public void initializeServerSocket(int port) throws IOException {
    this.port = port;
    this.socket = new ServerSocket(port);
    this.s = this.socket.accept();
  }

  /**
   * Method sets the InetAddress of client and
   * acknowledges the connection made.
   */
  public void acknowledgeConnectionWithClient() {
    this.clientAddress = this.socket.getInetAddress();
    this.logger.logMessage("Connection with " + this.clientAddress + " established.");
    System.out.println("Connection with Client Established");
  }

  /**
   * Method to collect Input from client, check checksum,
   * and return the message without the checksum header.
   * @return message without checksum
   * @throws IOException
   */
  public String[] receiveData() throws IOException {
    // get input
    this.sIn = this.s.getInputStream();
    this.dis = new DataInputStream(sIn);
    String packet = new String(this.dis.readUTF()); // decode response
    String[] parsedPacket = this.extractChecksum(packet);
    System.out.println("Message Received: " + parsedPacket[1]);
    return parsedPacket;
    /*
    if (parsedPacket != null) {
      System.out.println("Message Received: " + parsedPacket[1]);
      return parsedPacket;
    } else {
      return null;
    }
     */
  }

  /**
   * Method to send a response to the client based on their request.
   * @param result message from client
   * @return result
   * @throws IOException
   */
  public String sendPacket(String result) throws IOException {
    this.logger.logMessage("Sending to client: " + result);

    this.sout = this.s.getOutputStream();
    this.dos = new DataOutputStream(sout);
    String packagedResult = this.getChecksum(result);
    // send note back to client.client
    this.dos.writeUTF(packagedResult);
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
      this.sendPacket(result);
    }
  }

  /**
   * Method to close sockets and output streams.
   * @throws IOException
   */
  public void closeAll() throws IOException {
    this.socket.close();
    this.s.close();
    this.dos.close();
    this.sout.close();
  }

  /**
   * Main driver method for ServerTCP class.
   * @param args from user
   * @throws IOException
   */
  public static void main(String[] args ) throws IOException, InterruptedException {
    // parse input args
    int port = AbstractServer.parseArgs(args);
    // initialize new ServerSocket and wait for connection
    ServerTCP serverTCP = new ServerTCP();
    serverTCP.initializeServerSocket(port);
    // acknowledge connection
    serverTCP.acknowledgeConnectionWithClient();
    // communicate with client
    serverTCP.acceptNotesFromClient();
    // close
    serverTCP.closeAll();
  }

}
