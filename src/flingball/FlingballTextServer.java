package flingball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable, threadsafe text protocol flingball server
 *
 */
public class FlingballTextServer {

    private final ServerSocket serverSocket;
    private final Map<String, Socket> socketMappings = Collections.synchronizedMap(new HashMap<>());
    
    // Abstraction function:
    //   AF(serverSocket, socketMappings>: A text server that handles socket connections through serverSocket and
    //                                       sends inputs for clients running boardName through the socket in the
    //                                       appropriate mapping from socketMappings
    // Representation invariant:
    //  No two strings map to the same socket in socketMappings
    // Safety from rep exposure:
    //  all fields are final and private
    //  Only handleRequests (which is private) and getBoardName (which returns an immutable string)
    //    return anything
    // Thread safety argument:
    //  all fields are private + final
    //  socketMappings uses a threadsafe hashmap
    //  any uses of the fields are atomic operations
    //  disconnectAll and sendJoinedBoards and synchronized and can only be accessed by one 
    //      thread at a time

    /**
     * Asserts the rep invariant
     */
    private void checkRep() {
        assert serverSocket!=null;
        assert socketMappings!=null;
        final Set<Socket> uniqueSockets = new HashSet<>();
        for (Socket socket : socketMappings.values()) {
            uniqueSockets.add(socket);
        }
        assert uniqueSockets.size() == socketMappings.size();
    }

    /**
     * Creates a new text server that listens for connections on port
     * 
     * @param port the port for the server to listen on
     * @throws IOException if there is an error opening the server socket
     */
    public FlingballTextServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        checkRep();
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return serverSocket.getLocalPort();
    }
    
    /**
     * Gets the name of a board used by a particular client
     * 
     * @param socket the socket shared by a client running the board with the requested name
     * @return the name of the board connected to this server through the given socket
     * @throws IOException if error occurs reading in from the socket 
     */
    private static String getBoardName(Socket socket) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("getClientBoardName");
        out.flush();
        final String boardName = in.readLine();
        return boardName;
    }
    
    /**
     * Run the server, listening for and handling client connections.
     * Never returns normally.
     * 
     * @throws IOException if an error occurs waiting for a connection
     */
    public void serve() throws IOException {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        String line = "";
                        
                        while (line != "//n" && line != null) {
                            String message = "";
                            line = reader.readLine();
                            message = message + line;
                            if (message.equals("disconnect")) {
                                disconnectAll();
                                System.exit(0);
                            } else {
                                sendJoinBoards(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }   
                }
            }
        }).start();
        
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            
            // handle the client
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String boardName = getBoardName(socket);
                        
                        socketMappings.put(boardName, socket);
                        sendConnectedBoards();
                        handleConnection(socket, boardName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();  
        }
    }
    
    /**
     * Sends a message showing all connected boards, separated by new lines, to all currently 
     * connected clients 
     * 
     * @throws IOException if there is an error writing to one of the board sockets
     */
    private synchronized void sendConnectedBoards() throws IOException {
        final Set<String> connectedBoards = Collections.synchronizedSet(new HashSet<>(socketMappings.keySet()));
        final StringBuilder response = new StringBuilder();
        response.append("success ");
        response.append("allConnectedBoards=");
        for (String board : connectedBoards) { response.append(" " + board); }
        
        for (Socket socket : socketMappings.values()) {
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(response.toString());
            out.flush();
        }
        checkRep();
    }
    
    /*
     * Disconnects all clients and closes the server
     */
    private synchronized void disconnectAll() throws IOException {
        for (Socket socket : socketMappings.values()) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("success allConnectedBoards=");
            out.flush();
            out.println("disconnect");
            out.flush();
        }
    }
    
    /**
     * Sends a message to the boards being joined by the message about their joining
     * 
     * @param message the joining boards message. Must be of the form "h board1 board2" or "v board1 board2"
     * @throws IOException if there is an error writing to one of the board sockets
     */
    private void sendJoinBoards(String message) throws IOException {
        final String[] messageSplit =  message.split("[ ]+");
        final StringBuilder response = new StringBuilder();
        response.append("success ");
        if (messageSplit[0].equals("h")) {
            response.append("joinHorizontal= ");
            final String leftBoard = messageSplit[1];
            final String rightBoard = messageSplit[2];
            final String disconnectMessageLeft = "success disconnectWall= " + leftBoard + " left"; 
            final String disconnectMessageRight = "success disconnectWall= " + rightBoard + " right"; 
            for (String boardName : this.socketMappings.keySet()) {
                if (!boardName.equals(leftBoard) && !boardName.equals(rightBoard)) {
                    final PrintWriter firstBoardout = new PrintWriter(this.socketMappings.get(boardName).getOutputStream(), true);
                    firstBoardout.println(disconnectMessageLeft);
                    firstBoardout.flush();
                    firstBoardout.println(disconnectMessageRight);
                    firstBoardout.flush();
                }
            }
      
        } else if (messageSplit[0].equals("v")) {
            response.append("joinVertical= ");
            final String topBoard = messageSplit[1];
            final String bottomBoard = messageSplit[2];
            final String disconnectMessageTop = "success disconnectWall= " + topBoard + " top"; 
            final String disconnectMessageBottom = "success disconnectWall= " + bottomBoard + " bottom"; 
            for (String boardName : this.socketMappings.keySet()) {
                if (!boardName.equals(topBoard) && !boardName.equals(bottomBoard)) {
                    final PrintWriter firstBoardout = new PrintWriter(this.socketMappings.get(boardName).getOutputStream(), true);
                    firstBoardout.println(disconnectMessageTop.toString());
                    firstBoardout.flush();
                    firstBoardout.println(disconnectMessageBottom.toString());
                    firstBoardout.flush();
                }
            }
        }
        final String firstBoard = messageSplit[1];
        final String secondBoard = messageSplit[2];
        response.append(firstBoard);
        response.append(" " + secondBoard);
        final Socket firstBoardSocket = socketMappings.get(firstBoard);
        final Socket secondBoardSocket = socketMappings.get(secondBoard);
        final PrintWriter firstBoardout = new PrintWriter(firstBoardSocket.getOutputStream(), true);
        firstBoardout.println(response.toString());
        firstBoardout.flush();
        final PrintWriter secondBoardout = new PrintWriter(secondBoardSocket.getOutputStream(), true);
        secondBoardout.println(response.toString());
        secondBoardout.flush();
        checkRep();
    }
    
    /**
     * Handle a single client connection.
     * Returns when the client disconnects.
     * 
     * @param socket socket connected to client
     * @param boardName the name of the board using this connection
     * @throws IOException if the connection encounters an error or closes unexpectedly
     */
    private void handleConnection(Socket socket, String boardName) throws IOException {
        
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            for (String input = in.readLine(); input != null; input = in.readLine()) {
                if (input.isEmpty()) {
                    continue;
                }
               
                final Map<String, Socket> outputToSocketMapping = handleRequest(input, socket);
                if (outputToSocketMapping.isEmpty()) {
                   
                    this.socketMappings.remove(boardName);
                    sendConnectedBoards();
                    
                } else {
                    assert outputToSocketMapping.size() == 1;
                    for (String output : outputToSocketMapping.keySet()) {
                        if (outputToSocketMapping.get(output) == null) {
                            break;
                        }
                        out = new PrintWriter(outputToSocketMapping.get(output).getOutputStream(), true);
                        out.println(output);
                    }
                    out.flush();
                }
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } 
    }
    
    /**
     * Handle a single client request and return the server response.
     * 
     * Portal teleportation input: "teleport sourceBoard targetBoard targetPortal"
     *                      output: "success" + input or "failure"
     *                      
     * Wall Traversal input: "wall sourceBoard ballName"
     *                output: "success" + input or "failure"
     * 
     * @param input message from client
     * @param socket the socket making the request
     * @return output message to client
     * @throws IOException if unable to read or write to socket
     */
    private Map<String, Socket> handleRequest(String input, Socket socket) throws IOException {
        try {
            if (input.equals("quit")) {
                return Collections.emptyMap();
            }
            final int targetBoardNameIndex = 1;
            final String[] splitInput = input.split("[ ]+");
            final String targetBoardName = splitInput[targetBoardNameIndex];
            final Socket targetBoardSocket = socketMappings.get(targetBoardName);
            return Collections.singletonMap(("success " + input), targetBoardSocket);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("failure", socket);
        }
    }

}
