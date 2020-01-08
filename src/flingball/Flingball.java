package flingball;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Flingball is a 2D game similar to pinball. Running Flingball instantiates a
 * GUI window where gadgets (such as bumpers) in the Flingball board interact
 * with any flingballs in the board. Flingball can also simulate the effects of
 * gravity and friction. This class uses a Flingball board file, denoted by the extension
 * .fb to specify the board construction.
 */
public class Flingball {

    public static final int DEFAULT_PORT = 10987;
    
    // Abstraction function:
    //   AF(DEFAULT_PORT): A client that connects to a server via port DEFAULT_PORT if no 
    //                     port is specified through the command line
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
        assert 0 <= DEFAULT_PORT && DEFAULT_PORT <= maxPort;
    }
    
    /**
     * To run a Flingball game on command line interface: 
     * `java -cp bin:lib/parserlib.jar:lib/physics.jar flingball.Flingball [--host $HOST] 
     * [--port ${PORT] $FILE` where $HOST is an optional hostname or IP address of the server
     * to connect to. IF no $HOST is provided then the client runs in single-machine play mode
     * as described in the handout. $PORT is an optional integer in the range [0,65535] specifying
     * the port where the server is listening for incoming connections. If no port is supplied, 
     * the default port used is 10987. $FILE is the path to a file with the extension .fb following
     * correct Flingball board formatting. If no $FILE is provided, runs using boards/default.fb
     * and $HOST. In order to exit game play, a player must type 'quit' into the terminal in which
     * they instantiated game play.
     * 
     * @param args arguments for the program as detailed above
     * @throws IOException if the board file to read in cannot be parsed
     * @throws UnknownHostException if the server host cannot be recognized
     * @throws IllegalArgumentException if the arguments passed in aren't valid
     */
    public static void main(String[] args) 
            throws UnknownHostException, IOException, IllegalArgumentException { 
        Optional<String> hostName = Optional.empty();
        int port = DEFAULT_PORT;
        File fileToUse = new File("boards/default.fb");
        
        if (args.length > 0) {
            for (int i = 0; i < args.length - 2; i += 2) {
                if (args[i].equals("--host")) {
                    hostName = Optional.of(args[i + 1]);
                } else if (args[i].equals("--port")) {
                    port = Integer.valueOf(args[i + 1]);
                } else {
                    throw new IllegalArgumentException("Arguments or flags passed in were invalid.");
                }
            }
            if (args.length % 2 != 0)
                fileToUse = new File(args[args.length - 1]);
        } // else everything is default!
        
        checkRep();
        try {
            final Board flingBall = BoardParser.parse(fileToUse);
            final Simulator simulator = new Simulator(flingBall);
            
            if (hostName.isPresent()) {
                final Socket socket = new Socket(hostName.get(), port);
                flingBall.acceptSocket(socket);
                simulator.playFlingball();
                while (true) {
                    final String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    if (input.equals("quit")) {
                        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("quit");
                        out.flush();
                        out.close();
                        System.exit(0);
                    }
                }
            } else {
                simulator.playFlingball();
            }
        } catch (UnableToParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a board file and turns it into one big String
     * 
     * @param boardFile the board file to process and turn into a String
     * @return the String representation of the board file
     * @throws UnableToParseException if there was an error reading in a boardFile as a String
     */
    public static String boardFileToString(File boardFile) throws UnableToParseException {
        List<String> sampleBoard = new ArrayList<>();
        try {
            sampleBoard = Files.readAllLines(boardFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StringBuilder builder = new StringBuilder();
        for (String line : sampleBoard)
            builder.append(line + "\n");
        return builder.toString();
    }
}
