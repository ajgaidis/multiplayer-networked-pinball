package flingball;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Class is an immutable, threadsafe data type that is the action and response of pressing
 * a button on a keyboard.
 */
public class KeyAction implements KeyListener {

    private final Board board;
    private final String key;
    private final String gadget;
    private final boolean triggerOnKeyPressed;
    private boolean canTrigger = true;
    
    // Abstraction Function
    //  AF(board, key, gadget, triggerOnKeyPressed, canTrigger) = A listener for a keyboard event
    //              where key is the key it listening to get pressed or released, board is the board
    //              that pressing a key would have an action on. gadget is the gadget that is triggered
    //              on the press or release of the key that this listener is listening for.
    //              triggerOnKeyPressed determines whether an action is executed on key press or release.
    //              canTrigger is whether or not a key trigger can be activated or not.
    // Representation Invariant
    //  --| true
    // Safety from Representation Exposure
    //  --| all fields are private and final with the exception of canTrigger which is not final
    //      however no reference to this field (or any other) is ever returned to the client
    //  --| all methods return nothing/are void
    // Thread Safety Argument
    //  --| all fields are private
    //  --| monitor pattern is implemented, thus all methods are synchronized meaning that while
    //      canTrigger is not final, the caller of the methods change the field must control the lock
    //      on these methods
    
    /**
     * Checks to ensure that there is no representation exposure. 
     */
    private synchronized void checkRep() {
        assert board != null;
        assert key != null;
        assert gadget != null;
    }
    
    /**
     * Creates a KeyAction object that is the relationship between pressing a certain key
     * and generating the corresponding action given the pressed key.
     * 
     * @param board the flingball board that a KeyAction corresponds to. For example if an
     *              action happens on "BoardA" when the space key is pressed, the action will
     *              only happen on "BoardA" when a user has that Board running in the foreground.
     * @param triggerOnKeyPressed whether the key triggers on keydown (press) or keyup (release),
     *                must be either "keydown" or "keyup"
     * @param key the key that related to an action
     * @param gadget the gadget with a correspond action that is triggered when key is 
     *               pressed or released
     */
    public KeyAction(Board board, String triggerOnKeyPressed, String key, String gadget) {
        this.board = board;
        this.key = key;
        this.gadget = gadget;
        
        this.triggerOnKeyPressed = triggerOnKeyPressed.equals("keydown");
        
        checkRep();
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (triggerOnKeyPressed && 
                KeyNames.keyName.getOrDefault(e.getKeyCode(), "Key Not Found").equals(key) && canTrigger) {
            board.triggerGadgetByName(gadget);
            this.canTrigger = false;
        }
    }
    
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        if (!triggerOnKeyPressed && 
                KeyNames.keyName.getOrDefault(e.getKeyCode(), "Key Not Found").equals(key)) {
            board.triggerGadgetByName(gadget);
        } 
        if (KeyNames.keyName.getOrDefault(e.getKeyCode(), "Key Not Found").equals(key)) {
            this.canTrigger = true;
        }
    }

    @Override
    public synchronized void keyTyped(KeyEvent e) {} // interface method not needed

}
