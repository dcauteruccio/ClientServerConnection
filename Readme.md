# ReadMe
This program represents a Single Threaded client/server application, realized as both a TCP connection or a UDP connection. 
For this application there are two main packages, a client package and a server package, and the TCP and UDP connections
are two totally separate sets of applications.
Each package contains one Abstract class (AbstractClient and AbstractServer) that contains generalized methods required for 
both UDP and TCP clients and servers, and one logger class that helps log all incoming and outgoing activity to their 
respective logs. Then, separate TCP and UDP classes are included to fulfill the requirements of each type of connection.

To run the applications, open two terminal windows and navigate to the src folder where the two packages are located.
In one window, compile all the server code using javac on the whole package, and in the other window do the same for the 
client package (run javac client/**.java). Once compiled, decide which connection type you want to use.
For a TCP connection, run "java server.ServerTCP port" in one window, and "java client.ClientTCP hostname port" in the other
window. The client files can also be called with just the port number, or with neither port nor hostname. If either of those cases
occur, localhost and port 4999 will be used. The same is true of the server files; if no args are entered, port 4999 is used.
To run the UDP connections, run "java server.ServerUDP port" and "java client.ClientUDP hostname port". Again, the same note
applies, both can be run with less args and still work.

Once the client and server are running, the client will start by populating 5 key/value pairs into the server's key value store and
then performing 5 more puts, 5 gets, and 5 deletes. After that, the user will be prompted to enter a request that they can send to 
the server. The format of the requests should follow the below (case insensitive):

- For put requests: "put, key, value"
- For get requests: "get, key"
- For delete requests: "delete, key"

Once you type your request, hit enter and it will be sent to the server. The server will use checksum to make sure the packet
didn't get corrupted during transit, and then check that the appropriate format was followed with the request. If the format 
of the request is incorrect, you will receive a message noting the request was not properly formatted. The client, upon receiving
the server's response, will also use checksum to ensure the packet did not get corrupted. Once, this journey is complete, the user
can enter a new request to send to the server. 

If the user wants to quit, they can type "q" and send to the server. This will close both the server and the client applications.