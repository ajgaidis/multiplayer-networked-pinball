package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import physics.Angle;
import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;

/**
 * A flingball flipper that flips from one position to another when a specific button is pressed
 * by a user. If a ball hits a flipper while the flipper is moving, the ball will rebound with
 * greater velocity than it hit with. If the flipper is not moving, it acts as a normal bumper.
 * A flipper is rounded on both ends and flat on its sides.
 * 
 * Immutable, Threadsafe Datatype
 */
public class Flipper implements Bumper {

    private final boolean isRightFlipper;
    private final String name;
    private final Vect location;
    private final Angle orientation;
    private final Angle rotation;
    private final boolean isFlipping;
    
    private final LineSegment line;
    private final Vect pivot;
    private final List<Circle> circles; // lists contain the elements that make up the flipper
    private final double angularVelocity;
    
    private static final Color COLOR = Color.BLUE;
    private static final double STATIC_COEFF = 0.95;
    private static final double LENGTH = 2;
    
    // Abstraction Function:
    //  AF(isRightFlipper, name, location, orientation, rotation, isFlipping, line, circles, pivot, 
    //      angularVelocity, COLOR, LENGTH) = A flipper is identified by name. Physically, a flipper 
    //                  has color COLOR, a location on a flingball board given by location, an 
    //                  orientation orientation detailing how much the flipper is rotated about its 
    //                  pivot, a rotation detailing how far (in terms of an Angle) the flipper is through
    //                  its rotation. isRightFlipper tells whether or not the flipper is a right flipper 
    //                  or left flipper which described where it lays in its 2L x 2L square. If right, 
    //                  when vertical lays on the right half of the 2L x 2L square. When left, it lays
    //                  on the left half of the 2L x 2L square. The length of the flipper is given by 
    //                  LENGTH and is 2L line. circles contains all the pieces that make up the border
    //                  or outline shape of the flipper. angularVelocity is the speed at which the 
    //                  flipper flips (and direction denoted by +/-. pivot is the point about which 
    //                  the flipper rotates. isFlipping identifies whether the flipper is currently
    //                  moving from one position to another.
    // Representation Invariant:
    //  --| no fields can be null
    //  --| location must be integers, [0, 19]
    //  --| orientation must be either 0, 90, 180, 270 or the radial equivalent
    //  --| rotation must be in [0,90] degrees
    //  --| angular velocity must be +1080 degrees or -1080 degrees
    //  --| circles size must be equal to the number of ends of a line segment (2)
    // Safety From Representation Exposure:
    //  --| all fields are private and final
    //  --| class is immutable and the only return types are primitives or immutable objects
    // Thread Safety Argument:
    //  --| class satisfy the strongest form immutability and does not have any beneficent mutation
    //  --| all fields are private and final
    
    /**
     * Verifies that the representation invariant is not broken
     */
    private void checkRep() {
        assert name != null;
        assert location != null;
        assert orientation != null;
        assert rotation != null;
        assert line != null;
        assert pivot != null;
        assert circles != null;
        
        assert (int) 0 <= location.x() && location.x() <= Board.L;
        assert (int) 0 <= location.y() && location.y() <= Board.L;
        
        assert (orientation.compareTo(Angle.ZERO) == 0) || (orientation.compareTo(Angle.DEG_90) == 0)
        || (orientation.compareTo(Angle.DEG_180) == 0) || (orientation.compareTo(Angle.DEG_270) == 0);
        
        assert Angle.DEG_90.compareTo(rotation) >= 0 && new Angle(0).compareTo(rotation) <= 0;
        
        final double angularVelocityDegrees = 1080;
        final double delta = 0.01; // allow the velocity to be reasonably close but different
        
        assert Math.abs(angularVelocity - Math.toRadians(angularVelocityDegrees)) <= delta ||
               Math.abs(angularVelocity - Math.toRadians(-angularVelocityDegrees)) <= delta;
        
        assert circles.size() == 2;
    }
    
    /**
     * Creates a new flingball Flipper
     * 
     * @param isRightFlipper whether or not the flipper is a right flipper (true) or a left
     *                       flipper (false). A right flipper, at rest, sits vertically on 
     *                       the right-hand side of a 2L. When triggered it swings up along
     *                       the left quadrant. The opposite is true for the left flipper.
     * @param name the name that specifies this flipper
     * @param location the top-left of the flipper's bounding box.
     * @param orientation the base position that the flipper is in -- can either be rotated 0, 90,
     *                    180 or 270 around the flipper's pivot point as described in the
     *                    location parameter.
     * @param rotation the rotation around the pivot point from the base orientation the flipper is
     *                  must be in the range [0,90] degrees
     * @param isFlipping true if the Flipper is currently in the process of flipping, false otherwise
     * @param angularVelocity the angular velocity with which the flipper will rotate when flipping,
     *             must be either positive or negative 1080 degrees
     */
    public Flipper(boolean isRightFlipper, String name, Vect location, Angle orientation, 
            Angle rotation, boolean isFlipping, double angularVelocity) {
        this.isRightFlipper = isRightFlipper;
        this.name = name;
        this.location = location;
        this.orientation = orientation;
        this.rotation = rotation;
        this.isFlipping = isFlipping;
        this.angularVelocity = angularVelocity;
        
        final LineSegment ls;
        circles = new ArrayList<>();
        
        final Angle rotateDirection;
        if (!isRightFlipper) {
            rotateDirection = new Angle(0).minus(rotation);
        }
        else {
            rotateDirection = rotation;
        }
        
        if (isRightFlipper) {
            if (orientation.compareTo(new Angle(0)) == 0) { // pivot is NE 
                this.pivot = location.plus(new Vect(LENGTH, 0));
                ls = new LineSegment(pivot, location.plus(new Vect(LENGTH, LENGTH)));
            }
            else if (orientation.compareTo(Angle.DEG_90) == 0) { // pivot is SE
                this.pivot = location.plus(new Vect(LENGTH, LENGTH));
                ls = new LineSegment(pivot, location.plus(new Vect(0, LENGTH)));
            }
            else if (orientation.compareTo(Angle.DEG_180) == 0) { // pivot is SW
                this.pivot = location.plus(new Vect(0, LENGTH));
                ls = new LineSegment(pivot, location);
            }
            else { // pivot is NW
                this.pivot = location;
                ls = new LineSegment(pivot, location.plus(new Vect(LENGTH, 0)));
            }
        }
        
        else { // left flipper
            if (orientation.compareTo(new Angle(0)) == 0) { // pivot is NW
                this.pivot = location;
                ls = new LineSegment(pivot, location.plus(new Vect(0, LENGTH)));
            }
            else if (orientation.compareTo(Angle.DEG_90) == 0) { // pivot is SW
                this.pivot = location.plus(new Vect(0,  LENGTH));
                ls = new LineSegment(pivot, location.plus(new Vect(LENGTH, LENGTH)));
            }
            else if (orientation.compareTo(Angle.DEG_180) == 0) { // pivot is SE
                this.pivot = location.plus(new Vect(LENGTH, LENGTH));
                ls = new LineSegment(pivot, location.plus(new Vect(LENGTH, 0)));
            }
            else { // pivot is NE
                this.pivot = location.plus(new Vect(LENGTH, 0));
                ls = new LineSegment(pivot, location);
            }
        }

        this.line = Physics.rotateAround(ls, pivot, rotateDirection);
        circles.add(new Circle(line.p1(), 0));
        circles.add(new Circle(line.p2(), 0));
        
        checkRep();
    }

    @Override
    public Vect getLocation() {
        return location;
    }
    
    /**
     * Creates a new Flipper that is in either an ending static state or in a moving state. This is
     * determined by this Flipper's position and state. For example, if this Flipper is currently
     * in the middle of a starting and ending state, the Flipper generated will be in a transition
     * state. 
     * 
     * @param time the time-step (in seconds) between this Flipper and the creation of a new Flipper
     * @return a new Flipper that is either in a finishing state, i.e. done flipping, or in a
     *         state that is moving
     */
    public Flipper flip(double time) {
        Angle newRotation;
        boolean nowFlipping = true;
        double newVelocity = angularVelocity;
        
        if (!isRightFlipper) {
            newRotation = rotation.plus(new Angle(time * angularVelocity));
        }
        else {
            newRotation = rotation.minus(new Angle(time * angularVelocity));
        }
        if (new Angle(0).compareTo(newRotation) >= 0 || Angle.DEG_90.compareTo(newRotation) <= 0) { // done flipping
            nowFlipping = false;
            newVelocity = -angularVelocity;
            if ((!isRightFlipper && angularVelocity > 0) || (isRightFlipper && angularVelocity < 0)) {
                newRotation = Angle.DEG_90;
            }
            else {
                newRotation = new Angle(0);
            }
        }
        
        return new Flipper(isRightFlipper, name, location, orientation, newRotation, nowFlipping, newVelocity);
    }
    
    /**
     * @return whether this Flipper is in a state of transition between its two endpoint states,
     *         i.e. whether this Flipper is flipping
     */
    public boolean isFlipping() {
        return isFlipping;
    }
    
    @Override
    public Ball getCollisionRedirection(Ball ball) {
        for (int i = 0; i < circles.size(); i++) {
            final Vect newVelocity;
            if (isFlipping) {
                if (Physics.timeUntilRotatingCircleCollision(circles.get(i), location,
                        -angularVelocity, ball.getCircle(), ball.getVelocity()) < Board.EPSILON_16) {
                    newVelocity = Physics.reflectRotatingCircle(circles.get(i), location,
                            -angularVelocity, ball.getCircle(), ball.getVelocity(), STATIC_COEFF);
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            }
            else {
                if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), ball.getVelocity()) < Board.EPSILON_16) {
                    newVelocity = Physics.reflectCircle(circles.get(i).getCenter(), ball.getLocation(),
                            ball.getVelocity());
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            }
        }
        
        final Vect newVelocity;
        if (isFlipping) {
            if (Physics.timeUntilRotatingWallCollision(line, pivot, -angularVelocity, ball.getCircle(),
                  ball.getVelocity()) < Board.EPSILON_12) {
                    newVelocity = Physics.reflectRotatingWall(line, pivot, -angularVelocity,
                            ball.getCircle(), ball.getVelocity(), STATIC_COEFF);
              return new Ball(ball.getName(), ball.getLocation(), newVelocity);
            }
        }
        else {
            if (Physics.timeUntilWallCollision(line, ball.getCircle(), ball.getVelocity()) < Board.EPSILON_12) {
                newVelocity = Physics.reflectWall(line, ball.getVelocity());
                return new Ball(ball.getName(), ball.getLocation(), newVelocity);
            }
        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }
    
    @Override
    public boolean rejects(Ball ball) {
        return getTimeTillCollision(ball, Board.EPSILON_16) <= Board.EPSILON_16;
    }
    
    @Override
    public double getTimeTillCollision(Ball ball, double delta) {
        double minTimeTillCollision = Double.POSITIVE_INFINITY;
        
        if (isFlipping) {
            minTimeTillCollision = Math.min(minTimeTillCollision, Physics.timeUntilRotatingWallCollision(line,
                    pivot, -angularVelocity, ball.getCircle(), ball.getVelocity()));
        }
        else {
            minTimeTillCollision = Math.min(minTimeTillCollision, Physics.timeUntilWallCollision(line, ball.getCircle(), ball.getVelocity()));
        }
        for (int i = 0; i < circles.size(); i++) {
            if (isFlipping) {
                minTimeTillCollision = Math.min(minTimeTillCollision,Physics.timeUntilRotatingCircleCollision(circles.get(i),
                        location, -angularVelocity, ball.getCircle(), ball.getVelocity()));
            }
            else {
                minTimeTillCollision = Math.min(minTimeTillCollision,Physics.timeUntilCircleCollision(circles.get(i),
                        ball.getCircle(), ball.getVelocity()));
            }
        }
        return Math.max(minTimeTillCollision,0) <= delta ? minTimeTillCollision : Double.POSITIVE_INFINITY;
    }

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void draw(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        g2.setColor(COLOR);
        final Vect p1 = line.p1().times(Board.L);
        final Vect p2 = line.p2().times(Board.L);
        final LineSegment lineSegment = new LineSegment(p1, p2);
        g2.draw(lineSegment.toLine2D());
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "Flipper " + name + " is a " + (isRightFlipper ? "right" : "left") + " flipper, pivot @ " + location +
                " orientation= " + orientation +  " radians into rotation= " + rotation.radians() + "\n";
    }

    @Override 
    public int hashCode() {
       final int prime1 = 37;
       final int prime2 = 43;
       final int prime3 = 47;
       final int prime4 = 39;
       final int rightFlipperHash = isRightFlipper ? prime1 : prime2;
       final int flippingHash = isFlipping ? prime3 : prime4;
       return name.hashCode() + location.hashCode() + rightFlipperHash + flippingHash +
               orientation.hashCode() + rotation.hashCode() + line.hashCode() + pivot.hashCode() +
               circles.hashCode() + (int) angularVelocity;
    }
    
    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Flipper)) { return false; }
        final Flipper that = (Flipper) obj;
        return this.name.equals(that.name) && 
               this.location.equals(that.location) &&
               this.isRightFlipper == (that.isRightFlipper) &&
               this.isFlipping == (that.isFlipping) &&
               this.orientation.equals(that.orientation) &&
               this.rotation.equals(that.rotation) &&
               this.line.equals(that.line) &&
               this.pivot.equals(that.pivot) &&
               this.circles.equals(that.circles) &&
               this.angularVelocity == that.angularVelocity;
    }
}
