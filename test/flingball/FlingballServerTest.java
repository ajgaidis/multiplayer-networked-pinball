package flingball;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * A class to test the functionality and correctness of FlingballServer, as well as
 * layout the testing strategy used. 
 * 
 * @category no_didit
 */
public class FlingballServerTest {

    // /\/\/\/\/\/\/\/\/\
    //  Testing Strategy
    // \/\/\/\/\/\/\/\/\/
    //
    // The majority of our server tests were conducted as system tests following the partition
    // provided below. We made numerous extra board files to test special cases. These special
    // cases are integrated into our partitioning.
    //
    // The server's main job is to facilitate message passing between two clients. Thus, 
    // Testing the server can be broken into three parts: joining boards, teleporting balls,
    // and disconnecting and quitting the server.
    // 
    // ~~~ Joining Boards Partition ~~~
    //  - Two boards are the same
    //  - Two boards are different
    //  - Try to join a board to a board that doesn't exist
    //  - A board overrides a previous connection and joins to another board
    //
    // ~~~ Teleporting Balls Partition ~~~
    //  - Corresponding Board does not exist
    //  - A teleport would cause the teleported ball to be inside an object that isn't a portal 
    //    or an absorber (in which case we just get rid of the ball entirely from game play, if
    //    the ball ends up inside a portal or an absorber it is absorbed for the absorber and
    //    teleported for the portal) 
    //  - Teleporting Balls through one Board's walls to another's and from
    //    one Board's walls to own walls
    //      - Left -> Right
    //      - Right -> Left
    //      - Top -> Bottom
    //      - Bottom -> Top
    //  - Teleporting Balls from one Portal to another
    //      - Corresponding Portal does not exist
    //      - Ball enters the first Portal at all locations around its circumference
    //          and exits the second Portal at the opposite side of the Portal
    //
    // ~~~ Disconnecting and Quitting Server Partition~~~
    //  - Disconnect walls that are connected to other walls on the same Board as well as different Board
    //  - Disconnect portals after a client disconnects from the server 
    //     (balls should just pass over disconnected portals)
    //  - Quit the server gracefully
    
    private static final String LOCALHOST = "127.0.0.1";
    
    /* Start server on its own thread. */
    private static Thread startServer() {
        Thread thread = new Thread(() ->  {
            try {
                new FlingballTextServer(10987).serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }
    
    @Test(expected = AssertionError.class) 
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // Tests the server's capability to send messages to teleport balls through portals and walls
    // from one client to another as well as one one client to themselves. Since the bulk of our
    // server and client methods are private, this function serves as the main check to ensure that
    // server is passing messages appropriately according the the wire protocol. All other tests
    // were system tested.
    @Test
    public void testJoiningBoardsDifferentOverridingSame() 
            throws UnableToParseException, IOException, InterruptedException {
        Thread server = startServer();

        final Socket clientSocket1 = new Socket(LOCALHOST, 10987);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(clientSocket1.getOutputStream(), true);
        
        final Socket clientSocket2 = new Socket(LOCALHOST, 10987);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(clientSocket2.getOutputStream(), true);
        
        assertEquals("expected server to try to obtain a board name", "getClientBoardName", in1.readLine());
        assertEquals("expected server to try to obtain a board name", "getClientBoardName", in2.readLine());
        out1.println("Client1");
        out1.flush();
        assertEquals("expected server to response with connected clients", 
                "success allConnectedBoards= Client1", in1.readLine());
        
        out2.println("Client2");
        out2.flush();
        assertEquals("expected server to response with connected clients", 
                "success allConnectedBoards= Client1 Client2", in2.readLine());
        assertEquals("expected server to response with connected clients", 
                "success allConnectedBoards= Client1 Client2", in1.readLine());
        
        // teleport balls through walls
        out2.println("teleportWall= Client1 ball1 10.0 10.0 10.0 10.0 left");
        out2.flush();
        assertEquals("expected teleported ball", 
                "success teleportWall= Client1 ball1 10.0 10.0 10.0 10.0 left", in1.readLine());
        out1.println("teleportWall= Client2 ball2 10.0 10.0 10.0 10.0 right");
        out1.flush();
        assertEquals("expected teleported ball", 
                "success teleportWall= Client2 ball2 10.0 10.0 10.0 10.0 right", in2.readLine());
        // teleport balls through portals
        out2.println("teleportPortal= Client1 ball1 7.51 9.6 Portal1");
        out2.flush();
        assertEquals("expected teleported ball", 
                "success teleportPortal= Client1 ball1 7.51 9.6 Portal1", in1.readLine());
        out1.println("teleportWall= Client2 ball2 2.0 0.0 Portal2");
        out1.flush();
        assertEquals("expected teleported ball", 
                "success teleportWall= Client2 ball2 2.0 0.0 Portal2", in2.readLine());
        out1.println("teleportWall= Client1 ball3 2.0 1.0 Portal3");
        out1.flush(); // portal send a ball to its own board
        assertEquals("expected teleported ball", 
                "success teleportWall= Client1 ball3 2.0 1.0 Portal3", in1.readLine());

        clientSocket1.close();
        clientSocket2.close();
        server.join(1);
    }
    
    
    /* SYSTEM TESTS RAN */
    
    // To verify that board's were allowed to be parsed and handled with no name specified, we ran
    // empty.fb. This file didn't contain name in the board.fb file. Running this, we found that it
    // handled all conditions just fine.
    
    // We made three separate boards (SimplePortal1, SimplePortal2, SimplePortal3) with three
    // portals, one on each board. These portals were connected to each other in a cycle. To test
    // we disconnected one of the board's and observed that that did not affect the connections
    // between the two remaining boards. Additionally, we joined their walls together to ensure that
    // our server could take input from the command line, parse it, and successfully join boards that
    // pass balls between themselves. Finally, we ensured that when boards get reassigned to join
    // with another board, the text that is written on their walls gets removed or replaced corresponding
    // to these changes. We checked to see if this held true when boards were disconnected especially
    // making sure that boards disconnected from the server had no text written on them.
    
    // On board default3.fb we created 16 balls and varied their velocities, sometimes past 2000. This
    // ensured that our game mechanics worked in terms of both speed and a high number of objects
    // colliding and interacting with each other. By combining this board with portal2.fb and portal3.fb
    // we could see that our server could handle more traffic than just a single ball at a time.
    
    // On board flippers_key_controlled we created some flippers that are triggered by key strokes to test that 
    // keyboard input is handled appropriately. This was run through the server with multiple command line
    // argument configurations to ensure that they work and to ensure that keyboard input works for files
    // especially those started from the command line. 
}
