package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class to represent a client using a TCP connection.
 */
public class ClientTCP extends AbstractClient {

  private Socket clientSocket;
  private String host;
  private int port;
  // for sending
  private OutputStream sout;
  private DataOutputStream dos;
  // for receiving
  private InputStream sIn;
  private DataInputStream dis;


  /**
   * Constructor.
   */
  public ClientTCP() {
    super();
  }

  /**
   * Method to initialize a Datagram socket for the UDP Client
   * to send and receive messages from.
   * @throws SocketException
   */
  public Socket initializeSocket(String host, int port) throws IOException {
    this.host = host;
    this.port = port;
    // initialize socket
    this.clientSocket = new Socket(host, port);
    this.clientSocket.setSoTimeout(15000); // set timeout for 15 seconds

    // prepare for return message
    this.sIn = this.clientSocket.getInputStream();
    this.dis = new DataInputStream(this.sIn);

    return this.clientSocket;
  }

  /**
   * Close client socket and input streams when done.
   * @throws IOException
   */
  public void closeAll() throws IOException {
    this.dis.close();
    this.sIn.close();
    this.clientSocket.close();
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

    // get ready to output
    this.sout = this.clientSocket.getOutputStream();
    this.dos = new DataOutputStream(this.sout);

    this.dos.writeUTF(packet); // send
    String log = "Request sent to server: " + message;
    this.logger.logMessage(log);
    return message;
  }

  /**
   * Method to receive responses from the server.
   *
   * @return the decoded string of the message
   * @throws IOException
   */
  public String receiveData()  throws IOException {
    String result = "";
    try {
      this.sIn = this.clientSocket.getInputStream();
      result = new String(dis.readUTF()); // decode response
    } catch (java.net.SocketTimeoutException e) { // timeout if no response received
      String error = "Server unresponsive, timeout mechanism executed";
      System.out.println(error);
      this.logger.logMessage(error);
      return error;
    }
    this.dis = new DataInputStream(sIn);


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

  public void communicateWithServer() throws IOException {
    String message = "";
    Boolean flag = true;
    // keep sending and receiving messages until q
    //!message.toLowerCase().equals("q")
    while (flag) {
      message = this.sendPacket(this.collectInput());
      if (message.toLowerCase().equals("q")) { // kill server and client
        flag = false;
      }
      // get return message from server.server
      try {
        System.out.println("return message received from server:");
        String st = this.receiveData();
        System.out.println(st);
      } catch (java.net.SocketTimeoutException e) {
        String error = "Server unresponsive, timeout mechanism executed";
        System.out.println("error");
        this.logger.logMessage(error);
      }
    }
  }

  /**
   * Method to prepopulate server keyValue store with data.
   * @throws IOException
   */
  public void prePopulateServer() throws IOException {
    String[] requests = new String[]{"put, class, CS6650", "put, semester, Spring2024",
            "put, professor, Saripalli", "put, program, MSCS", "put, university, Northeastern"};
    byte[] receiveMessage = new byte[1024];
    String result = "";
    for (int i = 0; i < 5; i++) {
      try {
        this.sendPacket(requests[i]);
        result = this.receiveData();
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
    String result = "";
    // Perform 5 Put/Get/Delete Requests
    for (String key : this.hashMap.keySet()) {
      try { // put requests
        this.sendPacket("put, " + key + ", " + this.hashMap.get(key));
        result = this.receiveData();
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        this.logger.logMessage("Unable to sent put request for key: " + key);
      }
      try { // get requests
        this.sendPacket("get, " + key);
        result = this.receiveData();
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        this.logger.logMessage("Unable to sent get request for key: " + key);
      }
      try { // delete requests
        this.sendPacket("delete, " + key);
        result = this.receiveData();
        // print response to log
        System.out.println(result);
      } catch (IOException e) {
        this.logger.logMessage("Unable to sent delete request for key: " + key);
      }
    }
  }

  /**
   * Main driver for ClientTCP Class.
   * @param args inputted
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    // parse host and port from args
    String[] newArgs = AbstractClient.parseArgs(args);
    String host = newArgs[0];
    int port = Integer.parseInt(newArgs[1]);
    String message = "";
    // initialize client and socket
    ClientTCP client = new ClientTCP();
    client.initializeSocket(host, port);
    // pre-populate keyValue store
    client.prePopulateServer();
    // 5 put, get, delete requests
    client.autoPopulateServer();
    // communicate with server via user input
    client.communicateWithServer();
  }

}
