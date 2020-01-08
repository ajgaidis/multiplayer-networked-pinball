package flingball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import org.junit.Test;

import physics.Angle;
import physics.Vect;

public class GadgetTest {

    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /*
     * Types of Gadgets
     * Bumpers: Circle, Triangle, Square
     * Ball
     * Wall
     * Absorber
     * 
     * Testing Strategy:
     *
     * getLocation
     * Location Vect X position = 0, 0 < x < 19, 19
     * Location Vect Y position = 0, 0 < y< 19, 19
     * 
     * triggered
     * Ball hitting the gadget (Vect locations are equal)
     * Ball close to hitting gadget (vect locations are very close)
     * Ball not close/not hitting (Vect locations are significantly different)
     * 
     * hasTriggerTarget
     * number of trigger targets = 0, 1, > 1 (expect: false, true, true)
     * 
     * getTriggerTargets
     * number of trigger targets = 0, 1, > 1
     * 
     * getName
     * name empty
     * name not empty
     * 
     * equals
     * transitive
     * symmetric
     * reflexive
     * aliased objects (expect true)
     * objects constructed same way (expect true)
     * different components (name, position, etc) (expect false)
     * 
     * Absorber specific:
     * getSize
     * Width, height = 1, 1 < w,h < 19, 19 // could take up the whole board
     * theoretically. Must have non zero width/height
     * 
     * action
     * # of balls holding: 1, >1
     * expect number to reduce by 1 if >= 1
     * 
     * getNumberOfBalls
     * # of balls holding: 0, 1, >1
     * 
     * draw (drawSquare, drawCircle, drawTriangle, drawAbsorber)
     *  These gadgets will be drawn on a Graphics object that is the result of calling 
     *  .getGraphics() on a BufferedImage. This BufferedImage can then be tested for
     *  appropriate dimensions, and accurate pixel placement using examinePixelsOfImage(),
     *  a method adapted from the example code in PSet3.
     * 
     */
    

    /* covers: equals
    * transitive, reflexive
    * aliased objects
    */
    @Test public void testEqualsAliasedGadgets( ) {
        Ball ballOne = new Ball("ball", new Vect(10,10), new Vect(5,5));
        Ball ballTwo = ballOne;
        Ball ballThree = ballOne;
        assertTrue("Expect aliases to be equal", ballOne.equals(ballTwo));
        assertTrue("expect aliases to be equal", ballTwo.equals(ballThree));
        assertTrue("expect aliases to be equal", ballThree.equals(ballOne));
        assertTrue("expect object to equal itself", ballOne.equals(ballOne));
    }
    
    /* covers: equals
     * constructed same way
     * symmetry
     */
    @Test public void testEqualsSameButNotAliases() {
        SquareBumper bumperOne = new SquareBumper("bumper", new Vect(10,10));
        SquareBumper bumperTwo = new SquareBumper("bumper", new Vect(10,10));
        assertTrue("expect to be equal", bumperOne.equals(bumperTwo));
        assertTrue("expect to be equal on symmetry", bumperTwo.equals(bumperOne));
    }
    
    /* 
     * covers: equals
    * different components (name, position, etc) (expect false)
    */
    @Test public void testEqualsNotSame() {
        Absorber absOne = new Absorber("abs1", new Vect(4,4), new Vect(5,5));
        Absorber absTwo = new Absorber("abs2",new Vect(4,4), new Vect(5,5));
        assertFalse("expect to be not equals because different names", absOne.equals(absTwo));
    }
    
    /*
     * covers: getLocation
     * X start = 0
     * Y start = 0
     */
    @Test public void testGetLocationTopLeftCorner() {
        SquareBumper bumper = new SquareBumper("bummer", new Vect(0, 0));
        assertEquals("Expect position to be at origin", new Vect(0, 0), bumper.getLocation());
    }

    /*
     * covers: getLocation
     * 0 < X < 19
     * 0 < Y < 19
     */
    @Test public void testGetLocationMiddleBoard() {
        TriangleBumper bumper = new TriangleBumper("bummer", new Vect(5, 10), new Angle(0));
        assertEquals("expected positions to match", new Vect(5, 10), bumper.getLocation());
    }

    /*
     * covers: getLocation
     * X = 19
     * Y = 19
     */
    @Test public void testGetLocationBottomRightCorner() {
        CircleBumper bumper = new CircleBumper("bummer", new Vect(19, 19));
        assertEquals("expected bottom right corner to be valid position", new Vect(19, 19), bumper.getLocation());
    }

    /*
     * covers: getName
     * name not empty (Can't be empty)
     */
    @Test public void testGetName() {
        TriangleBumper bumper = new TriangleBumper("Bump", new Vect(10, 10), new Angle(0));
        assertEquals("expect name to be Bump", "Bump", bumper.getName());
    }

    /*
     * covers: getSize for absorber
     * Width, height = 1
     */
    @Test public void testAbsorberGetSizeSmall() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(1, 1));
        assertEquals("Expected width and height to be 1", new Vect(1, 1), abs.getSize());
    }

    /*
     * covers: getSize
     * 1 < width, height < 19
     */
    @Test public void testAbsorberGetSizeMedium() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(5, 10));
        assertEquals("Expected width and height to be 5, 10", new Vect(5, 10), abs.getSize());
    }

    /*
     * covers: getSize
     * width, height = 19
     */
    @Test public void testAbsorberGetSizeLarge() {
        Absorber abs = new Absorber("abs1", new Vect(0, 0), new Vect(19, 19));
        assertEquals("Expected width and height to be 19", new Vect(19, 19), abs.getSize());
    }

    /*
     * covers: action, getNumberOfBalls
     * # of balls held = 1, 0 (after)
     */
    @Test public void testAbsorberActionOneBall() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(5, 10));
        Ball firedBall = abs.action("ball");
        Ball expectedBall = new Ball("ball", new Vect(6.75,14.75), new Vect(0, -50));
        assertEquals("expected an equal fired ball", expectedBall, firedBall);
    }

    /*
     * covers: action, getNumberOfBalls
     * # of balls held > 1, # of balls held - 1 (after)
     */
    @Test public void testAbsorberActionMultipleBalls() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(5, 10));

        Ball firedBall = abs.action("ball");
        Ball expectedBall = new Ball("ball", new Vect(6.75, 14.75), new Vect(0, -50));

        assertEquals("expected an equal fired ball", expectedBall, firedBall);
    }
    
    // A unit on the board, L, is equal to 20 pixels
    private static final int L = 20;
    
    // Test drawSquare
    @Test
    public void testDrawSquare() {
        final int outputImageWidth = 3*L;
        final int outputImageHeight = 3*L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();
        
        SquareBumper squareBump = new SquareBumper("SimpleSquare", new Vect(1.0, 1.0));
        squareBump.draw(graphics);
        
        assertEquals("Image width should not have changed.", 3*L, buffImg.getWidth());
        assertEquals("Image height should not have changed.", 3*L, buffImg.getHeight());
        
        assertEquals("Square color should be magenta", Color.MAGENTA, graphics.getColor());
        
        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(0, 0, 3*L, L);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }
    
    // Test drawCircle
    @Test
    public void testDrawCircle() {
        final int outputImageWidth = L;
        final int outputImageHeight = L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();
        
        CircleBumper circleBump = new CircleBumper("Simple Circle", new Vect(0.5, 0.5));
        circleBump.draw(graphics);
        
        assertEquals("Image width should not have changed.", L, buffImg.getWidth());
        assertEquals("Image height should not have changed.", L, buffImg.getHeight());
        
        assertEquals("Circle color should be pink", Color.PINK, graphics.getColor());
        
        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(19, 19, 1, 1);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }
    
    // Test drawTriangle
    @Test
    public void testDrawTriangle() {
        final int outputImageWidth = L;
        final int outputImageHeight = L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();
        
        TriangleBumper triangle1 = new TriangleBumper("Triangle1", new Vect(0.0, 0.0), Angle.ZERO);
        triangle1.draw(graphics);
        BufferedImage subImage = buffImg.getSubimage(18, 18, 2, 2);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());

        // Two triangles together will make a square
        TriangleBumper triangle2 = new TriangleBumper("Triangle2", new Vect(0.0, 0.0), Angle.DEG_180);
        triangle2.draw(graphics);

        assertEquals("Tringles' color should be cyan", Color.CYAN, graphics.getColor());
        
        assertEquals("Image width should not have changed", L, buffImg.getWidth());
        assertEquals("Image height should not have changed", L, buffImg.getHeight());
    }
    
    // Test drawAbsorber
    @Test
    public void testDrawAbsorber() {
        final int outputImageWidth = 20*L;
        final int outputImageHeight = 20*L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();
        
        Absorber absorber = new Absorber("Simple Absorber", new Vect(0.0, 1.0), new Vect(20, 19));
        absorber.draw(graphics);
        
        assertEquals("Image width should not have changed", 20*L, buffImg.getWidth());
        assertEquals("Image height should not have changed", 20*L, buffImg.getHeight());
        
        assertEquals("Absorber color should be pink", Color.PINK, graphics.getColor());
        
        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(0, 0, L, 20*L);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }

}
