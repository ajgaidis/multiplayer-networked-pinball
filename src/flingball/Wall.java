package flingball;

/**
 * An immutable, threadsafe datatype that is the enumeration of the walls found in a board.
 * ex.   +---------TOP---------+
 *       |                     |
 *       |                     | 
 *       |                     R
 *       L                     I
 *       E                     G 
 *       F                     H
 *       T                     T
 *       |                     | 
 *       |                     | 
 *       |                     |
 *       +--------BOTTOM-------+
 */
public enum Wall {
        LEFT, 
        RIGHT, 
        TOP, 
        BOTTOM
}
