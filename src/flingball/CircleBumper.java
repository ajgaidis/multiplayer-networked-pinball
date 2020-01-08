package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.Circle;
import physics.Physics;
import physics.Vect;

/**
 * An immutable, threadsafe circle bumper in the Flingball game, an implementation of the Bumper interface
 */
public class CircleBumper implements Bumper {

    private final String name;
    private final Vect location;
    private final Circle circle;

    private static final Color COLOR = Color.PINK;
    private static final int DIAMETER = 1;
    private static final double RADIUS = 0.5;

    // Abstraction Function
    //  AF(name, location, circle, COLOR, DIAMETER, RADIUS) = 
    //             A 2D image of a static circular object that has a label of name.
    //             In a pinball game, this represents a bumper that balls can bounce off. The object's
    //             center is located at coordinates (x, y), where (x, y) are integers between 0 and 19
    //             inclusive that determine the triangle's position on a pinball board. circle contains
    //             the borders of the circular object to check if any balls have hit it. The color of
    //             this image is given by COLOR, the diameter of this circular object is given by
    //             DIAMETER, and radius of the circular object is given by RADIUS.
    // Representation Invariant
    // --| location must be integers, [0, 19]
    // --| other things that would break representation exposure (incorrect names) fail faster in
    //          other classes such as BoardParser
    // Safety from Representation Exposure
    // --| all fields are private and final
    // --| all methods return immutable types
    // Thread Safety Argument
    // --| this class satisfies the strongest definition of immutability specified in reading 20
    // -----| all mutable fields are private, final, and are never modified after the class' creation
    // --| there is no representation exposure in this class --> all methods return immutable types

    /**
     * Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private void checkRep() {
        // sanity checks :)
        assert name != null;
        assert location != null;
        assert circle != null;

        assert (int) 0 <= location.x() && location.x() <= Board.L - 1;
        assert (int) 0 <= location.y() && location.y() <= Board.L - 1;
    }

    /**
     * Construct a circle bumper with given location
     * @param name the name of the circle bumper
     * @param location the location of the "top-left" of the circle if a square were drawn around it
     */
    CircleBumper(String name, Vect location) {
        this.name = name;
        this.location = location;
        this.circle = new Circle(location.plus(new Vect(RADIUS, RADIUS)), RADIUS);

        checkRep();
    }

    @Override 
    public Vect getLocation() {
        return location;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override 
    public Color getColor() {
        return COLOR;
    }

    @Override 
    public Ball getCollisionRedirection(Ball ball) {
        if (Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity()) < Board.TIME) {
            final Vect newVelocity = Physics.reflectCircle(circle.getCenter(), ball.getLocation(), ball.getVelocity());
            return new Ball(ball.getName(), ball.getLocation(), newVelocity);
        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }
    
    @Override
    public double getTimeTillCollision(Ball ball, double delta) {
        final double timeTillCollision = Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity());
        return timeTillCollision <= delta ? timeTillCollision : Double.POSITIVE_INFINITY;
    }
    
    @Override
    public boolean rejects(Ball ball) {
        return Math.pow(Physics.distanceSquared(this.circle.getCenter(), ball.getLocation()), RADIUS) <= (CircleBumper.RADIUS + Ball.RADIUS);
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
    public String toString() {
        return "Circle Bumper: " + name + " @ " + location + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof CircleBumper)) { return false; }
        final CircleBumper that = (CircleBumper) obj;
        return this.name    .equals(that.name) && 
               this.location.equals(that.location);
    }
}
