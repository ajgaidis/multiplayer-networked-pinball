package flingball;

import java.util.Optional;

import org.junit.Test;

import physics.Angle;
import physics.Vect;


/**
 * Tests for the simulator
 * 
 *Testing Strategy:
 *Manually checked tests
 *
 *Simulation should be able to render and run for 10 seconds with the following conditions:
 *Balls: 1 ball, >1 ball
 *triangle bumper: 0,1,>1
 *square bumper: 0,1,>1
 *circle bumper: 0,1,>1
 *absorber: 0,1,>1
 *flipper: 0,1,>1
 *portal: 0,1,>1
 *keyaction: none, keyup, keydown
 *
 *Only 4 walls/more than just walls
 *
 *Ball speed: 0, 1, >1, >max speed(200)
 *
 * @category no_didit
 *
 */
public class SimulatorTest {

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    
    @Test
    // tests single ball, only 4 walls, velocity =1
    public void testSimulatorBallInBox() {
        Board board = new Board("default");
        
        board.addBall(new Ball("ball",new Vect(5,5),new Vect(1,0)));
        
        try {
            Simulator simulator = new Simulator(board);
            simulator.playFlingball();
            Thread.sleep(10000);
        } catch (Exception ex) {
            assert false: "Simulation broke" + ex;
        }
    }
    
    @Test
    // tests single ball, only 4 walls, velocity =0
    public void testSimulatorBallInBoxWithNoVelocity() {
        Board board = new Board("default");
        
        board.addBall(new Ball("ball",new Vect(5,5),new Vect(0,0)));
        
        try {
            Simulator simulator = new Simulator(board);
            simulator.playFlingball();
            Thread.sleep(10000);
        } catch (Exception ex) {
            assert false: "Simulation broke" + ex;
        }
    }
    
    
    // tests multiple balls, circle bumper, square bumper, triangle bumper, flipper, portal
    @Test
    public void testSimulatorMultipleBalls() {
        Board board = new Board("default");
        
        board.addBall(new Ball("ball",new Vect(5,5),new Vect(0,4)));
        board.addBall(new Ball("ball",new Vect(10,10),new Vect(0,5)));
        
        board.addBumper(new CircleBumper("circleA", new Vect(1, 10)));
        board.addBumper(new TriangleBumper("triangleA", new Vect(10,15), Angle.DEG_90));
        board.addBumper(new SquareBumper("circleA", new Vect(5, 3)));
        
        board.addFlipper(new Flipper(false, "flipA", new Vect(13, 12), Angle.DEG_90, new Angle(0), false, Math.toRadians(1080)));
        board.addPortal(new Portal("portalA", new Vect(11, 11), Optional.empty(), "portalC"));
        
        try {
            Simulator simulator = new Simulator(board);
            simulator.playFlingball();
            Thread.sleep(10000);
        } catch (Exception ex) {
            assert false: "Simulation broke" + ex;
        }
    }
    
    //tests absorber, mutliple balls, >1 triangle, >1 square, >1 circle bumpers, velocity >1
    @Test
    public void testSimulatorAbsorber() {
        Board board = new Board("default");
        
        board.addBall(new Ball("ball",new Vect(5,5),new Vect(0,4)));
        board.addBall(new Ball("ball",new Vect(10,10),new Vect(0,5)));
        
        board.addBumper(new CircleBumper("circleA", new Vect(5, 10)));
        board.addBumper(new TriangleBumper("triangleA", new Vect(19,0), Angle.DEG_90));
        board.addBumper(new SquareBumper("squareA", new Vect(1, 10)));
        board.addBumper(new CircleBumper("squareB", new Vect(4, 10)));
        board.addBumper(new TriangleBumper("triangleB", new Vect(0,0), new Angle(0)));
        board.addBumper(new SquareBumper("circleB", new Vect(3, 10)));
        
        board.addAbsorber(new Absorber("abs", new Vect(0,18), new Vect(10,2)));
        
        board.setTarget((Absorber) board.getGadgetByName("abs"), board.getGadgetByName("abs"));
        
        try {
            Simulator simulator = new Simulator(board);
            simulator.playFlingball();
            Thread.sleep(10000);
        } catch (Exception ex) {
            assert false: "Simulation broke" + ex;
        }
    }
    
    @Test
    //tests multiple absorbers, mutliple balls, >1 triangle, >1 square, >1 circle bumpers, >1 flippers
    //         >1 portals, velocity >maxspeed, keyup, keydown
    public void testMultipleGadgets() {
        Board board = new Board("default");
        
        board.addBall(new Ball("ball",new Vect(5,5),new Vect(300,300)));
        board.addBall(new Ball("ball",new Vect(10,10),new Vect(500,500)));
        
        board.addBumper(new CircleBumper("circleA", new Vect(1, 10)));
        board.addBumper(new TriangleBumper("triangleA", new Vect(19,0), Angle.DEG_90));
        board.addBumper(new SquareBumper("squareA", new Vect(1, 10)));
        board.addBumper(new CircleBumper("circleB", new Vect(4, 10)));
        board.addBumper(new TriangleBumper("triangleB", new Vect(0,0), new Angle(0)));
        board.addBumper(new SquareBumper("squareB", new Vect(3, 10)));
        
        board.addAbsorber(new Absorber("abs", new Vect(0,18), new Vect(10,2)));
        board.addAbsorber(new Absorber("abs2", new Vect(11,18), new Vect(9,2)));
        
        board.addFlipper(new Flipper(true, "flipR1", new Vect(7, 7), new Angle(0), new Angle(0), false, Math.toRadians(1080)));
        board.addFlipper(new Flipper(false, "flipL1", new Vect(3, 4), new Angle(0), new Angle(0), false, Math.toRadians(1080)));
        
        Portal portalA = new Portal("portalA", new Vect(19, 10), Optional.empty(), "portalB");
        Portal portalB = new Portal("portalB", new Vect(19, 7), Optional.empty(), "portalA");
        board.addPortal(portalA);
        board.addPortal(portalB);
        board.addLocalPortal(portalA);
        board.addLocalPortal(portalB);
        
        board.setTarget((Absorber) board.getGadgetByName("abs"), board.getGadgetByName("circleA"));
        board.setTarget((Absorber) board.getGadgetByName("abs2"), board.getGadgetByName("abs2")); 
        
        board.addProtoListener("keyup a abs");
        board.addProtoListener("keydown z flipR1");
        
        try {
            Simulator simulator = new Simulator(board);
            simulator.playFlingball();
            Thread.sleep(10000);
        } catch (Exception ex) {
            assert false: "Simulation broke" + ex;
        }
    }
}
