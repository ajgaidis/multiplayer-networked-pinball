package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.*;

/**
 * An immutable, threadsafe gadget in the flingball game
 */
public interface Gadget {

    /**
     * Get the location of a Gadget on a Board object.
     * 
     * @return the top-left location of the Gadget. For a ball,
     *         the location is the top-left location of the circle's center.
     */
    public Vect getLocation();

    /**
     * Gets the color of the Gadget.
     * 
     * @return the color of the Gadget
     */
    public Color getColor();

    /**
     * Draws a Gadget on a Graphics object.
     * 
     * @param g the 2D Graphics render on which to draw.
     */
    public void draw(Graphics g);

    /**
     * Gets the Gaget's name.
     * 
     * @return return the name of a Gadget.
     */
    public String getName();
    
    /**
     * Gets the time until ball collides with the Gadget 
     * 
     * @param ball the ball to check the time until collision of with the Gadget
     * @param delta the "foresight", or the time step that dictates how far into
     *              future fling ball game play to check for collisions
     * @return the time until a ball will collide with the Gadget
     */
    public double getTimeTillCollision(Ball ball, double delta);
    
    /**
     * Determines whether when trying to add a ball after teleportation the ball's location is
     * invalid because a gadget or other ball is blocking its way, so if it were to teleport it
     * would end up inside the object.
     * 
     * @param ball the ball to test whether or not it can be successfully teleported or gets rejected
     * @return true if the ball is rejected and cannot be teleported, false otherwise
     */
    public boolean rejects(Ball ball);

}
