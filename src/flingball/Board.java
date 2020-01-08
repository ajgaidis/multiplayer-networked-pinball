package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import physics.LineSegment;
import physics.Physics;
import physics.Vect;

/**
 * A mutable game board of the Flingball game
 */
public class Board {

    private String name;
    private final List<Ball> balls;
    private final List<Bumper> bumpers;
    private final List<Absorber> absorbers;
    private final List<Flipper> flippers;
    private final List<Portal> portals;
    private final List<Portal> localPortals;
    private final List<LineSegment> walls;
    private float gravity;
    private float friction1;  // units of per second
    private float friction2;  // units of per L
    
    
    private Optional<Socket> socket = Optional.empty();
    
    private List<String> connectedBoards = new LinkedList<>();
    private final Map<Wall, Optional<String>> joinedBoards = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Wall, Wall> WALL_TO_TARGET_WALL = Collections.synchronizedMap(new HashMap<>());
    private static final Map<LineSegment, Wall> LINE_SEGMENT_TO_WALL = Collections.synchronizedMap(new HashMap<>());
    
    private final Map<Gadget, List<Absorber>> triggerAbsorberMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Gadget, List<Flipper>> triggerFlipperMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Absorber, List<String>> absorberBallNamesMap = Collections.synchronizedMap(new HashMap<>());
    
    private final List<String> protoListeners = Collections.synchronizedList(new LinkedList<>());
    private final Map<String, Boolean> portalConnected = Collections.synchronizedMap(new HashMap<>());
        
    private final Color color = Color.WHITE;
    public static final double TIME = 0.001;
    public static final double EPSILON_16 = 1e-16;
    public static final double EPSILON_14 = 1e-14;
    public static final double EPSILON_12 = 1e-12;
    public static final double EPSILON_9 = 1e-9;
    public static final double EPSILON_7 = 1e-7;
    public static final double EPSILON_3 = 1e-3;
    public static final int L = 20;  // 1L = 20 pixels
    public static final int PIXELS_PER_L = 20;
    private static final float GRAVITY_DEFAULT = 25;  // units of L/(sec^2)
    private static final float FRICTION_DEFAULT = 0.025f;
    
    // Abstraction Function:
    //  AF(name, balls, bumpers, absorbers, flippers, portals, localPortals, walls, gravity,
    //     friction1, friction2, socket, connectedBoards, joinedBoards, WALL_TO_TARGET_WALL,
    //     LINE_SEGMENT_TO_WALL, triggerAbsorberMap, triggerFlipperMap, absorberBallNamesMap,
    //     protoListeners, portalConnected, COLOR, TIME, L, PIXELS_PER_L) =
    //     
    //      A fling ball board that is identified by name. The following are the mappings of board
    //      from its concrete implementations to the corresponding abstract spaces.
    //      - balls : the balls that are contained on the board and are considered in game play
    //      - bumpers : the bumpers that are on this flingball board that balls can bounce off
    //      - absorbers : the absorbers that are on this flingball board that can absorb and shoot balls
    //      - flippers : the flippers that are on this flingball board that can rotate to hit balls
    //      - portals : the portals that are on this flingball board that can teleport balls to locations
    //      - localPortals : the portals that are on this flingball board that teleport balls to portals
    //                       also located on this flingball board
    //      - walls : the four line segments that are the borders of this flingball board
    //      - gravity : the gravity that affects the movement of balls
    //      - friction1 : the velocity-independent friction that affects the movement of balls
    //      - friction2 : the velocity-dependent friction that affects the movement of balls
    //      - socket : the connection that delivers messages to the server to allow communication
    //                 between multiple flingball boards
    //      - connectedBoards : the boards that are currently playing and connected to a server
    //      - joinedBoards : the boards that are currently joined to any of the four walls of this board
    //      - WALL_TO_TARGET_WALL : the mapping of a relation between walls and their corresponding targets
    //                              that they would teleport balls if another board is joined via those walls
    //      - LINE_SEGMENT_TO_WALL : the corresponding wall to a line segment that makes up the borders of this board
    //      - triggerAbsorberMap : represents the gadgets that trigger the absorbers
    //      - targetFlipperMap : represents the gadgets that trigger flippers
    //      - absorberBallNamesMap : represents the balls that are contained within absorbers on this board
    //      - protoListeners : represents the listeners of the board that listen for key input to generate an action
    //      - portalConnected : represents whether the portals on the board are connected to another portal or not
    //      - COLOR : represents the background color of the board
    //      - TIME : represents a time that emulates frame rate of a fling ball game
    //      - L : represents one unit on the fling board that is generally 20L x 20L
    //      - PIXELS_PER_L : represents the number of pixels per unit (L) on a flingball board
    // Representation Invariant:
    //  --| the list of connectedBoards has no duplicate boards in it
    //  --| Portals in localPortals cannot connect to boards aside from this one
    //  --| all local portals are connected
    //  --| friction1 & friction2 are >= 0
    //  --| if socket is present then this board is in connectedBoards
    //  --| all absorbers, flippers, and their triggers in the trigger maps are on the board
    // Safety from Representation Exposure:
    //  --| All fields are private
    //  --| All getter methods that return mutable objects implement defensive copying
    //  -----| Other return types are immutable and thus safe for returning
    // Thread Safety:
    //  --| All shared data is immutable and thus safe for sharing
    //  --| Every method synchronized following the monitor pattern except for:
    //  -----| socketInput() which needs to be constantly listening for input thus it wouldn't
    //         make sense to synchronize this method. SocketInput() must call synchronized getters
    //         and setters, thus, it will never be subject to race conditions. parseCommand() is the
    //         only method called within socketInput() that observes or mutates any part of the board
    //         and this method is synchronized.
    
    /**
     * Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private synchronized void checkRep() {
        // sanity checks :)
        assert name != null;
        assert balls != null;
        assert bumpers != null;
        assert absorbers != null;
        assert walls != null;
        
        assert friction1 >= 0;
        assert friction2 >= 0;
        assert this.connectedBoards.size() == new HashSet<>(this.connectedBoards).size();
        
        for(Portal portal : this.localPortals) {
            assert (!portal.getConnectedBoard().isPresent() || portal.getConnectedBoard().get().equals(this.name));
        }
        
        if (this.socket.isPresent()) {
            this.connectedBoards.contains(this.name);
        }
        
        for (Gadget gadget : this.triggerAbsorberMap.keySet()) {
            if (getAbsorbers().contains(gadget)) { continue; }
            if (getBumpers().contains(gadget)) {continue; }
            if (getFlippers().contains(gadget)) {continue; }
            if (getPortals().contains(gadget)) {continue; }
            assert false;
        }
        
        for (Gadget gadget : this.triggerFlipperMap.keySet()) {
            if (getAbsorbers().contains(gadget)) { continue; }
            if (getBumpers().contains(gadget)) {continue; }
            if (getFlippers().contains(gadget)) {continue; }
            if (getPortals().contains(gadget)) {continue; }
            assert false;
        }
        
    }
    
    /**
     * Construct a new empty Board
     * 
     * @param name the name of the Board
     */
    Board(String name) {
        this.name = name;
        this.balls = Collections.synchronizedList(new LinkedList<Ball>());
        this.bumpers = Collections.synchronizedList(new LinkedList<Bumper>());
        this.absorbers = Collections.synchronizedList(new LinkedList<Absorber>());
        this.flippers = Collections.synchronizedList(new LinkedList<Flipper>());
        this.portals = Collections.synchronizedList(new LinkedList<Portal>());
        this.localPortals = Collections.synchronizedList(new LinkedList<Portal>());
        this.walls = Collections.synchronizedList(constructWalls());
        populateJoinedBoardsMap();
        this.gravity = GRAVITY_DEFAULT;
        this.friction1 = FRICTION_DEFAULT;
        this.friction2 = FRICTION_DEFAULT;
        checkRep();
    }

    /**
     * Construct a new empty board of Flingball
     * 
     * @param name String name of the board
     * @param gravity float representing L/(seconds^2) that influences Ball movement
     * @param friction1 float friction to scale collisions by (mu1 per second)
     *                  that influences Ball movement
     * @param friction2 float friction to scale collisions by (mu2 per L)
     *                  that influences Ball movement
     */
    Board(String name, float gravity, float friction1, float friction2) {
        this.name = name;
        this.balls = new LinkedList<Ball>();
        this.bumpers = new LinkedList<Bumper>();
        this.absorbers = new LinkedList<Absorber>();
        this.flippers = new LinkedList<Flipper>();
        this.portals = new LinkedList<Portal>();
        this.localPortals = new LinkedList<Portal>();
        this.walls = constructWalls();
        populateJoinedBoardsMap();
        this.gravity = gravity;
        this.friction1 = friction1;
        this.friction2 = friction2;
        
        checkRep();
    }

    /**
     * Construct a new Board with given list of Balls, Bumpers and Absorbers
     * 
     * @param name the name of the board
     * @param balls given list of Balls to add to the Board
     * @param bumpers given list of Bumpers to add to the Board
     * @param absorbers given list of Absorbers to add to the Board 
     */
    Board(String name, List<Ball> balls, List<Bumper> bumpers, List<Absorber> absorbers) {
        this.name = name;
        this.balls = Collections.synchronizedList(new LinkedList<Ball>(balls));
        this.bumpers = Collections.synchronizedList(new LinkedList<Bumper>(bumpers));
        this.absorbers = Collections.synchronizedList(new LinkedList<Absorber>(absorbers));
        for (Absorber absorber:absorbers) { absorberBallNamesMap.put(absorber, Collections.synchronizedList(new ArrayList<>())); }
        this.flippers = Collections.synchronizedList(new LinkedList<Flipper>());
        this.portals = Collections.synchronizedList(new LinkedList<Portal>());
        this.localPortals = Collections.synchronizedList(new LinkedList<Portal>());
        this.walls = Collections.synchronizedList(constructWalls());
        populateJoinedBoardsMap();
        this.gravity = GRAVITY_DEFAULT;
        this.friction1 = FRICTION_DEFAULT;
        this.friction2 = FRICTION_DEFAULT;
        
        checkRep();
    }

    /**
     * Construct a new Board with given list of Balls, Bumpers, Absorbers, Flippers, and Portals
     * 
     * @param name the name of the board
     * @param balls given list of Balls to add to the Board
     * @param bumpers given list of Bumpers to add to the Board
     * @param absorbers given list of Absorbers to add to the Board
     * @param flippers given list of Flippers to add to the board
     * @param portals given list of Portals to add to the board
     * @param localPortals given list of local Portals to add to the baord
     * @param gravity float representing L/(seconds^2) that influences Ball movement
     * @param friction1 float friction to scale collisions by (mu1 per second) 
     *                  that influences Ball movement
     * @param friction2 float friction to scale collisions by (mu2 per L)
     *                  that influences Ball movement
     */
    Board(String name, List<Ball> balls, List<Bumper> bumpers, List<Absorber> absorbers,
            List<Flipper> flippers, List<Portal> portals, List<Portal> localPortals, 
            float gravity, float friction1, float friction2) {
        this.name = name;
        this.balls = Collections.synchronizedList(new LinkedList<Ball>(balls));
        this.bumpers = Collections.synchronizedList(new LinkedList<Bumper>(bumpers));
        this.flippers = Collections.synchronizedList(new LinkedList<Flipper>(flippers));
        this.portals = Collections.synchronizedList(new LinkedList<Portal>(portals));
        this.localPortals = Collections.synchronizedList(new LinkedList<Portal>(localPortals));
        this.absorbers = Collections.synchronizedList(new LinkedList<Absorber>(absorbers)); 
        for(Absorber absorber:absorbers ) { absorberBallNamesMap.put(absorber, Collections.synchronizedList(new ArrayList<>())); }
        this.walls = Collections.synchronizedList(constructWalls());
        populateJoinedBoardsMap();
        this.gravity = gravity;
        this.friction1 = friction1;
        this.friction2 = friction2;
        
        checkRep();
    }
    
    /**
     * Construct the four walls of the board. The dimensions are 20L x 20L.
     * The order walls are created is TOP -> BOTTOM -> LEFT -> RIGHT
     * 
     * @return A list containing four linesegments corresponding to the four walls of the board
     */
    private synchronized static List<LineSegment> constructWalls() {
        final List<LineSegment> boardWalls = new LinkedList<>();
        final LineSegment top = new LineSegment(new Vect(0, 0), new Vect(L, 0));
        final LineSegment right = new LineSegment(new Vect(L, 0), new Vect(L, L));
        final LineSegment left = new LineSegment(new Vect(0, 0), new Vect(0, L));
        final LineSegment bottom = new LineSegment(new Vect(0, L), new Vect(L, L));
        // Add general board walls
        boardWalls.add(top);
        boardWalls.add(right);
        boardWalls.add(left);
        boardWalls.add(bottom);
        // Add mappings of line segments to their enumeration, wall counterparts
        LINE_SEGMENT_TO_WALL.put(top, Wall.TOP);
        LINE_SEGMENT_TO_WALL.put(bottom, Wall.BOTTOM);
        LINE_SEGMENT_TO_WALL.put(left, Wall.LEFT);
        LINE_SEGMENT_TO_WALL.put(right, Wall.RIGHT);
        // Add mappings of line segments to their predetermined connection walls 
        WALL_TO_TARGET_WALL.put(Wall.TOP, Wall.BOTTOM);
        WALL_TO_TARGET_WALL.put(Wall.BOTTOM, Wall.TOP);
        WALL_TO_TARGET_WALL.put(Wall.LEFT, Wall.RIGHT);
        WALL_TO_TARGET_WALL.put(Wall.RIGHT, Wall.LEFT);
        return boardWalls;
    }
    
    /**
     * Populates the joined boards map with each of this board's possible walls.
     * Each wall is mapped to an empty optional since at instantiation, no boards
     * are connected to this board. 
     */
    private synchronized void populateJoinedBoardsMap() {
        joinedBoards.put(Wall.LEFT, Optional.empty());
        joinedBoards.put(Wall.RIGHT, Optional.empty());
        joinedBoards.put(Wall.TOP, Optional.empty());
        joinedBoards.put(Wall.BOTTOM, Optional.empty());
        checkRep();
    }
    
    /**
     * Takes in a socket connected to a server which allows for communication between
     * the board and the server.
     * 
     * @param s the socket that board will send messages through. The socket must be
     *          connected to a FlingballTextServer.
     */
    public synchronized void acceptSocket(Socket s) {
        this.socket = Optional.of(s);
        new Thread(new Runnable() {
            public void run() {
                socketInput();
            }
        }).start();
        checkRep();
    }
    
    /**
     * Gets input from a socket that is connected to a FlingballTextServer
     */
    public void socketInput() {
        assert this.socket.isPresent();
        
        try {
            final InputStream inputStream = this.socket.get().getInputStream();
            final InputStreamReader iStreamReader = new InputStreamReader(inputStream);
            final BufferedReader buffReader = new BufferedReader(iStreamReader);
            while (true) { 
                final String line = buffReader.readLine();
                if (line != null) {
                    parseCommand(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkRep();
        
    }
     
    /**
     * Parses a command string and handles the corresponding response. The command
     * string must follow the grammar which is displayed in WireProtocol.g. After
     * parsing, the corresponding action to a command is executed.
     * 
     * @param command the text to parse and interpret as a command
     */
    private synchronized void parseCommand(String command) {
        if (command.equals("getClientBoardName")) {
            socketOutput(this.name);
            checkRep();
            return;
        }
        
        if (command.equals("disconnect")) {
            this.socket = Optional.empty();
        }
        
        final String[] commandSplit = command.split("[ ]+");
        if (commandSplit.length == 1 || commandSplit[0].equals("failure")) {
            System.out.println("There was a problem communicating with the server.");

        } else if (commandSplit[1].equals("joinHorizontal=")) {
            final int leftBoardNameIndex = 2;
            final int rightBoardNameIndex = 3;
            final String leftBoardName = commandSplit[leftBoardNameIndex];
            final String rightBoardName = commandSplit[rightBoardNameIndex];
            if (this.name.equals(leftBoardName))
                joinedBoards.put(Wall.LEFT, Optional.of(rightBoardName));
            if (this.name.equals(rightBoardName))
                joinedBoards.put(Wall.RIGHT, Optional.of(leftBoardName));

        } else if (commandSplit[1].equals("joinVertical=")) {
            final int topBoardNameIndex = 2;
            final int bottomBoardNameIndex = 3;
            final String topBoardName = commandSplit[topBoardNameIndex];
            final String bottomBoardName = commandSplit[bottomBoardNameIndex];
            if (this.name.equals(topBoardName)) {
                joinedBoards.put(Wall.TOP, Optional.of(bottomBoardName));
            }
            if (this.name.equals(bottomBoardName)) {
                joinedBoards.put(Wall.BOTTOM, Optional.of(topBoardName));
            }
        } else if (commandSplit[1].equals("disconnectWall="))  {
            final int disconnectBoardIndex = 2;
            final int disconnectWallIndex = 3;
            final String disconnectBoard = commandSplit[disconnectBoardIndex];
            final Wall disconnectWall = Wall.valueOf(commandSplit[disconnectWallIndex].toUpperCase());
            final Wall wallToDisconnect = Board.WALL_TO_TARGET_WALL.get(disconnectWall);
            if (this.joinedBoards.get(wallToDisconnect).isPresent() && 
                    this.joinedBoards.get(wallToDisconnect).get().equals(disconnectBoard)) {
                this.joinedBoards.put(wallToDisconnect, Optional.empty());
            }  
            
        } else if (commandSplit[1].equals("teleportPortal=")) {
            final int ballNameIndex = 3;
            final int velocityXIndex = 4;
            final int velocityYIndex = 5;
            final int portalNameIndex = 6;
            
            final Portal portal = getPortalByName(commandSplit[portalNameIndex]);
            final Vect portalLocation = portal.getLocation();
            final Vect ballVelocity = new Vect(Double.parseDouble(commandSplit[velocityXIndex]), 
                                         Double.parseDouble(commandSplit[velocityYIndex]));
            final Ball teleportedBall = new Ball(commandSplit[ballNameIndex], portalLocation, ballVelocity);
            launchBallFromPortal(teleportedBall, portal);
              
        } else if (commandSplit[1].equals("teleportWall=")) {
            final int ballNameIndex = 3;
            final int velocityXIndex = 4;
            final int velocityYIndex = 5;
            final int wallLocationXIndex = 6;
            final int wallLocationYIndex = 7;
            final int wallNameIndex = 8;
            
            Vect ballLocationOnWall = new Vect(Double.parseDouble(commandSplit[wallLocationXIndex]), 
                                               Double.parseDouble(commandSplit[wallLocationYIndex]));
            final Wall wall = Wall.valueOf(commandSplit[wallNameIndex].toUpperCase());
            final Vect ballVelocity = new Vect(Double.parseDouble(commandSplit[velocityXIndex]), 
                                         Double.parseDouble(commandSplit[velocityYIndex]));
            switch (wall) {
                case LEFT: 
                {
                   ballLocationOnWall = new Vect(Ball.RADIUS/2, ballLocationOnWall.y());
                   break; 
                }
                case RIGHT:
                {
                    ballLocationOnWall = new Vect(L - Ball.RADIUS/2, ballLocationOnWall.y());
                    break;
                }
                case TOP:
                {
                    ballLocationOnWall = new Vect(ballLocationOnWall.x(), Ball.RADIUS/2);
                    break;
                }
                case BOTTOM:
                {
                    ballLocationOnWall = new Vect(ballLocationOnWall.x(), L - Ball.RADIUS/2);
                    break;
                }
                default:
                    throw new IllegalArgumentException("This was not a valid wall as in Wall ENUM.");
            }
            final Ball teleportedBall = new Ball(commandSplit[ballNameIndex], ballLocationOnWall, ballVelocity);
            launchBallFromWall(teleportedBall);
            
        } else if (commandSplit[1].equals("connectPortal=")) {
            final int connectedPortalIndex = 2;
            this.portalConnected.put(commandSplit[connectedPortalIndex], true);
            
        } else if (commandSplit[1].equals("disconnectPortal=")) {
            final int connectedPortalIndex = 2;
            this.portalConnected.put(commandSplit[connectedPortalIndex], false);
            
        } else if (commandSplit[1].equals("disconnectWall=")) {
            final int boardNameIndex = 2;
            final String boardName = commandSplit[boardNameIndex];
            for (Wall wall : this.joinedBoards.keySet()) {
                if (joinedBoards.get(wall).isPresent() && joinedBoards.get(wall).get().equals(boardName))
                    joinedBoards.put(wall, Optional.empty());
            }
            
        } else if (commandSplit[1].equals("allConnectedBoards=")) {
            final List<String> boards = Arrays.asList(commandSplit);
            this.connectedBoards = boards.subList(2, boards.size());
            for (Wall wall : this.joinedBoards.keySet()) {
                if (this.joinedBoards.get(wall).isPresent() && !this.connectedBoards.contains(this.joinedBoards.get(wall).get())) {
                    this.joinedBoards.put(wall, Optional.empty());
                }
            }
        }
        checkRep();
    }
    
    /**
     * Gets a portal by its name
     * 
     * @param portalName the name of a portal
     * @return the Portal whose name is portalName
     */
    private synchronized Portal getPortalByName(String portalName) {
        return portals.stream().filter(x -> x.getName().equals(portalName)).collect(Collectors.toList()).get(0);
    }
    
    /**
     * Sends a message to FlingballTextServer
     * 
     * @param message the text to send to the server to initiate an action by the server
     *                such as teleporting a ball.
     */
    private synchronized void socketOutput(String message) {
        try {
            final PrintWriter out = new PrintWriter(socket.get().getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }     
    }

    /**
     * Sets the board's gravity constant to a new value
     * 
     * @param gravity the gravity constant of the board
     */
    public synchronized void setGravity(float gravity) {
        this.gravity = gravity;
        checkRep();
    }
    
    /**
     * Sets the board's first friction constant to a new value
     * 
     * @param f1 the first friction constant of the board
     */
    public synchronized void setFriction1(float f1) {
        this.friction1 = f1;
        checkRep();
    }
    
    /**
     * Sets the board's second friction constant to a new value
     * 
     * @param f2 the second friciton constant of the board
     */
    public synchronized void setFriction2(float f2) {
        this.friction2 = f2;
        checkRep();
    }
    
    /**
     * Sets the board's name to a new value
     * 
     * @param name the new name of the board
     */
    public synchronized void setName(String name) {
        this.name = name;
        checkRep();
    }
    
    /**
     * Adds a portal that connects to another portal on the this board to this board
     * 
     * @param portal the portal that is locally connected to another portal
     */
    public synchronized void addLocalPortal(Portal portal) {
        localPortals.add(portal);
        checkRep();
    }
    
    /**
     * Add new Balls to this board
     * 
     * @param newBalls given list of balls to add to the Board
     */
    protected synchronized void addBall(List<Ball> newBalls) {
        balls.addAll(newBalls);
        checkRep();
    }

    /**
     * Add a new Ball to this board
     * 
     * @param newBall the singular new ball to add to the Board
     */
    protected synchronized void addBall(Ball newBall) {
        balls.add(newBall);
        checkRep();
    }
    

    /**
     * Add new Bumpers to this board
     * 
     * @param newBumpers given list of Bumpers to add to the Board
     */
    protected synchronized void addBumper(List<Bumper> newBumpers) {
        bumpers.addAll(newBumpers);
        checkRep();
    }

    /**
     * Add a new Bumper to this board
     * 
     * @param newBumper the singular bumper to add to the Board
     */
    protected synchronized void addBumper(Bumper newBumper) {
        bumpers.add(newBumper);
        checkRep();
    }

    /**
     * Add new Absorbers to this board
     * 
     * @param newAbsorbors given list of Absorbers to add the the Board
     */
    protected synchronized void addAbsorber(List<Absorber> newAbsorbors) {
        absorbers.addAll(newAbsorbors);
        for(Absorber absorber:newAbsorbors)
            absorberBallNamesMap.put(absorber,new ArrayList<>());
        checkRep();
    }

    /**
     * Add a new Absorber to this board
     * 
     * @param newAbsorbor the singular Absorber to add to the Board
     */
    protected synchronized void addAbsorber(Absorber newAbsorbor) {
        absorbers.add(newAbsorbor);
        absorberBallNamesMap.put(newAbsorbor,new ArrayList<>());
        checkRep();
    }
    
    /**
     * Add a new flipper gadget to the flingball board
     * 
     * @param newFlipper the singular flipper gadget to add to the flingball board
     */
    public synchronized void addFlipper(Flipper newFlipper) {
        flippers.add(newFlipper);
        checkRep();
    }
    
    /**
     * Add new Flippers to this board
     * 
     * @param newFlippers given list of Flippers to add the the Board
     */

    public synchronized void addFlipper(List<Flipper> newFlippers) {
        for (Flipper flipper : newFlippers) {
            flippers.add(flipper);
        }
        checkRep();
    }

    /**
     * Add a new Portal gadget to the flingball board
     * 
     * @param newPortal the singular portal gadget to add the the flingball board
     */
    public synchronized void addPortal(Portal newPortal) {
        portals.add(newPortal);
        checkRep();
    }
    
    /**
     * Add new Portals to this board
     * 
     * @param newPortals given list of Portals to add the the Board
     */
    public synchronized void addPortal(List<Portal> newPortals) {
        for (Portal portal : newPortals) {
            portals.add(portal);
        }
        checkRep();
    }

    /**
     * @return the list of Balls that are currently on the Board
     */
    protected synchronized List<Ball> getBalls() {
        final List<Ball> ballsCopy = new LinkedList<Ball>();
        for (Ball ball : balls)
            ballsCopy.add(ball);
        return ballsCopy;
    }

    /**
     * @return the list of Bumpers that are currently on the Board
     */
    protected synchronized List<Bumper> getBumpers() {
        final List<Bumper> bumpersCopy = new LinkedList<Bumper>();
        for (Bumper bumper : bumpers)
            bumpersCopy.add(bumper);
        return bumpersCopy;
    }
    
    /**
     * @return a mapping of the walls that a board contains to the optional board
     *         that they correspond to and would send balls to if struck by a ball
     */
    protected synchronized Map<Wall, Optional<String>> getJoinBoards(){
        return new HashMap<>(this.joinedBoards);
    }

    /**
     * @return the list of Absorbers that are currently on the Board
     */
    protected synchronized List<Absorber> getAbsorbers() {
        final List<Absorber> absorberCopy = new LinkedList<Absorber>();
        for (Absorber absorber : absorbers)
            absorberCopy.add(absorber);
        return absorberCopy;
    }
    
    /**
     * @return the list of Flippers that are currently on the Board
     */
    protected synchronized List<Flipper> getFlippers() {
        final List<Flipper> flipperCopy = new LinkedList<Flipper>();
        for (Flipper flipper : flippers)
            flipperCopy.add(flipper);
        return flipperCopy;
    }
    
    /**
     * @return the list of Portals that are currently on the Board
     */
    protected synchronized List<Portal> getPortals() {
        final List<Portal> portalCopy = new LinkedList<Portal>();
        for (Portal portal : portals)
            portalCopy.add(portal);
        return portalCopy;
    }

    /**
     * @return the list of all static Gadgets (Bumpers and Absorbers)
     *         that are currently on the Board
     */
    protected synchronized List<Gadget> getStaticGadgets() {
        final List<Gadget> staticGadgets = new LinkedList<>();
        for (Bumper bumper : bumpers)
            staticGadgets.add(bumper);
        for (Absorber absorber : absorbers)
            staticGadgets.add(absorber);
        for (Portal portal : portals) 
            staticGadgets.add(portal);
        return staticGadgets;
    }
    
    /**
     * Gets a specific gadget given only its name
     * 
     * @param gadgetName the name of the gadget, must be in the board
     * @return the gadget whose name is gadgetName
     * @throws IllegalArgumentException if no gadget with name name is in this board
     */
    protected synchronized Gadget getGadgetByName(String gadgetName) throws IllegalArgumentException {
        for (Gadget gadget : getStaticGadgets()) {
            if (gadget.getName().equals(gadgetName)) {
                return gadget;
            }
        }
        for (Flipper flipper : flippers) {
            if (flipper.getName().equals(gadgetName)) {
                return flipper;
            }
        }
        throw new IllegalArgumentException("No gadget with that name in the board");
    }

    /**
     * @return the Gravity of the Board
     */
    protected synchronized float getGravity() {
        return gravity;
    }

    /**
     * @return the Friction (mu1 per second) of the Board
     */
    protected synchronized float getFriction1() {
        return friction1;
    }

    /**
     * @return the Friction (mu2 per L) of the Board
     */
    protected synchronized float getFriction2() {
        return friction2;
    }

    /**
     * @return the name of the Board
     */
    protected synchronized String getName() {
        return name;
    }
    
    /**
     * @return a mapping of the name of an absorber to a list of balls (by name) that are contained
     *         in the absorber. If the absorber contains no balls, then the name of that
     *         absorber is mapped to an empty list.
     */
    public synchronized Map<String, List<String>> getAbsorberNameToBallMap() {
        final Map<String, List<String>>absorberNameMap = new HashMap<>();
        for (Absorber abs : absorberBallNamesMap.keySet()) {
            absorberNameMap.put(abs.getName(), new ArrayList<>());
            for (String ballName : absorberBallNamesMap.get(abs)) {
                absorberNameMap.get(abs.getName()).add(ballName);
            }
        }
        return absorberNameMap;
    }

    /**
    * Returns the Absorbers that will perform an action when the trigger 
    * Gadget is triggered (hit by a Ball)
    * 
    * @param trigger Gadget to find the corresponding Target Absorbers of
    * @return List of Absorbers that respond when the  corresponding trigger Gadget is hit. 
    *         If trigger has no target Absorbers, returns an empty List.
    */
    protected synchronized List<Absorber> getAbsorberTarget(Gadget trigger) {
        if (!triggerAbsorberMap.containsKey(trigger)) {  checkRep(); return new ArrayList<Absorber>();}
        return triggerAbsorberMap.get(trigger);
    }
    
    /**
     * Returns the Flippers that will perform an action when the trigger 
     * Gadget is triggered (hit by a Ball)
     * 
     * @param trigger Gadget to find the corresponding Target Absorbers of
     * @return List of Flippers that respond when the  corresponding trigger Gadget is hit. 
     *         If trigger has no target Flipper, returns an empty List.
     */
     protected synchronized List<Flipper> getFlipperTarget(Gadget trigger) {
         if (!triggerFlipperMap.containsKey(trigger)) {  checkRep(); return new ArrayList<Flipper>();}
         return triggerFlipperMap.get(trigger);
     }
    
    /**
    * Sets the relationship of triggers and actions between gadgets. When a gadget
    * is triggered, an action will occur in all of its target Absorbers
    * 
    * @param target Absorber that will performs an action when trigger is triggered
    * @param trigger Gadget that when triggered causes a response in target
    * @return boolean representing if the trigger-target pair were added.
    */
    protected synchronized boolean setTarget(Absorber target, Gadget trigger) {
        if (triggerAbsorberMap.containsKey(trigger) && triggerAbsorberMap.get(trigger).contains(target)) {
            checkRep();
            return false;
        }
        if (!triggerAbsorberMap.containsKey(trigger)) {
            triggerAbsorberMap.put(trigger, new ArrayList<>(Arrays.asList(target)));
            checkRep();
            return true;
        }
        triggerAbsorberMap.get(trigger).add(target);
        checkRep();
        return true;
    }
    
    /**
     * Sets the relationship of triggers and actions between gadgets. When a gadget
     * is triggered, an action will occur in all of its target Flippers
     * 
     * @param target Flipper that will performs an action when trigger is triggered
     * @param trigger Gadget that when triggered causes a response in target
     * @return boolean representing if the trigger-target pair were added.
     */
     protected synchronized boolean setTarget(Flipper target, Gadget trigger) {
         if (triggerFlipperMap.containsKey(trigger) && triggerFlipperMap.get(trigger).contains(target)) {
             checkRep();
             return false;
         }
         if (!triggerFlipperMap.containsKey(trigger)) {
             triggerFlipperMap.put(trigger, new ArrayList<>(Arrays.asList(target)));
             checkRep();
             return true;
         }
         triggerFlipperMap.get(trigger).add(target);
         checkRep();
         return true;
     }
    
    /**
     * Moves balls forward according to a timestep (not taking into account collisions).
     * Additionally, this functions handles the movement of flippers from one position to the next.
     * 
     * @param time the time step that helps govern the velocity of the balls on the board.
     *             requires: no collisions can occur in time < time
     */
    private synchronized void stepBoard(double time) {
        final List<Flipper> toFlip = new ArrayList<>();
        for (Flipper flipper : this.flippers) {
            if (flipper.isFlipping()) {
                toFlip.add(flipper);
            }
        }
        
        for (Flipper flipper : toFlip) {
            handleFlipping(flipper, time);
        }
       final List<Ball> newBallList = new ArrayList<>();
       for (Ball ball: this.balls) {
           final Ball newBall = new Ball(ball.getName(), ball.getLocation()
                   .plus(new Vect(ball.getVelocity().angle(), ball.getVelocity().length()*time)), 
                   ball.getVelocity());
           newBallList.add(newBall);
       }
       this.balls.clear();
       this.balls.addAll(newBallList);
       checkRep();
    }
    
    /**
     * Handles the collisions and subsequent reflections of two balls 
     * 
     * @param ball1 the first ball that collides with the second
     * @param ball2 the second ball that collides with the first
     */
    private synchronized void resolveCollisionBall(Ball ball1, Ball ball2) {
        final Physics.VectPair velocities = Physics.reflectBalls
                (ball1.getLocation(), 1, ball1.getVelocity(), ball2.getLocation(), 1, ball2.getVelocity());
        final Ball newBall1 = new Ball(ball1.getName(), ball1.getLocation(), velocities.v1);
        final Ball newBall2 = new Ball(ball2.getName(), ball2.getLocation(), velocities.v2);
        this.balls.remove(ball1);
        this.balls.remove(ball2);
        this.balls.add(newBall1);
        this.balls.add(newBall2);
        checkRep();   
    }
    
    /**
     * Handles the colliding of a ball with a bumper. The ball will
     * reflect off the bumper at certain angle and with a certain velocity
     * determined by the bumper reflection coefficient.
     * 
     * @param ball the ball that is colliding with bumper
     * @param bumper the bumper that is receiving the ball hitting it
     */
    private synchronized  void resolveCollisionBumper(Ball ball, Bumper bumper) {
       this.balls.remove(ball);
       final Ball reflectedBall = bumper.getCollisionRedirection(ball);
       this.balls.add(reflectedBall);
       checkRep();   
    }
    
    /**
     * Handles the collision of a ball with an absorber, i.e. the ball gets absorbed
     * and subsequently removed from the board
     * 
     * @param ball the ball that is colliding with the absorber
     * @param absorber the absorber that the ball will collide with. This absorber
     *                 will absorb the ball that hits it.
     */
    private synchronized void resolveCollisionAbsorber(Ball ball, Absorber absorber) {
        this.balls.remove(ball);
        this.absorberBallNamesMap.get(absorber).add(ball.getName());
        checkRep();   
    }
    
    /**
     * Handles the collision of a ball with a portal, i.e. the ball gets teleported
     * and subsequently removed from the board. If the portal the ball collided with
     * has an invalid connection or isn't connected to anything, the ball will just pass
     * over the portal.
     * 
     * @param ball the ball that is colliding with the portal
     * @param portal the portal that the ball will collide with. This portal
     *               will teleport the ball that hits it if correctly connected.
     */
    private synchronized void resolveCollisionPortal(Ball ball, Portal portal) {
        this.balls.remove(ball);
        if (localPortals.contains(portal)) {
            launchBallFromPortal(ball, portal);
        } else {
            final StringBuilder request = new StringBuilder();
            request.append("teleportPortal= ");
            if (portal.getConnectedBoard().isPresent())
                request.append(portal.getConnectedBoard().get() + " ");
            else
                request.append(this.getName() + " ");
            request.append(ball.getName() + " ");
            request.append(ball.getVelocity().x() + " " + ball.getVelocity().y() + " ");
            request.append(portal.getConnectedPortal());
            socketOutput(request.toString());
        }
        checkRep();
    }

    /**
     * Handles the collision of a ball with a wall. The ball will
     * reflect off the bumper at certain angle and with a certain velocity
     * determined by the bumper reflection coefficient.
     * 
     * @param ball the ball that is colliding with the wall
     * @param line the LineSegment that is a board wall that a ball is about to hit
     */
    private synchronized void resolveCollisionWall(Ball ball, LineSegment line) { 
        this.balls.remove(ball);
        final Wall wallHit = LINE_SEGMENT_TO_WALL.get(line);
        if (this.joinedBoards.get(wallHit).isPresent()) {
            final StringBuilder request = new StringBuilder();
            request.append("teleportWall= ");
            request.append(this.joinedBoards.get(wallHit).get() + " ");
            request.append(ball.getName() + " ");
            request.append(ball.getVelocity().x() + " " + ball.getVelocity().y() + " ");
            request.append(ball.getLocation().x() + " " + ball.getLocation().y() + " ");
            request.append(WALL_TO_TARGET_WALL.get(wallHit));
            socketOutput(request.toString());
        } else {
            this.balls.add(new Ball(ball.getName(), ball.getLocation(), 
                    Physics.reflectWall(line, ball.getVelocity()))); 
        }
        checkRep();
    }
    
    /**
     * Calculates the time until a collision will happen on the board
     * 
     * @param givenTime the "foresight" time, i.e. the amount of time which
     *                  the function looks forward in order to detect collisions
     * @return the time until a collision is going to happen
     */
    private synchronized double timeTillNextCollision(double givenTime) {
        double minTimeTillCollision = Double.POSITIVE_INFINITY;
        for(Ball ball : this.balls) {
            for (Ball ball2 : this.balls) {
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        ball2.getTimeTillCollision(ball, givenTime));
            }
            for (Bumper bumper: this.bumpers) {
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        bumper.getTimeTillCollision(ball, givenTime));
            }
            for (Absorber absorber: this.absorbers) {
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        absorber.getTimeTillCollision(ball, givenTime));
            }
            for (LineSegment line : this.walls) {
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        Physics.timeUntilWallCollision(line, ball.getCircle(), ball.getVelocity()));
            }
            for (Flipper flipper : this.flippers) {
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        flipper.getTimeTillCollision(ball, givenTime));
            }
            for (Portal portal : this.portals) {
                if (!(portal.getConnectedBoard().isPresent() && 
                        connectedBoards.contains(portal.getConnectedBoard().get())) && 
                        !(this.localPortals.contains(portal)) && !(portal.isContained(ball))){
                    continue;
                }
                minTimeTillCollision = Math.min(minTimeTillCollision, 
                        portal.getTimeTillCollision(ball, givenTime));  
            }
       
        }
        checkRep();
        return minTimeTillCollision;     
    }
    
    /**
     * Applies friction and gravity to all balls on the board between two
     * given time steps or board updates.
     * 
     * @param delta the elapsed time between board updates
     */
    public synchronized void applyFrictionGravity(double delta) {
        final List<Ball> newBalls = new ArrayList<>();
        for(Ball ball: this.balls) {
            newBalls.add(ball.updateVelocity(delta, this.gravity, this.friction1, this.friction2));
        }
        this.balls.clear();
        this.balls.addAll(newBalls);
        checkRep();
    }
    
    /**
     * Updates this board to the next frame
     * 
     * @param givenTime the "foresight" time, i.e. the amount of time which
     *                  the function looks forward in order to detect collisions
     */
    protected synchronized void updateBoard(double givenTime) {
        double timeTillNextCollision;
        mainLoop:
        while (givenTime >= EPSILON_14) {
            timeTillNextCollision = timeTillNextCollision(givenTime);
            if (givenTime < EPSILON_14) {
                givenTime = 0;
                break mainLoop;
            }
            if (timeTillNextCollision >= givenTime) {
                this.stepBoard(givenTime);
                givenTime = 0;
                break mainLoop;
               
            }
            this.stepBoard(timeTillNextCollision);
           
            /*To make sure you don't change the list while iterating over it */
            for (Ball ball: new ArrayList<>(this.balls) ) {
                for (Ball ball1: this.balls) {
                    if(ball1.getTimeTillCollision(ball, givenTime) <= EPSILON_14) {
                        resolveCollisionBall(ball, ball1);   
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }
                  
                for (Bumper bumper : this.bumpers) {
                    if(bumper.getTimeTillCollision(ball, givenTime) <= EPSILON_14) {
                        resolveCollisionBumper(ball, bumper);
                        updateActionedAbsorbers(bumper);
                        updateActionedFlippers(bumper, givenTime);
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }

                for (LineSegment line : this.walls) {
                    if (Physics.timeUntilWallCollision(line, ball.getCircle(), ball.getVelocity()) <= EPSILON_14){
                        resolveCollisionWall(ball, line);
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }

                
                for (Absorber absorber: this.absorbers) {
                    if (absorber.getTimeTillCollision(ball, givenTime) <= EPSILON_14) {
                        if (absorber.isContained(ball)) {
                            continue mainLoop;
                        }
                        resolveCollisionAbsorber(ball, absorber);
                        updateActionedAbsorbers(absorber);
                        updateActionedFlippers(absorber, givenTime);
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }
           
                /* Needs to be checked last because minTimeTillCollision will return 0 even when there
                 * is no collision because minTimeTillCollision doesn't take into account that the
                 * portal isn't connected. */
                for (Portal portal: this.portals) {
                    if (!(portal.getConnectedBoard().isPresent() && 
                            connectedBoards.contains(portal.getConnectedBoard().get())) && 
                            !(this.localPortals.contains(portal)) && !(portal.isContained(ball))){
                        continue;
                    }
                    if (portal.getTimeTillCollision(ball, givenTime) <= EPSILON_14) {
                        if (portal.isContained(ball)) {
                            continue mainLoop;
                        }
                        if (this.localPortals.contains(portal)) {
                            resolveCollisionPortal(ball, portal); 
                        }
                        else if ((portal.getConnectedBoard().isPresent() && 
                                connectedBoards.contains(portal.getConnectedBoard().get()))){
                            resolveCollisionPortal(ball, portal);
                        }
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }
                
                for (Flipper flipper : this.flippers) {
                    if(flipper.getTimeTillCollision(ball, givenTime) <= EPSILON_14) {
                        resolveCollisionBumper(ball, flipper);
                        updateActionedAbsorbers(flipper);
                        updateActionedFlippers(flipper, givenTime);
                        givenTime = givenTime - timeTillNextCollision;
                        continue mainLoop;
                    }
                }
                
            }
        } 
        checkRep();
    }

    /**
     * For the given Gadgets, updates all of their target Absorbers (if any).
     * Adds the balls shot by the Absorbers to newBalls. Creates new Absorbers 
     * with 1 less balls and add them to Absorbers List. 
     *
     * @param gadget the gadget which may or may not trigger absorbers to perform an action 
     */
    private synchronized void updateActionedAbsorbers(Gadget gadget) {
        final List<Absorber> targets = getAbsorberTarget(gadget);
        for(Absorber target: targets) {
            if(absorberBallNamesMap.get(target).size()>0) {
                final String ballName = absorberBallNamesMap.get(target).get(0);
                absorberBallNamesMap.get(target).remove(0);
                this.balls.add(target.action(ballName));
                
            }
        }
        checkRep();
    }
    
    /**
     * For the given Gadget, updates all of its target Flippers (if any).
     * Adds the new flipper returned by the Flipper to  the board and removes
     * the old one from the board.
     * 
     * @param gadget the gadget which may or may not trigger flippers to perform an action
     * @param time the time step to simulate passing
     */
    private synchronized void updateActionedFlippers(Gadget gadget, double time) {
        final List<Flipper> toFlip = new ArrayList<>();
        final List<Flipper> targets = getFlipperTarget(gadget);
        for(Flipper target: targets) {
            toFlip.add(target);
        }
        for (Flipper flipper : toFlip) {
            handleFlipping(flipper, time);
        }
        checkRep();
    }
    
    /**
     * Launches a ball from the board's specified portal. This method is used in
     * teleporting balls across portals through the server.
     *
     * @param ball the ball to send from the center of the portal. ball's velocity
     *             is preserved when fired from the portal
     * @param portal the portal to send the ball through on this board
     */
    private synchronized void launchBallFromPortal(Ball ball, Portal portal) {
        final Portal targetPortal = this.portals.stream()
                .filter(x -> x.getName().equals(
                        portal.getConnectedPortal())).collect(Collectors.toList()).get(0);
        this.balls.add(targetPortal.release(ball));
        checkRep();
    }
    
    /**
     * Launches a ball from the boards wall. This method is used in
     * teleporting balls across walls through the server. The method
     * also ensures that no balls will enter another board if an object
     * is blocking its place of entry. In that case, the ball just
     * disappears from game play. 
     * 
     * @param ball the ball to launch from a wall. The ball already
     *             contains the updated position location for it to
     *             fire correctly from the right spot, so no wall is
     *             needed as a parameter.
     */
    private synchronized void launchBallFromWall(Ball ball) {
        for (Ball ball2 : this.balls) 
            if (ball2.rejects(ball)) { return; }
        for (Bumper bumper: this.bumpers) 
            if (bumper.rejects(ball)) { return; }      
        for (Flipper flipper : this.flippers) 
            if (flipper.rejects(ball)) { return; }        
             
        for (Portal portal : this.portals) {
            if (portal.intersects(ball)) {
                /*teleport through portal*/
                launchBallFromPortal(ball, portal);
                checkRep();
                return;
            }
        }
        
        for (Absorber absorber: this.absorbers) {
            if (absorber.intersects(ball)) {
                /*Absorb the ball */
                this.absorberBallNamesMap.get(absorber).add(ball.getName());
                checkRep();
                return;
            }
        }
        checkRep();
        this.balls.add(ball);
    }
    
    /**
     * Flips a flipper
     * 
     * @param flipper a flipper to trigger the flipping action of
     * @param time the time step from update board dictating how
     *             much time to simulate passing between the creation
     *             of the new Flipper
     */
    private synchronized void handleFlipping(Flipper flipper, double time) {
        final Flipper newFlipper = flipper.flip(time);
        
        flippers.remove(flipper);
        flippers.add(newFlipper);
        
        for (Gadget g : triggerFlipperMap.keySet()) {
            if (triggerFlipperMap.get(g).contains(flipper)) {
                triggerFlipperMap.get(g).remove(flipper);
                triggerFlipperMap.get(g).add(newFlipper);
            }
        }
        
        checkRep();
    }
    
    /**
     * Launches a ball from an absorber given it contains one
     * 
     * @param absorber the absorber to trigger
     */
    private synchronized void launchBall(Absorber absorber) {
        if (absorberBallNamesMap.get(absorber).size() > 0) {
            final String ballName = absorberBallNamesMap.get(absorber).get(0);
            absorberBallNamesMap.get(absorber).remove(0);
            this.balls.add(absorber.action(ballName));
        }
        checkRep();
    }
    
    /**
     * Triggers a gadget's action given the gadget's name.
     * 
     * @param gadget the name of a gadget to trigger
     */
    public synchronized void triggerGadgetByName(String gadget) {
        final List<Absorber> absorbersList = absorbers.stream()
                .filter(x -> x.getName().equals(gadget)).collect(Collectors.toList());
        final List<Flipper> flippersList = flippers.stream()
                .filter(x -> x.getName().equals(gadget)).collect(Collectors.toList());
        if (!absorbersList.isEmpty()) {
            launchBall(absorbersList.get(0));
        } else if (!flippersList.isEmpty()) {
            handleFlipping(flippersList.get(0), EPSILON_16);
        }
        checkRep();
        
    }
    
    /**
     * Adds a key listener to the board. 
     * 
     * @param listenerString the key to press or release in order
     *                       to trigger an action on this board
     */
    public synchronized void addProtoListener(String listenerString) {
        this.protoListeners.add(listenerString);
        checkRep();
    }
    
    /**
     * @return A list of strings that are the keys that 
     *         trigger an action on this board
     */
    public synchronized List<String> getProtoListeners() {
        return new LinkedList<>(this.protoListeners);
    }
    
    /**
     * Draws a banner that is the name of the board that this board's wall
     * is connected to. 
     * 
     * @param graphics the graphics on which the board is drawn and which the joined boards'
     *                 names will be drawn on
     */
    public synchronized void drawJoinBanner(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        final int centeringOffset = 170;
        final int offset = 12;
        final int topYOffset = 13;
        final int bottomYOffset = 395;
        final int leftXOffset = 3;
        final int rightXOffset = 390;
        
        for (Wall wall : this.joinedBoards.keySet()) {
            if (!this.joinedBoards.get(wall).isPresent()) 
                continue;
            
            final String connectedBoardName = this.joinedBoards.get(wall).get();
            final char[] boardNameChars = connectedBoardName.toCharArray();
            int nextCharOffset = 0;
            for (char c : boardNameChars) {
                if (wall == Wall.TOP) {
                    graphics.drawChars(new char[]{c}, 0, 1, centeringOffset + nextCharOffset, topYOffset);
                } else if (wall == Wall.BOTTOM) {
                    graphics.drawChars(new char[]{c}, 0, 1, centeringOffset + nextCharOffset, bottomYOffset);
                } else if (wall == Wall.LEFT) {
                    graphics.drawChars(new char[]{c}, 0, 1, leftXOffset, centeringOffset + nextCharOffset);
                } else if (wall == Wall.RIGHT) {
                    graphics.drawChars(new char[]{c}, 0, 1, rightXOffset, centeringOffset + nextCharOffset);
                }
                nextCharOffset += offset;
            }
        }
        graphics.setColor(this.color);
        checkRep();
    }

    /**
     * Draws an image of all the parts of a Flingball game that do not
     * move. Since these are static, they do not need to be redrawn with
     * each frame, so they will be drawn once and set as a background.
     * 
     * @return the Board's background image where the background image is
     *         a BufferedImage with all Gadgets drawn and has a background
     *         color of white.
     */
    public synchronized BufferedImage drawBackground() {
        final int xDimensions = PIXELS_PER_L * L;
        final int yDimensions = PIXELS_PER_L * L;
        final BufferedImage background = 
                new BufferedImage(xDimensions, yDimensions, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = background.getGraphics();
        graphics.setColor(this.color);
        graphics.fillRect(0, 0, xDimensions, yDimensions);
        
        for (Gadget gadget : getStaticGadgets())         
            gadget.draw(graphics); 
        checkRep();
        return background;
    }
    
    /**
     * Draws balls on a board. Doesn't draw balls that are contained in an absorber.
     * 
     * @param graphics a Graphics object that represents the board to draw balls on
     */
    public synchronized void drawBalls(Graphics graphics) {
        ballLoop:
        for (Ball ball : balls) {
            for (Absorber absorber : this.absorbers) {
                if (absorber.isContained(ball)) {
                    continue ballLoop;
                }    
            }

            ball.draw(graphics);
        }
        checkRep();
    }
    
    /**
     * Draw the flippers on a board
     * 
     * @param graphics a Graphics object that represents the board to draw flippers on
     */
    public synchronized void drawFlippers(Graphics graphics) {
        for (Flipper flipper : flippers)
            flipper.draw(graphics);
        checkRep();
    }

    @Override 
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Board: " + name + "\n");
        sb.append("--| Gravity: " + gravity + "\n");
        sb.append("--| Friction1: " + friction1 + "\n");
        sb.append("--| Friction2: " + friction2 + "\n");
        sb.append("--| Balls:\n");
        for (Ball ball : balls) { sb.append("-----| " + ball.toString()); }
        sb.append("--| Bumpers:\n");
        for (Bumper bumper : bumpers) { sb.append("-----| " + bumper.toString()); }
        sb.append("--| Absorbers:\n");
        for (Absorber absorber : absorbers) { sb.append("-----| " + absorber.toString()); }
        sb.append("--| Flippers:\n");
        for (Flipper flipper : flippers) {sb.append("-----| " + flipper.toString());  }
        sb.append("--| Portals:\n");
        for (Portal portal : portals) {sb.append("-----| " + portal.toString());  }
        checkRep();
        return sb.toString();
    }
    
}
