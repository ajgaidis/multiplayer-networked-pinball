package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.Circle;
import physics.Physics;
import physics.Vect;

/**
 * An immutable, threadsafe ball in the flingball game.
 */
public class Ball {
    private final String name;
    private final Circle ball;
    private final Vect velocity;
    private final Vect location;
    
    private static final Color COLOR = Color.BLACK;
    public static final double DIAMETER = 0.5;
    public static final double RADIUS = 0.25;

    //  AF(name, ball, velocity, location, COLOR, DIAMETER, RADIUS) = 
    //              A 2D image of a circle, ball, whose name is name. This ball's center is located
    //              at coordinates (x, y), where (x, y) are floats between 0.25 and 19.75 inclusive that
    //              determine where the ball is on a rendered image. The ball travels around a flingball
    //              board at a velocity given by velocity's (x, y) components which specify the velocity
    //              in those directions as a unit vector. The appearance of the ball is given by COLOR
    //              which specifies the color of the ball, DIAMETER which is the diameter of the circle,
    //              ball, that represents this flingball ball, and RADIUS which is the radius of the
    //              of the circle, ball, that represents this flingball ball.
    // Representation Invariant 
    //  --| location must be, [0.25, 19.75]
    //  --| velocity must be: -500 <= velocity <= 500
    //  --| other things that would break representation exposure (incorrect names) fail faster in
    //      other classes such as BoardParser
    // Safety from Representation Exposure
    //  --| all fields are private and final
    //  --| all methods return immutable types
    // Thread Safety Argument
    //  --| This class satisfies the strongest definition of immutability specified in reading 20

    /**
     *  Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private void checkRep() {
        // sanity checks :)
        assert name != null;
        assert location != null;
        assert velocity != null;
        assert ball != null;

        final double lowerBallLocationBound = 0; // values account for floating point imprecision
        final double upperBallLocationBound = 20;
        assert lowerBallLocationBound < location.x() && location.x() < upperBallLocationBound;
        assert lowerBallLocationBound < location.y() && location.y() < upperBallLocationBound;
        
        final int maxVelocity = 500;
        final int minVelocity = -500;
        assert minVelocity <= velocity.x() && velocity.x() <= maxVelocity;
        assert minVelocity <= velocity.y() && velocity.y() <= maxVelocity;
    }

    
    /**
     * Construct a ball with given name, location, and velocity.
     * 
     * @param name the name of the ball
     * @param location is the center of the ball on the Board in L
     * @param velocity the velocity of the Ball in L/sec
     */
    Ball(String name, Vect location, Vect velocity){
        this.name = name;
        this.ball = new Circle(location, RADIUS);
        this.velocity = velocity;
        this.location = location;
        checkRep();
    }
    
    
    /**
     * @return the Circle object of the Ball
     */
    Circle getCircle() {
        return ball;
    }
    
    /**
     * @return the velocity of the Ball
     */
    Vect getVelocity(){
        return velocity;
    }

    /**
     * @return location of of the Ball's center
     */
    public Vect getLocation() {
        return location;
    }
    
    /**
     * Draws this ball.
     * 
     * @param g Graphics the 2D render on which to draw this ball.
     */
    public void draw(Graphics g) {        
        g.setColor(COLOR);
        g.fillOval((int)((this.location.x()-RADIUS) * Board.PIXELS_PER_L), 
                   (int)((this.location.y()-RADIUS) * Board.PIXELS_PER_L), 
                   (int)(Ball.DIAMETER * Board.PIXELS_PER_L), 
                   (int)(Ball.DIAMETER * Board.PIXELS_PER_L));
        checkRep();
    }

    /**
     * @return the given name of the Ball
     */
    public String getName() {
        return name;
    }
    
    /**
     * Updates this ball's velocity.
     * 
     * @param delta the time step by which to update the ball's velocity. For example, if the time
     *              step is 1 second then the function will return the ball's velocity 1 second 
     *              after this function is called. 
     * @param gravity the gravity constant of the flingball board that this ball is currently on
     * @param mu1 the first friction constant of the fingball board that this ball is currently on
     * @param mu2 the second friction constant of the flingball board that this ball is currently on
     * @return a new Ball that is the same as this ball but with an updated velocity according to
     *         the proceedings of this ball during time step delta
     */
    public Ball updateVelocity(double delta, double gravity, double mu1, double mu2) {
        final Vect velocityFriction = new Vect(this.velocity.angle(), 
                                         Math.max(this.velocity.length() * 
                                                 (1-mu1*delta-mu2*this.velocity.length()*delta), 0));
        final Vect velocityGravityAndFriction = velocityFriction.plus(new Vect(0, gravity*delta));
        return new Ball(this.name, this.location, velocityGravityAndFriction);
    }
    
    /**
     * Determines whether this ball's dimensions is within another balls dimensions
     * 
     * @param otherBall the ball to check whether or not this ball is inside of
     * @return true if this ball is shares dimensions in the same space as otherBall, else false
     */
    public boolean rejects(Ball otherBall) {
       return Math.pow(Physics.distanceSquared(
               this.getLocation(), otherBall.getLocation()), DIAMETER) <= Ball.DIAMETER;
    }
    
    /**
     * Gets the time until a ball will collide with another ball.
     * 
     * @param collideBall the ball that this ball is estimated to collide with
     * @param delta the time step that is "foresight" or the amount of time the function
     *              will look ahead to see if and when a ball to ball collision will happen
     * @return the time until the next collision between this ball and collideBall if the time
     *         until collision is <= delta then positive infinity is returned as no collision
     *         is estimated to take place
     */
    public double getTimeTillCollision(Ball collideBall, double delta) {
        final double timeTillCollision = Physics.timeUntilBallBallCollision
                (this.getCircle(), this.velocity, collideBall.getCircle(), collideBall.velocity);
        return timeTillCollision <= delta ? timeTillCollision : Double.POSITIVE_INFINITY;
    }

    @Override 
    public String toString() {
        return "Ball: " + name + " @ " + location.toString() + 
                ", Velocity: " + velocity.toString() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() + velocity.hashCode();
    }
    
    @Override 
    public boolean equals(Object obj) {
        if ( ! (obj instanceof Ball)) { return false; }
        final Ball that = (Ball) obj;
        return this.name       .equals(that.name) &&
               this.location   .equals(that.location) &&
               this.velocity   .equals(that.velocity);
    }
    
}
