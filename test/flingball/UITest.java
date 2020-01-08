package flingball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Tests for the user interface in Flingball. Cannot be run simulatneously (slowdown from many open simulations)
 *   must run each test individually
 *
 * @category no_didit
 * 
 */
public class UITest {
    
    private static final double DELTA = 0.00001;
    
    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /*
     * Testing strategy: 
     * 
     * Key up & down trigger absorbers
     *                       right flippers
     *                       left flippers
     * One key press at a time, two key presses, >2 key presses:
     *      At least one key doesn't trigger anything
     *      Multiple keys trigger same gadget
     *      Keys trigger different gadgets
     *      Multiple gadgets triggered by single key
     *          -  combination of key up + key down
     */
    // covers key up triggers absorber
    //        one key press
    @Test
    public void testKeyUpAbsorberTrigger() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // wait for simulator
        
        robot.keyPress(KeyEvent.VK_A);
        
        Thread.sleep(50);
        
        assertEquals("Absorber 1 should have one ball", 1,
                board.getAbsorberNameToBallMap().get("Abs1").size());
        
        robot.keyRelease(KeyEvent.VK_A);
        
        Thread.sleep(50);
        assertEquals("Absorber 1 should have no balls in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs1").size());
    }
    
    
    // covers key down triggers absorber
    //        one key press
    @Test
    public void testKeyDownAbsorberTrigger() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // wait for simulator
        
        robot.keyPress(KeyEvent.VK_Z);
        
        Thread.sleep(50); // update board
        
        assertEquals("Absorber 2 should have one ball in it now", 1,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers key up triggers right flipper
    //        one key press
    @Test
    public void testKeyUpRightFlipper() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        Robot robot = new Robot();
        Flipper flipper;
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure window is open first
        robot.keyPress(KeyEvent.VK_X);
        
        Thread.sleep(50); // let board update
        flipper = (Flipper) board.getGadgetByName("FlipR2");
        assertFalse("Should not be flipping yet", flipper.isFlipping());
        
        robot.keyRelease(KeyEvent.VK_X);
        Thread.sleep(50); // let board update
        flipper = (Flipper) board.getGadgetByName("FlipR2");
        assertTrue("Should be flipping", flipper.isFlipping());
    }
    
    // covers key down triggers right flipper
    //        one key press
    @Test
    public void testKeyDownRightFlipper() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        Flipper flipper;
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        flipper = (Flipper) board.getGadgetByName("FlipR1");
        robot.keyPress(KeyEvent.VK_S);
        
        Thread.sleep(50); // let board update
        assertFalse("Should not be flipping", flipper.isFlipping());
        
        robot.keyRelease(KeyEvent.VK_S);
        flipper = (Flipper) board.getGadgetByName("FlipR1");
        
        Thread.sleep(50); // let board update
        assertTrue("Should be flipping", flipper.isFlipping());
    }
    
    // covers key up triggers left flipper
    //        one key press
    @Test
    public void testKeyUpLeftFlipper() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        Robot robot = new Robot();
        Flipper flipper;
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_Z);

        Thread.sleep(50); // update board
        
        flipper = (Flipper) board.getGadgetByName("FlipL2");
        assertFalse("Should not be flipping yet", flipper.isFlipping());
        
        robot.keyRelease(KeyEvent.VK_Z);
        
        Thread.sleep(50);
        
        flipper = (Flipper) board.getGadgetByName("FlipL2");
        assertTrue("Should be flipping", flipper.isFlipping());
    }
    
    // covers key down triggers left flipper
    //        one key press
    @Test
    public void testKeyDownLeftFlipper() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        Robot robot = new Robot();
        Flipper flipper;
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_A);
        
        Thread.sleep(50); // update board
        
        flipper = (Flipper) board.getGadgetByName("FlipL1");
        assertTrue("Should be flipping", flipper.isFlipping());
        
        Thread.sleep(250); // make sure done flipping
        
        robot.keyRelease(KeyEvent.VK_A);
        
        Thread.sleep(50); // update board
        
        flipper = (Flipper) board.getGadgetByName("FlipL1");
        assertFalse("Should not be flipping", flipper.isFlipping());
    }
    
    // covers two keys trigger same gadget
    //            both key downs
    @Test
    public void testTwoKeysSameGadget() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        board.updateBoard(DELTA);
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_Z);
        robot.keyPress(KeyEvent.VK_F);

        Thread.sleep(50);
        
        assertEquals("Absorber 2 should have nothing in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers two keys trigger different gadgets
    //              both keys down
    @Test
    public void testTwoKeysDifferentGadgets() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        board.updateBoard(DELTA);
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_Z);
        robot.keyPress(KeyEvent.VK_F);

        Thread.sleep(50); // update board
        
        assertEquals("Absorber 1 should have nothing in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs1").size());
        assertEquals("Absorber 2 should have one ball in it now", 1,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers two keys, one does nothing
    @Test
    public void testTwoOneWorthless() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        board.updateBoard(DELTA);
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_Z);
        robot.keyPress(KeyEvent.VK_T);
        
        Thread.sleep(250); // update board
        
        assertEquals("Absorber 2 should have nothing in it now", 1,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers >2 keys, different gadgets
    @Test
    public void testLotsOfKeysDifferentGadgets() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        board.updateBoard(DELTA);
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_A);
        robot.keyPress(KeyEvent.VK_S);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_ALT);

        Thread.sleep(50); // update board
        
        Flipper flipperL1 = (Flipper) board.getGadgetByName("FlipL1");
        assertTrue("Flipper L1 should be flipping", flipperL1.isFlipping());
        
        Flipper flipperL2 = (Flipper) board.getGadgetByName("FlipR1");
        assertTrue("Flipper R1 should be flipping", flipperL2.isFlipping());
        
        assertEquals("Absorber 1 should have nothing in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs1").size());
        assertEquals("Absorber 2 should have nothing in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers key triggers multiple gadgets
    //             both key down
    @Test
    public void testKeyDownMultipleGadgets() throws UnableToParseException, AWTException, InterruptedException  {
        final Board board = BoardParser.parse(new File("boards/simple_keys.fb"));
        board.updateBoard(DELTA);
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_F);
        
        Thread.sleep(50); // update board
        
        assertEquals("Absorber 1 should have nothing in it now", 0,
                board.getAbsorberNameToBallMap().get("Abs1").size());
        assertEquals("Absorber 2 should have one ball in it now", 1,
                board.getAbsorberNameToBallMap().get("Abs2").size());
    }
    
    // covers key triggers multiple gadgets
    //              one key up, one key down
    @Test
    public void testKeyUpDownMultipleGadgets() throws UnableToParseException, AWTException, InterruptedException {
        final Board board = BoardParser.parse(new File("boards/multi_key.fb"));
        Robot robot = new Robot();
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(250); // make sure simulator is running
        
        robot.keyPress(KeyEvent.VK_A);
        robot.keyPress(KeyEvent.VK_Z);
        robot.keyRelease(KeyEvent.VK_Z);
        
        Thread.sleep(50); // make sure board gets updated
        
        Flipper flipperL1 = (Flipper) board.getGadgetByName("FlipL1");
        assertTrue("Flipper L1 should be flipping", flipperL1.isFlipping());
        
        Flipper flipperL2 = (Flipper) board.getGadgetByName("FlipL2");
        assertTrue("Flipper L2 should be flipping", flipperL2.isFlipping());
    }
}
