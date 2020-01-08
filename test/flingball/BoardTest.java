package flingball;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import physics.*;

public class BoardTest {

    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    public static final float GRAVITY_DEFAULT = 25;
    public static final float FRICTION_DEFAULT = 0.025f;

    /*
     * Testing Strategy
     * 
     * addBall
     * Single ball
     * list of balls size = 0, 1, >1
     * 
     * addBumper
     * single bumper
     * list of bumpers size = 0, 1, >1
     * 
     * addAbsorber
     * single absorber
     * list of absorbers size = 0, 1, > 1
     * 
     * addFlipper
     * single flipper
     * list of flippers size = 0, 1, >1
     * 
     * addPortal
     * single portal
     * list of portals size = 0, 1, >1
     * 
     * getBalls
     * list of balls size = 0, 1, >1
     * 
     * getBumpers
     * list of bumpers size = 0, 1, >1
     * 
     * getAbsorbers
     * list of absorbers size = 0, 1, > 1
     * 
     * getFlipper
     * list size 0, 1, >1
     * 
     * getPortal
     * list size 0, 1, >1
     * 
     * getStaticGadgets
     * list size = 0, 1, > 1
     * 
     * getGravity
     * gravity = default (25), > default, 0 < gravity < default
     * 
     * getFriction1
     * mu1 = 0, default (0.025), 0 < mu1 < default, > default
     * 
     * getFriction2
     * mu2 = 0, default (0.025), 0 < mu2 < default, > default
     * 
     * getName
     * Name is lowercase, uppercase, mixed case
     * 
     * updateBoard
     * check output of ball: velocity, position
     * ball doesn't collide
     * ball does collide 
     
     * toString
     * # of gadgets = 0, 1, >1
     * 
     */

    /*
     * covers: addBall, getBalls
     * Single ball, list size = 1
     */
    @Test public void testAddBallSingleBall() {
        Board board = new Board("board name");
        Ball newBall = new Ball("ball name", new Vect(10, 10), new Vect(5, 5));
        board.addBall(newBall);
        assertEquals("Expect number of balls in board to be 1", 1, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * List size = 0
     */
    @Test public void testAddBallsNoBalls() {
        Board board = new Board("board name");
        List<Ball> empty = new ArrayList<Ball>();
        board.addBall(empty);
        assertEquals("Expected number of balls in board to be 0", 0, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * list size = 1;
     */
    @Test public void testAddBallsOneBallInList() {
        Board board = new Board("board name");
        board.addBall(Arrays.asList(new Ball("ball name", new Vect(10, 10), new Vect(5, 5))));
        assertEquals("Expect number of balls in board to be 1", 1, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * list size > 1
     */
    @Test public void testAddBAllsMultipleBallsInList() {
        Board board = new Board("board name");
        List<Ball> balls = new ArrayList<Ball>();
        balls.add(new Ball("ball1", new Vect(10, 10), new Vect(5, 5)));
        balls.add(new Ball("ball1", new Vect(2, 10), new Vect(1, 0)));
        board.addBall(balls);
        assertEquals("Expect number of balls in board to be 2", 2, board.getBalls().size());
    }

    /*
     * covers: addBumper, getBumpers
     * single bumper, list size = 1
     */
    @Test public void testAddBumperSingleBumper() {
        Board board = new Board("board name");
        Bumper newBumper = new CircleBumper("circle", new Vect(3, 15));
        board.addBumper(newBumper);
        assertEquals("Expected number of bumpers in board to be 1", 1, board.getBumpers().size());
    }

    /*
     * covers: addBumper, getBumpers
     * list size = 0
     */
    @Test public void testAddBumperNoBumpers() {
        Board board = new Board("board name");
        List<Bumper> empty = new ArrayList<Bumper>();
        board.addBumper(empty);
        assertEquals("Expected number of bumpers in board to be 0", 0, board.getBumpers().size());
    }

    /*
     * covers: addBumper, getBumpers
     * list size = 1
     */
    @Test public void testAddBumperOneBumperInList() {
        Board board = new Board("board name");
        List<Bumper> one = new ArrayList<Bumper>();
        one.add(new CircleBumper("circle", new Vect(3, 15)));
        board.addBumper(one);
        assertEquals("Expected number of bumpers in board to be 1", 1, board.getBumpers().size());
    }

    /*
     * covers: addBumper, getBumpers
     * list size > 1
     */
    @Test public void testAddBumperMultipleBumpers() {
        Board board = new Board("board name");
        List<Bumper> multi = new ArrayList<Bumper>();
        multi.add(new CircleBumper("circle", new Vect(3, 15)));
        multi.add(new TriangleBumper("triangle", new Vect(5, 6), new Angle(0)));
        board.addBumper(multi);
        assertEquals("expect number of bumpers in board to be 2", 2, board.getBumpers().size());
    }

    /*
     * covers: addAbsorber, getAbsobers
     * single Absorber, list size = 1
     */
    @Test public void testAddAbsorberSingle() {
        Board board = new Board("board");
        Absorber abs = new Absorber("abs", new Vect(0, 0), new Vect(5, 5));
        board.addAbsorber(abs);
        assertEquals("expected number of absorbers to be 1", 1, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 0
     */
    @Test public void testAddAbsorberNone() {
        Board board = new Board("board");
        List<Absorber> empty = new ArrayList<Absorber>();
        board.addAbsorber(empty);
        assertEquals("expect number of absorbers in board to be 0", 0, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 1
     */
    @Test public void testAddAbsorberOneInList() {
        Board board = new Board("board");
        List<Absorber> abs = new ArrayList<Absorber>();
        abs.add(new Absorber("abs1", new Vect(0, 0), new Vect(5, 5)));
        board.addAbsorber(abs);
        assertEquals("expect number of absorbers in board to be 1", 1, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 2
     */
    @Test public void testAddAbsorberMultiple() {
        Board board = new Board("board");
        List<Absorber> abs = new ArrayList<Absorber>();
        abs.add(new Absorber("abs1", new Vect(0, 0), new Vect(5, 5)));
        abs.add(new Absorber("abs2", new Vect(10, 10), new Vect(1, 1)));
        board.addAbsorber(abs);
        assertEquals("expect number of absorbers in board to be 2", 2, board.getAbsorbers().size());
    }
    
    final static double ANGULAR_VELOCITY_FLIPPER = Math.toRadians(1080);
    /*
     * covers: addFlipper, getFlipper
     * single Flipper, list size = 1
     */
    @Test public void testAddFlipperSingle() {
        Board board = new Board("board");
        Flipper flipper = new Flipper(true, "flipper1", new Vect(1, 1), Angle.DEG_90, new Angle(0), false,  ANGULAR_VELOCITY_FLIPPER);
        board.addFlipper(flipper);
        assertEquals("expected number of flippers to be 1", 1, board.getFlippers().size());
    }

    /*
     * covers: addFlippers, getFlippers
     * list size = 0
     */
    @Test public void testAddFlipperNone() {
        Board board = new Board("board");
        List<Portal> empty = new ArrayList<Portal>();
        board.addPortal(empty);
        assertEquals("expect number of flippers in board to be 0", 0, board.getFlippers().size());
    }

    /*
     * covers: addFlipper, getFlipper
     * list size = 1
     */
    @Test public void testAddFlipperOneInList() {
        Board board = new Board("board");
        List<Flipper> flippers = new ArrayList<>();
        flippers.add(new Flipper(true, "flipper1", new Vect(1, 1), Angle.DEG_90, new Angle(0), false,  ANGULAR_VELOCITY_FLIPPER));
        board.addFlipper(flippers);
        assertEquals("expect number of flippers in board to be 1", 1, board.getFlippers().size());
    }

    /*
     * covers: addFlipper, getFlipper
     * list size = 2
     */
    @Test public void testAddFlipperMultiple() {
        Board board = new Board("board");
        List<Flipper> flippers = new ArrayList<>();
        flippers.add(new Flipper(true, "flipper1", new Vect(1, 1), Angle.DEG_90, new Angle(0), false,  ANGULAR_VELOCITY_FLIPPER));
        flippers.add(new Flipper(true, "flipper1", new Vect(1, 1), Angle.DEG_90, new Angle(0), false,  ANGULAR_VELOCITY_FLIPPER));
        board.addFlipper(flippers);
        assertEquals("expect number of flippers in board to be 2", 2, board.getFlippers().size());
    }    
    
    /*
     * covers: addPortal, getPortal
     * single Portal, list size = 1
     */
    @Test public void testAddPortalSingle() {
        Board board = new Board("board");
        Portal portal = new Portal("portal1", new Vect(1,1), Optional.empty(), "portal2");
        board.addPortal(portal);
        assertEquals("expected number of portals to be 1", 1, board.getPortals().size());
    }

    /*
     * covers: addPortal, getPortal
     * list size = 0
     */
    @Test public void testAddPortalNone() {
        Board board = new Board("board");
        List<Portal> empty = new ArrayList<Portal>();
        board.addPortal(empty);
        assertEquals("expect number of portals in board to be 0", 0, board.getPortals().size());
    }

    /*
     * covers: addPortal, getPortal
     * list size = 1
     */
    @Test public void testAddPortalOneInList() {
        Board board = new Board("board");
        List<Portal> portals = new ArrayList<>();
        portals.add(new Portal("portal1", new Vect(1,1), Optional.empty(), "portal2"));
        board.addPortal(portals);
        assertEquals("expect number of portals in board to be 1", 1, board.getPortals().size());
    }

    /*
     * covers: addPortal, getPortal
     * list size = 2
     */
    @Test public void testAddPortalMultiple() {
        Board board = new Board("board");
        List<Portal> portals = new ArrayList<>();
        portals.add(new Portal("portal1", new Vect(1,1), Optional.empty(), "portal2"));
        portals.add(new Portal("portal2", new Vect(1,1), Optional.empty(), "portal1"));
        board.addPortal(portals);
        assertEquals("expect number of portals in board to be 2", 2, board.getPortals().size());
    } 

    /*
     * covers: getStaticGadgets
     * list size = 0
     */
    @Test public void testGetStaticGadgetsEmpty() {
        Board board = new Board("board");
        assertEquals("Expect empty board to have no gadgets", 0, board.getStaticGadgets().size());
    }

    /*
     * covers: getStaticGadgets
     * list size = 1
     */
    @Test public void testGetStaticGadgetsOne() {
        Board board = new Board("i'm board");
        Bumper newBumper = new CircleBumper("circle", new Vect(3, 15));
        board.addBumper(newBumper);
        assertEquals("Expected board to have 1 static gadget", 1, board.getStaticGadgets().size());
    }

    /*
     * covers: getStaticGadgets
     * list size > 1
     */
    @Test public void testGetStaticGadgetsMultiple() {
        List<Absorber> abs = new ArrayList<Absorber>();
        abs.add(new Absorber("abs1", new Vect(0, 0), new Vect(5, 5)));
        List<Bumper> bumps = new ArrayList<Bumper>();
        bumps.add(new CircleBumper("circle", new Vect(3, 15)));
        List<Ball> balls = new ArrayList<Ball>();
        balls.add(new Ball("ball name", new Vect(10, 10), new Vect(5, 5)));
        Board board = new Board("bored", balls, bumps, abs);
        assertEquals("expected board to have 2 static gadgets", 2, board.getStaticGadgets().size());
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 = default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2Default() {
        Board board = new Board("board");
        assertEquals("expected gravity to be default", GRAVITY_DEFAULT, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be default", FRICTION_DEFAULT, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be default", FRICTION_DEFAULT, board.getFriction1(), 0.001);
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 < default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2LessThanDefault() {
        float mu1 = 0.005f; // friction1
        float mu2 = 0.003f; // friction2
        float gravity = 5;
        Board board = new Board("board", gravity, mu1, mu2);
        assertEquals("expected gravity to be 5", 5, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be 0.005", 0.005, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be 0.003", 0.003, board.getFriction2(), 0.001);
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 > default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2MoreThanDefault() {
        float mu1 = 0.50f; // friction1
        float mu2 = 1f; // friction2
        float gravity = 50;
        Board board = new Board("board", gravity, mu1, mu2);
        assertEquals("expected gravity to be 50", 50, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be 0.5", 0.5, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be 1", 1, board.getFriction2(), 0.001);
    }

    /*
     * covers: getName
     * lowercase
     */
    @Test public void testGetNameAllLowercase() {
        String name = "bored";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: getName
     * uppercase
     */
    @Test public void testGetNameAllUppercase() {
        String name = "BOARD";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: getName
     * mixed case
     */
    @Test public void testGetNameAllMixedCase() {
        String name = "Im Bored";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: updateBoard
     * doesn't collide
     */
    @Test public void testUpdateBoardNoCollision() {
        final double time = .01;
        Board board = new Board("board", 0, 0, 0); // no consideration of gravity, no friction
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(0, 1));
        board.addBall(ball);
        board.updateBoard(0.01); // move the ball for one time step (.01)
        Vect expectedPosition = ball.getLocation().plus(ball.getVelocity().times(time));
        
        // only ball so index 0
        assertEquals("expect ball to have moved one time step", expectedPosition, board.getBalls().get(0).getLocation());
    }
    
    /*
     * covers: updateBoard
     * does collide
     */
    @Test
    public void testUpdateBoardCollision() {
        final double time = .01;
        Board board = new Board("board", 0, 0, 0); // no consideration of gravity, no friction
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(0, 1));
        Ball ball1 = new Ball("ball1", new Vect(10, 10.5), new Vect(0, -1));
        board.addBall(ball);
        board.addBall(ball1);
        board.updateBoard(0.01); // move the ball for one time step (.01)
        Vect expectedPositionball = ball.getLocation().plus(ball.getVelocity().times(-time));
        Vect expectedPositionball1 = ball1.getLocation().plus(ball1.getVelocity().times(-time));
        
        // only ball so index 0
        assertEquals("expect ball to have moved one time step", expectedPositionball, board.getBalls().get(0).getLocation());
        assertEquals("expect ball to have moved one time step", expectedPositionball1, board.getBalls().get(1).getLocation());
    }
    
    /* covers: toString
     * # of gadgets = 0
     */
    @Test public void testBoardToStringEmptyBoard() {
        String boardName = "empty";
        Board emptyBoard = new Board(boardName);
        String expected = "Board: empty\n" + 
                           "--| Gravity: 25.0\n" + 
                           "--| Friction1: 0.025\n" + 
                           "--| Friction2: 0.025\n" + 
                           "--| Balls:\n" + 
                           "--| Bumpers:\n" + 
                           "--| Absorbers:\n" +
                           "--| Flippers:\n" +
                           "--| Portals:\n";
        assertEquals("Expected strings to match", expected, emptyBoard.toString());
    }
    
    /* covers: toString
     * # of gadgets = 1
     */
    @Test public void testBoardToStringOneGadget() {
        String boardName = "oneBall";
        Ball ball = new Ball("ball", new Vect(10,10), new Vect(0,5));
        List<Ball> balls = new LinkedList<>(Arrays.asList(ball));
        Board oneBall = new Board(boardName, balls, Collections.emptyList(), Collections.emptyList());
        String expected = "Board: oneBall\n" + 
                          "--| Gravity: 25.0\n" + 
                          "--| Friction1: 0.025\n" + 
                          "--| Friction2: 0.025\n" + 
                          "--| Balls:\n" + 
                          "-----| Ball: ball @ <10.0,10.0>, Velocity: <0.0,5.0>\n" + 
                          "--| Bumpers:\n" + 
                          "--| Absorbers:\n" +
                          "--| Flippers:\n" +
                          "--| Portals:\n";
        assertEquals("Expected strings to match", expected, oneBall.toString());

    }
    
    /* covers: toString
     * # of gadgets > 1
     */
    @Test public void testBoardToStringMultipleGadgets() {
        String boardName = "multi";
        String ballName = "ball";
        Ball ball = new Ball(ballName, new Vect(10,10), new Vect(0,5));
        List<Ball> balls = new LinkedList<>(Arrays.asList(ball));
        String bumperName = "cB1";
        Bumper circleBumper = new CircleBumper(bumperName, new Vect(2,5));
        List<Bumper> bumpers = new LinkedList<>(Arrays.asList(circleBumper));
        Board multi = new Board(boardName, balls, bumpers, Collections.emptyList());
        String expected = "Board: multi\n" + 
                          "--| Gravity: 25.0\n" + 
                          "--| Friction1: 0.025\n" + 
                          "--| Friction2: 0.025\n" + 
                          "--| Balls:\n" + 
                          "-----| Ball: ball @ <10.0,10.0>, Velocity: <0.0,5.0>\n" + 
                          "--| Bumpers:\n" + 
                          "-----| Circle Bumper: cB1 @ <2.0,5.0>\n" + 
                          "--| Absorbers:\n" +
                          "--| Flippers:\n" +
                          "--| Portals:\n";
        assertEquals("Expected strings to match", expected, multi.toString());
    }
    
    // Test drawBoard
    @Test
    public void testDrawBoard() {
        final Board basicBoard = new Board("Basic Board");
        final Graphics board = basicBoard.drawBackground().getGraphics();
        assertEquals("Board color should be pink", Color.WHITE, board.getColor());
    }
}
