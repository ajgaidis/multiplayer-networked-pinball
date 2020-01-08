import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Smile draws a smiley face to demonstrate some Java2D drawing commands, and
 * changes its color whenever a key is pressed to demonstrate some keyboard event handling.
 */
public class Smile2 {

    private static final int GAMEBOARD_SIZE = 20;
    private static final int PIXELS_PER_L = 20;
    private static final int DRAWING_AREA_SIZE_IN_PIXELS = GAMEBOARD_SIZE * PIXELS_PER_L;
    
    private static final int TIMER_INTERVAL_MILLISECONDS = 50; // for ~20 frames per second
    
    private static Color faceColor = Color.white;
    
    /*
     * Main program. Make a window containing the drawing area.
     */
    public static void main(String[] args) {
        animate();
    }
    
    // Best animation approach for combining with keyboard handling:
    // use a UI timer and paintComponent()/repaint()
    private static void animate() {
        final JFrame window = new JFrame("Smile");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final JPanel drawingArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                drawSmile(g);
            }
        };
        
        drawingArea.setPreferredSize(new Dimension(DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        window.add(drawingArea);
        window.pack();
        window.setVisible(true);

        // listen for keyboard events
        drawingArea.setFocusable(true);
        drawingArea.requestFocusInWindow();
        drawingArea.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                faceColor = Color.yellow;
            }

            @Override public void keyReleased(KeyEvent e) {
                System.out.println("jds");
                faceColor = Color.white;
            }
        });      

        
        initializeSmile();
        // note: the time must be javax.swing.Timer, not java.util.Timer
        new Timer(TIMER_INTERVAL_MILLISECONDS, (ActionEvent e) -> {
            drawingArea.repaint();
        }).start();
    }
    
    
    //
    // Drawing
    //
    
    private static final double MILLISECONDS_PER_PIXEL = 5.0 * 1000 / DRAWING_AREA_SIZE_IN_PIXELS; // cross the board in 5 seconds

    private static final double SMILE_WIDTH = 80;
    private static final double SMILE_HEIGHT = 80;
    private static final double EYE_SIZE = 9;
    private static final double EYE_OFFSET = SMILE_WIDTH/6;
    
    private static final int SMILE_STROKE_WIDTH = 3;

    
    private static double smileX;
    private static double smileY;
    private static long timeOfLastDraw;
    
    /*
     * Initialize the smile animation.
     */
    private static void initializeSmile() {
        smileX = 0;
        smileY = 0;
        timeOfLastDraw = System.currentTimeMillis();
    }
    
    /*
     * Animate a smile across the window.
     * @param g graphics for the drawing buffer for the window.  Modifies this graphics by drawing a smile on it, at a 
     * position determined by the current clock time.
     */
    private static void drawSmile(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;  // every Graphics object is also a Graphics2D, which is a stronger spec
        
        // fill the background to erase everything
        g2.setColor(Color.black);
        g2.fill(new Rectangle2D.Double(0, 0, DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));

        // animate the position of the smile by computing a position directly from clock time.
        final long now = System.currentTimeMillis();
        final long timeSinceLastDraw = now - timeOfLastDraw;
        timeOfLastDraw = now;
        
        final double pixelsMoved = timeSinceLastDraw / MILLISECONDS_PER_PIXEL;
        smileX += pixelsMoved;
        smileX = Math.min(smileX, DRAWING_AREA_SIZE_IN_PIXELS - SMILE_WIDTH);
        smileY += pixelsMoved;
        smileY = Math.min(smileY, DRAWING_AREA_SIZE_IN_PIXELS - SMILE_HEIGHT);

        // move the origin of drawing, so that we don't have to add smileX+ and smileY+ to every drawing call below. 
        g2.translate(smileX, smileY);        

        // fill the face with yellow
        g2.setColor(faceColor);
        g2.fill(new Rectangle2D.Double(0, 0, SMILE_WIDTH, SMILE_HEIGHT));

        // use a black pen
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(SMILE_STROKE_WIDTH));
        
        // draw the smile -- an arc inscribed in the SMILE_WIDTH x SMILE_HEIGHT box
        final int startingSmileAngle = -30; // south east
        final int endingSmileAngle = -120; // south west
        g2.draw(new Arc2D.Double(0, 0, SMILE_WIDTH, SMILE_HEIGHT, startingSmileAngle, endingSmileAngle, Arc2D.OPEN));
        
        // draw some eyes to make it look like a smile rather than an arc
        for (int side: new int[] { -1, 1 }) {
            g2.fill(new Ellipse2D.Double(SMILE_WIDTH/2 + side * EYE_OFFSET - EYE_SIZE/2,
                                         SMILE_HEIGHT/2 - EYE_OFFSET - EYE_SIZE/2,
                                         EYE_SIZE,
                                         EYE_SIZE));
        }
    }

}
