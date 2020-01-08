package flingball;

/**
 * An immutable, threadsafe bumper in the flingball game.
 */
public interface Bumper extends Gadget {
    
    /**
     * Gets a redirected ball after a collision with a Gadget's boundaries
     * 
     * @param ball the ball pre-Gadget interaction. This Ball and the Gadget that implement
     *             this function will deduce the resulting Ball's velocity. 
     * @return a new Ball object that is the old Ball's interaction with the Gadget
     *         at a given time step. For example, if a Ball hits a bumper at a given angle
     *         and with a given velocity, this will be reflected in the new Ball's velocity.
     */
    public Ball getCollisionRedirection(Ball ball);
    
}
