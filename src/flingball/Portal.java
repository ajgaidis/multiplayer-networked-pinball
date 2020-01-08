package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Optional;

import physics.Circle;
import physics.Physics;
import physics.Vect;

/**
 * A flingball portal. This portal is curcular with diameter 1L and will teleport a ball from one
 * portal gadget to another, possible on a different board on a different machine.
 * 
 * Immutable, Threadsafe Datatype
 */
public class Portal implements Gadget {
    
    private final String name;
    private final Vect location;
    private final Optional<String> otherBoard;
    private final String otherPortal;
    private final Circle circle;
    
    private static final Color COLOR = Color.BLUE;
    private static final int DIAMETER = 1;
    private static final double RADIUS = 0.5;
    
    // Abstraction Function:
    //  AF(name, location, otherBoard, otherPortal, circle, COLOR, DIAMETER, RADIUS) = A portal that is a
    //           circular object identified by name. Its physical appearance has color Color, a
    //           diameter specified DAMETER, and a radius specified by RADIUS. Additionally, a portal is
    //           represented as a circular object given by circle. The portal's center
    //           on the flingball board is at location. otherBoard represents the flingball board
    //           that the portal will send a ball to after interaction with the portal. If otherBoard
    //           is an empty optional, it is assumed that the portal will send a ball to another
    //           portal currently on the portal's flingball board. otherPortal represents the portal
    //           to shoot the ball out of after interaction with this portal. In the case that
    //           otherPortal can't be found on the portal's board or otherBoard, a ball just passes
    //           over the portal without reflection or absorption.           
    // Representation Invariant:
    //  --| no fields can be null
    //  --| location must be integers, [0, 19]
    // Safety From Representation Exposure:
    //  --| all fields are private and final
    //  --| class is immutable and the only return types are primitives
    // Thread Safety Argument:
    //  --| class satisfy the strongest form immutability and does not have any beneficent mutation
    //  --| all fields are private and final
    
    /**
     * Verifies that the representation invariant is not broken
     */
    private void checkRep() {
        assert name != null;
        assert location != null;
        assert circle != null;
        assert otherBoard != null;
        assert otherPortal != null;
        
        assert (int) 0 <= location.x() && location.x() <= Board.L - 1;
        assert (int) 0 <= location.y() && location.y() <= Board.L - 1;
    }

    /**
     * Creates a new flingball portal that specifies a board for this portal to connect to.
     * 
     * @param name the name that identifies the portal
     * @param location the location of the portal's top left corner
     * @param otherBoard the board that a portal is connected to.
     * @param otherPortal the portal this portal connects to. The connected portal can reside
     *                    either on the board of this portal or on a separate board. 
     */
    public Portal(String name, Vect location, Optional<String> otherBoard, String otherPortal) {
        this.name = name;
        this.location = location;
        this.otherBoard = otherBoard;
        this.otherPortal = otherPortal;
        this.circle = new Circle(location.plus(new Vect(RADIUS, RADIUS)), RADIUS);
        checkRep();
    }

    @Override
    public Vect getLocation() {
        return location;
    }
    
    /**
     * @return the String that is the name of the portal that this portal is connected to
     */
    public String getConnectedPortal() {
        return this.otherPortal;
    }
    
    /**
     * @return An optional containing the board name that the portal is connected to. If the portal
     *         is not connected to a board, then the empty optional is returned.
     */
    public Optional<String> getConnectedBoard() {
        return this.otherBoard;
    }
    
    /**
     * Returns a new ball that is the ball getting fired out of this portal.
     * 
     * @param ball the ball that is the ball's state prior to getting shot out of the absorber
     * @return a new ball that is leaving the absorber and getting shot out
     */
    protected Ball release(Ball ball) {
        return new Ball(ball.getName(), this.circle.getCenter(), ball.getVelocity());
    }
    
    /**
     * Determines whether a ball is contained inside this portal.
     * 
     * @param ball the ball to test whether it is inside this portal
     * @return true if the ball is contained inside the boundaries of this portal, else false
     */
    public boolean isContained(Ball ball) {
        return (Math.pow(Physics.distanceSquared(ball.getCircle().getCenter(), this.circle.getCenter()), RADIUS) < RADIUS);
    }
    
    @Override
    public double getTimeTillCollision(Ball ball, double delta) {
        if (this.isContained(ball)) {
            return Double.POSITIVE_INFINITY;
        }
        final double timeTillCollision = Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity());
        return timeTillCollision <= delta ? timeTillCollision : Double.POSITIVE_INFINITY;
    }
    
    /**
     * Determines whether newBall intersects with the any of the surface area of this portal
     * 
     * @param newBall the ball to check to see if it intersects with this portal
     * @return true if newBall's dimensions touch any of this portal's surface area
     */
    public boolean intersects(Ball newBall) {
        return Math.pow(Physics.distanceSquared(this.circle.getCenter(), 
                newBall.getCircle().getCenter()), RADIUS) <=  Ball.RADIUS + Portal.RADIUS;                
    }
    
    @Override
    public boolean rejects(Ball ball) {
        /* Portal will never reject anything*/
        return false;
    }

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(COLOR);
        g.fillOval((int)((this.location.x()) * Board.PIXELS_PER_L), 
                   (int)((this.location.y()) * Board.PIXELS_PER_L), 
                   (int)(DIAMETER * Board.PIXELS_PER_L), 
                   (int)(DIAMETER * Board.PIXELS_PER_L));
        checkRep();
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override 
    public String toString() {
        String s = "Portal" + name + " @ " + location;
        s += " Telporting to " + otherPortal;
        if (!otherBoard.equals(Optional.empty())) {
            s += " On board " + otherBoard;
        }
        return s + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() +
               otherBoard.hashCode() + otherPortal.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Portal)) { return false; }
        final Portal that = (Portal) obj;
        return this.name.equals(that.name) && 
               this.location.equals(that.location) &&
               this.otherBoard.equals(that.otherBoard) &&
               this.otherPortal.equals(that.otherPortal);
    }  

}
