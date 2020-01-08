package flingball;

import java.io.IOException;
import java.util.Arrays;

/**
 * server to run the Flingball game
 */
public class FlingballServer {
    
    private static int port;
    
    // Abstraction function:
    //   AF(port): A server that listens for connections on port
    // Representation invariant:
    //   port must be in the range [0,65535]
    // Safety from rep exposure:
    //   all fields are private and final
    //   all methods have a void return type or return an immutable String
    // Thread safety argument:
    //   No rep exposure
    //   port is only modified in main, but only one instance of main can be run at a time
    
    /**
     * Asserts the rep invariant
     */
    private static void checkRep() {
        final int maxPort = 65535;
        assert 0 <= port && port <= maxPort;
    }
    
    /**
     * Start a server using the given arguments
     * and read command-line interface to join boards together on the server
     * 
     * The server will listen for connecting clients and expects clients to send a board object
     *  via an ObjectOutputStream
     *  
     *  The server will listen for additional input from the command line after initialization. 
     *  Specifically, two boards can be joined vertically with the command 'v [Board1] [Board2]'
     *  where [Board1] is the top join and [Board2] is the bottom join. Alternatively, two boards
     *  can be joined horizontally with the command 'h [Board1] [Board2]' where [Board1] is the
     *  left join and [Board2] is the right join. Finally, a server can be disconnected by prompting
     *  the server with a 'disconnect' command at the command prompt. 
     *  
     * @param args a single optional parameter of the form '--port PORT' that defines
     *  what port should be used to listen for incoming connections. If no port is given,
     *  then the default port 10987 is used
     *  @throws IOException if an error occurs starting the server
     *  @throws IllegalArgumentException if there are two arguments and there is no --port flag
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException {
        if (args.length == 2) {
            if (args[0] != "--port")
                throw new IllegalArgumentException("must state the --port flag and then a port value");
            final String[] commands = Arrays.asList(args).get(0).split(" ");
            port = Integer.parseInt(commands[1]);
        } else if (args.length == 0) {
            port = Flingball.DEFAULT_PORT;
        } else {
            throw new IllegalArgumentException("The arguments passed into FlingballServer were invalid.");
        }
        
        checkRep();
        new FlingballTextServer(port).serve();
        return;
    }
    
    /**
     * @return the port that the server is operating on
     */
    public int port() {
        final int p = port;
        return p;
    }
    
}
