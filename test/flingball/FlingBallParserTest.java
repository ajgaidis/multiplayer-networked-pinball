package flingball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import physics.Angle;
import physics.Circle;
import physics.Vect;

public class FlingBallParserTest {
    
    /*
     * Testing Strategy for each method in FlingBallParser:
     * 
     * parse():
     * 
     * file contains 0,1,>1 balls
     * file contains 0,>=1 gadgets
     * 
     * file contains line that is not comment
     * file contains line that is a comment
     * 
     * file contains, does not contain gravity
     * file contains, does not contain friction1
     * file contains, does not contain friction2
     * 
     * file contains, does not contain triangleBumper
     * file contains, does not contain squareBumper
     * file contains, does not contain circleBumper
     * file contains, does not contain absorber
     * file contains, does not contain triangleBumper
     * 
     * file contains, does not contain leftflipper/rightflipper
     * file contains, does not contain portal
     * file contains, does not contain keyup
     * file contains, does not contain keydown
     * 
     * file contains, does not contain event trigger-action
     * trigger-action is, is not self trigger
     * 
     */
    
    private static boolean checkContainGadget(List<Gadget> allGadgets, String name) {
        for (Gadget gadget : allGadgets) {
            if (gadget.getName().equals(name)) return true;
        }
        return false;
    }
    
    private static Gadget findGadget(List<Gadget> allGadgets, String name) {
        for (Gadget gadget : allGadgets) {
            if (gadget.getName().equals(name)) return gadget;
        }
        return null;
    }
    
    private static boolean checkContainFlipper(List<Flipper> allFlippers, String name) {
        for (Flipper flipper : allFlippers) {
            if (flipper.getName().equals(name)) return true;
        }
        return false;
    }
    
    private static boolean checkContainBall(List<Ball> allBalls, String name) {
        for (Ball ball : allBalls) {
            if (ball.getName().equals(name)) return true;
        }
        return false;
    }
    
    private static Ball findBall(List<Ball> allBalls, String name) {
        for (Ball ball : allBalls) {
            if (ball.getName().equals(name)) return ball;
        }
        return null;
    }
    
    private static boolean compareDouble(double x, double y) {
        return Math.abs(x-y) < 0.00001;
    }
    
    /*
     * covers:
     *  - file contains >1 balls
     *  - file contains event trigger-action
     *  - trigger-action is self trigger
     *  - trigger-action is not self-trigger
     *  - file does not contain SquareBumper
     *  - file does contain Absorber
     *  - file does not contain TriangleBumper
     *  - file contains CircleBumper
     *
     *  - file does not contain gravity
     *  - file does not contain friction1
     *  - file does not contain friction2
     */
    @Test
    public void testAbsorberFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/absorber.fb"));
        
        assertEquals("Wrong name", "Absorber", board.getName());
        List<Gadget> allGadgets = board.getStaticGadgets();
        List<Ball> allBalls = board.getBalls();
        assertTrue("Missing some ball", checkContainBall(allBalls, "BallA"));
        assertTrue("Missing some ball", checkContainBall(allBalls, "BallB"));
        assertTrue("Missing some ball", checkContainBall(allBalls, "BallC"));
        final Ball ballA = findBall(allBalls,"BallA");
        final Vect ballAVelocity = ballA.getVelocity();
        final Circle ballACircle = ballA.getCircle();
        final double expectedVelocityXballA = 0.1;
        final double expectedVelocityYballA = 0.2;
        final double expectedCenterXballA = 10.25;
        final double expectedCenterYballA = 15.25;
        assertTrue("Wrong velocity", compareDouble(ballAVelocity.x(),expectedVelocityXballA));
        assertTrue("Wrong velocity", compareDouble(ballAVelocity.y(),expectedVelocityYballA));
        assertTrue("Wrong center", compareDouble(ballACircle.getCenter().x(),expectedCenterXballA));
        assertTrue("Wrong center", compareDouble(ballACircle.getCenter().y(),expectedCenterYballA));
        
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleC"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleD"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleE"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleF"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleG"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"Abs1"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"Abs2"));
        
        final CircleBumper circleA = (CircleBumper) findGadget(allGadgets, "CircleA");
        final Vect circleALocation = circleA.getLocation();
        final double expectedXcircleA = 1.0;
        final double expectedYcircleA = 10.0;
        assertTrue("Wrong Location", compareDouble(circleALocation.x(),expectedXcircleA));
        assertTrue("Wrong Location", compareDouble(circleALocation.y(),expectedYcircleA));
        
        final Absorber absorber1 = (Absorber) findGadget(allGadgets, "Abs1");
        
        assertTrue("gadget is not triggered", board.getAbsorberTarget(circleA).contains(absorber1));
        
        
        final Absorber absorber2 = (Absorber) findGadget(allGadgets, "Abs2");
        assertTrue("gadget is not triggered", board.getAbsorberTarget(absorber2).contains(absorber2));
    }
    
    /*
     * covers:
     *  - file contains 1 ball
     *  - file contain TriangleBumper
     *  - file contain SquareBumper
     *  - file does not contain CircleBumper
     *  - file does not contain Absorber
     *  
     */
    @Test
    public void testDefaultFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/default2.fb"));
        assertEquals("Wrong name", "Default", board.getName());
        List<Gadget> allGadgets = board.getStaticGadgets();
        
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"SquareA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"SquareB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"SquareC"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"Tri1"));
        assertTrue("Shouldn't read commented gadget", !checkContainGadget(allGadgets,"Tri2"));
        
        final SquareBumper squareA = (SquareBumper) findGadget(allGadgets, "SquareA");
        final Vect squareALocation = squareA.getLocation();
        final double expectedXSquareA = 0.0;
        final double expectedYSquareA = 17.0;
        assertTrue("Wrong Location", compareDouble(squareALocation.x(),expectedXSquareA));
        assertTrue("Wrong Location", compareDouble(squareALocation.y(),expectedYSquareA));
        
        final TriangleBumper triangleA = (TriangleBumper) findGadget(allGadgets, "Tri1");
        final Vect triangleALocation = triangleA.getLocation();
        final Angle triangleAOrientation = triangleA.getRotation();
        final double expectedXTriangleA = 12.0;
        final double expectedYTriangleA = 15.0;
        assertTrue("Wrong Location", compareDouble(triangleALocation.x(),expectedXTriangleA));
        assertTrue("Wrong Location", compareDouble(triangleALocation.y(),expectedYTriangleA));
        assertTrue("Wrong Orientation", compareDouble(triangleAOrientation.radians(),Math.PI));
        
        final float expectedGravity = (float) 24.0;
        final double expectedFriction1 = 0.2;
        final double expectedFriction2 = 0.6;
        assertEquals("Wrong gravity", expectedGravity, board.getGravity(), 0.01);
        assertTrue("Wrong friction1", compareDouble(expectedFriction1, board.getFriction1()));
        assertTrue("Wrong friction2", compareDouble(expectedFriction2, board.getFriction2()));
    }
    
    // covers:
    //  -contains left flippers, right flippers
    //  -contains circle bumpers
    //  -contains triangle bumpers
    //  -contains absorbers
    @Test
    public void testFlippersFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/flippers.fb"));
        assertEquals("Wrong name", "Flippers", board.getName());
        List<Gadget> allGadgets = board.getStaticGadgets();
        List<Flipper> allFlippers = board.getFlippers();
        
        // Flippers
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipA"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipB"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipC"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipD"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipE"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers,"FlipF"));
        
        Flipper flipA = (Flipper) board.getGadgetByName("FlipA");
        final Vect flipALocation = flipA.getLocation();
        final double expectedXFlipA = 0;
        final double expectedYFlipA = 8;
        assertEquals("Wrong location", expectedXFlipA, flipALocation.x(), 0.01);
        assertEquals("Wrong location", expectedYFlipA, flipALocation.y(), 0.01);
        
        // Circles
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleC"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleD"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleE"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"CircleF"));
        
        // Triangle Bumpers
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"TriA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"TriB"));
        
        // Absorbers
        assertTrue("Missing some gadget", checkContainGadget(allGadgets,"Abs"));
    }
    
    // covers:
    //  -contains square bumpers
    //  -contains circle bumpers
    //  -contains triangle bumper
    //  -contains portals
    @Test
    public void testPortalFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/portal.fb"));
        assertEquals("Wrong name", "Portal", board.getName());
        List<Gadget> allGadgets = board.getStaticGadgets();
        
        // Square Bumpers
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "SquareA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "SquareB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "SquareC"));
        
        // Circle Bumpers
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "CircleA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "CircleB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "CircleC"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "CircleD"));
        
        // Triangle Bumper
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "Tri"));
        
        // Portals
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "PortalA"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "PortalB"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "PortalC"));
        
        Portal portalC = (Portal) board.getGadgetByName("PortalC");
        final Vect portalCLocation = portalC.getLocation();
        final double expectedXPortalC = 7;
        final double expectedYPortalC = 7;
        
        assertEquals("Wrong location", expectedXPortalC, portalCLocation.x(), 0.01);
        assertEquals("Wrong location", expectedYPortalC, portalCLocation.y(), 0.01);
        
        assertEquals("Wrong other board", "Portal2", portalC.getConnectedBoard().get());
        assertEquals("Wrong other portal", "PortalB", portalC.getConnectedPortal());
    }
    
    // covers:
    //  -contains left flipper, right flipper
    //  -contains absorbers
    //  -contains keyup
    //  -contains keydown
    @Test
    public void testMultiKeyFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        assertEquals("Wrong name", "MultiKey", board.getName());
        final List<Gadget> allGadgets = board.getStaticGadgets();
        final List<Flipper> allFlippers = board.getFlippers();
        
        // Flippers
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers, "FlipL1"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers, "FlipL2"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers, "FlipR1"));
        assertTrue("Missing some gadget", checkContainFlipper(allFlippers, "FlipR2"));
        
        // Absorbers
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "Abs1"));
        assertTrue("Missing some gadget", checkContainGadget(allGadgets, "Abs2"));
        
        // Key actions
        final List<String> protoListeners = board.getProtoListeners();
        assertTrue("Missing FlipL1 key", protoListeners.contains("keydown a FlipL1"));
        assertTrue("Missing FlipL2 key", protoListeners.contains("keyup z FlipL2"));
        assertTrue("Missing FlipR1 key", protoListeners.contains("keydown s FlipR1"));
        assertTrue("Missing FlipR2 key", protoListeners.contains("keyup x FlipR2"));
        assertTrue("Missing Abs1 key", protoListeners.contains("keydown space Abs1"));
        assertTrue("Missing Abs2 key", protoListeners.contains("keyup alt Abs2"));
    }
    
    /*
     * covers:
     *  - file contains 0 balls
     *  - file contains 0 gadgets
     */
    @Test
    public void testEmptyFile() throws UnableToParseException {
        final Board board = BoardParser.parse(new File("boards/empty.fb"));
        assertEquals("Wrong name", "default", board.getName());
        List<Gadget> allGadgets = board.getStaticGadgets();
        List<Ball> allBalls = board.getBalls();
        final int expectedNumberBalls = 0;
        final int expectedNumberGadgets = 0;
        assertEquals("Should have no gadgets", expectedNumberGadgets, allGadgets.size());
        assertEquals("Should have no ball", expectedNumberBalls, allBalls.size());
    }
}