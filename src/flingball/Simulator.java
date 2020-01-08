package flingball;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Simulator is a GUI interface on which to play a specific game of Flingball,
 * specified by board. It runs the simulation at a minimum of 10-20 frames per
 * second (FPS).
 */
public class Simulator {

    private final BufferedImage boardImage;
    private final Board board;
    private final List<KeyAction> listeners = new LinkedList<>();
    private static final int GAMEBOARD_SIZE = 20;
    private static final int PIXELS_PER_L = 20;
    private static final int DRAWING_AREA_SIZE_IN_PIXELS = GAMEBOARD_SIZE * PIXELS_PER_L;
    private static final int TIMER_INTERVAL_MILLISECONDS = 20;
    private static final ImageObserver NO_OBSERVER_NEEDED = null;

    // Abstraction Function
    //  AF(board, boardImage, listeners, GAMEBOARD_SIZE, PIXELS_PER_L, DRAWING_AREA_SIZE_IN_PIXELS,
    //          TIMER_INTERVAL_MILLISECONDS) = The simulator of a flingball game with board board with
    //                        boardImage which contains the background color and all static gadgets in
    //                        this board as an image. listeners specify the key action listeners that
    //                        trigger an event to happen during game play if a keyboard key is pressed
    //                        or released. GAMEBOARD_SIZE specifies the size of the width and height
    //                        of the game board getting simulated where each unit of width x height
    //                        are PIXELS_PER_L x PIXELS_PER_L pixels. DRAWING_AREA_SIZE_IN_PIXELS
    //                        further specifies this more exactly and TIMER_INTERVAL_MILLISECONDS
    //                        sets the frame-rate of the simulation. 
    // Representation Invariant
    //  --| boardImage and board are not null
    // Safety from Rep Exposure
    //  --| boardImage, board are private, final and is never returned
    // Thread Safety Argument
    //  --| all fields are private and final
    //  --| addToListeners, while it adds to a list in the rep, is only added to upon initialization
    //      of a board

    /**
     * Construct a Simulator with given board
     * 
     * @param board the flingball board to construct a simulation with
     */
    public Simulator(Board board) {
        this.board = board;
        boardImage = board.drawBackground();
        checkRep();
    }

    /**
     * Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private void checkRep() {
        assert board != null;
        assert boardImage != null;
    }

    /**
     * Adds the key listeners that correspond board to our key mappings so that when a button
     * is pressed, it corresponds to an action in a gadget 
     */
    public void addToListeners() {
        final List<String> listenersList = board.getProtoListeners();
        listeners.addAll(listenersList.stream().map(x -> x.split(" "))
            .map(x -> new KeyAction(board, x[0], x[1], x[2]))
            .collect(Collectors.toList()));
    }
    
    /**
     * Play the flingball game of this simulator
     */
    public void playFlingball() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                makeAndShowGUI();
            }
        });
    }

    /**
     * Makes and displays the window in which to run the Flingball game.
     */
    public void makeAndShowGUI() {
        final JFrame window = new JFrame("Flingball");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel drawingArea = new JPanel() {

            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.drawImage(boardImage, 0, 0, DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS,
                        NO_OBSERVER_NEEDED);
              
                board.drawBalls(graphics); 
                board.drawFlippers(graphics);
                board.drawJoinBanner(graphics);
            }
        };
        
        drawingArea.setPreferredSize(new Dimension(DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        window.add(drawingArea);
        window.pack();
        window.setVisible(true);
         // listen for keyboard events
        drawingArea.setFocusable(true);
        drawingArea.requestFocusInWindow();
        addToListeners();
        for (KeyAction listener : listeners) {
            drawingArea.addKeyListener(listener);   
        }

        new Timer(TIMER_INTERVAL_MILLISECONDS, (ActionEvent e) -> {
  
            final double startTime = System.nanoTime()*Board.EPSILON_9;
            double time1 = System.nanoTime();
            double delta = -1;
            while ((System.nanoTime()*Board.EPSILON_9-startTime-Board.EPSILON_7)< TIMER_INTERVAL_MILLISECONDS*Board.EPSILON_3) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                delta = (System.nanoTime()-time1)*Board.EPSILON_9;  
                time1 = System.nanoTime();
                board.updateBoard(delta);
                board.applyFrictionGravity(delta);
            }
            drawingArea.repaint();
        }).start(); 
    }

}
