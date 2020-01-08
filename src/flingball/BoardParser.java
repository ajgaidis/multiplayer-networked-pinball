package flingball;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
import physics.Angle;
import physics.Vect;

/**
 * Parser for the Flingball board grammar.
 */
public class BoardParser {

    // the nonterminals of the grammar
    private enum FlingballGrammar {
        COMMENT,
        BALL, 
        BOARD, 
        BUMPER, 
        ABSORBER,
        FLIPPER,
        FLIPPERNAME,
        PORTAL,
        FIRE, 
        INTEGER,
        KEYACTION,
        UPORDOWN,
        KEY,
        FLOAT, 
        NAME, 
        ITEM, 
        BUMPERNAME, 
        WHITESPACE, 
        BOARDATTRIBUTE, 
        BOARDATTRIBUTENAME
    }

    private static final Parser<FlingballGrammar> PARSER = makeParser();
    private static final List<Gadget> GADGETS = new ArrayList<>();
    private static final List<Absorber> ABSORBERS = new ArrayList<>();
    private static final List<Flipper> FLIPPERS = new ArrayList<>();
    private static final List<String> PROTO_TRIGGERS = new ArrayList<>();
    
    private static final double ANGULAR_VELOCITY_DEGREES = 1080;
    private static final double HALF_CIRCLE = 180;
    private static final double ANGULAR_VELOCITY = ANGULAR_VELOCITY_DEGREES * Math.PI/HALF_CIRCLE;
    
    private static final int PARSE_TREE_FIRST_ELEM = 0;
    private static final int PARSE_TREE_SECOND_ELEM = 1;
    private static final int PARSE_TREE_THIRD_ELEM = 2;
    private static final int PARSE_TREE_FOURTH_ELEM = 3;
    private static final int PARSE_TREE_FIFTH_ELEM = 4;

    // Abstraction Function
    //  AF(parser, gadgets, absorbers, flippers, protoTriggers, ANGULAR_VELOCITY_DEGREES, ANGULAR_VELOCITY) = 
    //                          A parser that interprets files that represent flingball game boards.
    //                          parser is the built parser that interprets board files and populates
    //                          gadgets, absorbers, and flippers which are the specific gadgets that
    //                          are on the created flingball board. protoTriggers are the keys that
    //                          could be pressed in order to trigger an event on the flingball board.
    //                          ANGULAR_VELOCITY_DEGREES and ANGULAR_VELOCITY are the velocity with 
    //                          which the flippers on the flingball board rotate. 
    //                          
    // Representation Invariant
    //  --| true
    // Safety from Representation Exposure
    //  --| all fields are private, final and are never returned to the client
    // Thread Safety Argument
    //  --| The instances created of BoardParser are completely confined to themselves
    
    /**
     * Compile the grammar into a parser
     *
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has exceptions
     */
    private static Parser<FlingballGrammar> makeParser() {
        try {
            final File grammarFile = new File("src/flingball/Board.g");
            return Parser.compile(grammarFile, FlingballGrammar.ITEM);
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("can't read the grammar file", e);
        }
    }

    /**
     * Parse a string into a gadget according to the grammar.
     * 
     * @param file board file to parse
     * @return Board parsed from the string
     * @throws UnableToParseException if the string doesn't match the Flingball
     *         grammar
     */
    public static Board parse(final File file) throws UnableToParseException {
        final Scanner scan;
        try {
            scan = new Scanner(file);
            scan.useDelimiter("\\r?\\n");
            Board board = new Board("default");    

            while (scan.hasNext()) {
                final ParseTree<FlingballGrammar> parseTree = PARSER.parse(scan.next());
                board = makeAbstractSyntaxTree(parseTree, board);
            }
            scan.close();
            return board;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find file");
        }
    }
    
    /**
     * Finds first terminal value of a parse tree
     * @param parseTree the parse tree to examine
     * @return String terminal value
     */
    private static String findValue(ParseTree<FlingballGrammar> parseTree) {
        if (parseTree.children().isEmpty()) {
            return parseTree.text();
        } else {
            return findValue(parseTree.children().get(0));
        }
    }

    /**
     * Convert a parse tree into the corresponding abstract syntax tree.
     *
     * @param parseTree constructed according to the Flingball grammar
     * @param board the board being constructed
     * @return Board board created by parseTree
     */
    private static Board makeAbstractSyntaxTree(final ParseTree<FlingballGrammar> parseTree, Board board) {
        switch (parseTree.name()) {
        case ITEM: // (board|comment|bumper|absorber|fire|ball)?
        {
            if (!parseTree.children().isEmpty()) {
            final ParseTree<FlingballGrammar> child = parseTree.children().get(0);
            return makeAbstractSyntaxTree(child, board);
            }
            return board;
        }
        case BUMPER: // bumperName 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('orientation='
                     // ('0'|'90'|'180'|'270'))?;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String bumperName = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final String name = findValue(children.get(PARSE_TREE_SECOND_ELEM));
            final Integer x = Integer.parseInt(findValue(children.get(PARSE_TREE_THIRD_ELEM)));
            final Integer y = Integer.parseInt(findValue(children.get(PARSE_TREE_FOURTH_ELEM)));
            switch (bumperName) {
            case "squareBumper": {
                final SquareBumper squareBumper = new SquareBumper(name,new Vect(x,y));
                GADGETS.add(squareBumper);
                board.addBumper(squareBumper);
                break;
            }
            case "triangleBumper": {
                Angle orientation = new Angle(0);
                final int sizeWithOrientation = 5;
                if (children.size() == sizeWithOrientation) {
                    final String exactOrientation = findValue(children.get(PARSE_TREE_FIFTH_ELEM));
                    switch (exactOrientation) {
                    case "90":
                        orientation = Angle.DEG_90;
                        break;
                    case "180":
                        orientation = Angle.DEG_180;
                        break;
                    case "270":
                        orientation = Angle.DEG_270;
                        break;
                    default:
                        orientation = new Angle(0);
                        break;
                    }
                }
                
                final TriangleBumper triangleBumper = new TriangleBumper(name,new Vect(x,y),orientation);
                GADGETS.add(triangleBumper);
                board.addBumper(triangleBumper);
                break;
            }
            case "circleBumper": {
                final CircleBumper circleBumper = new CircleBumper(name, new Vect(x,y));
                GADGETS.add(circleBumper);
                board.addBumper(circleBumper);
                break;
            }
            default:
                throw new AssertionError("should never get here default end: " + bumperName);
            }
            break;
        }
        case BOARD: // 'board' ('name=' NAME)? ('gravity' '=' FLOAT)? ('friction1' '=' FLOAT)?
                    // ('friction2' '=' FLOAT)?;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            for (int i = 0; i < children.size(); i++) {
                final ParseTree<FlingballGrammar> boardAttribute = children.get(i);
                switch (boardAttribute.children().get(0).text().trim()) {
                case "name": {
                    board.setName(boardAttribute.children().get(1).text());
                    break;
                }
                case "gravity": {
                    board.setGravity(Float.parseFloat(boardAttribute.children().get(1).text()));
                    break;
                }
                case "friction1": {
                    board.setFriction1(Float.parseFloat(boardAttribute.children().get(1).text()));
                    break;
                }
                case "friction2": {
                    board.setFriction2(Float.parseFloat(boardAttribute.children().get(1).text()));
                    break;
                }
                }
            }
            break;
        }
        case ABSORBER: // 'absorber' 'name=' NAME 'x=' INTEGER 'y=' INTEGER 'width=' INTEGER 'height='
                       // INTEGER;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String name = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final Integer x = Integer.parseInt(findValue(children.get(PARSE_TREE_SECOND_ELEM)));
            final Integer y = Integer.parseInt(findValue(children.get(PARSE_TREE_THIRD_ELEM)));
            final Integer width = Integer.parseInt(findValue(children.get(PARSE_TREE_FOURTH_ELEM)));
            final Integer height = Integer.parseInt(findValue(children.get(PARSE_TREE_FIFTH_ELEM)));
            final Absorber absorber = new Absorber(name,new Vect(x,y), new Vect(width,height));
            GADGETS.add(absorber);
            ABSORBERS.add(absorber);
            board.addAbsorber(absorber);
            break;
        }
        case BALL: // 'ball' 'name=' NAME 'x=' FLOAT 'y=' FLOAT 'xVelocity=' FLOAT 'yVelocity='
                   // FLOAT;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String name = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final Float x = Float.parseFloat(findValue(children.get(PARSE_TREE_SECOND_ELEM)));
            final Float y = Float.parseFloat(findValue(children.get(PARSE_TREE_THIRD_ELEM)));
            final Float xVelocity = Float.parseFloat(findValue(children.get(PARSE_TREE_FOURTH_ELEM)));
            final Float yVelocity = Float.parseFloat(findValue(children.get(PARSE_TREE_FIFTH_ELEM)));
            board.addBall(new Ball(name,new Vect(x, y), new Vect(xVelocity,yVelocity)));
            break;
        }
        
        case FIRE: //'fire' 'trigger=' NAME 'action=' NAME;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String triggerName = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final String actionName = findValue(children.get(PARSE_TREE_SECOND_ELEM));
            
            Gadget actionGadget = null;
            Gadget trigger = null;
            
            try {
                actionGadget = board.getGadgetByName(actionName);
                trigger = board.getGadgetByName(triggerName);
                if (ABSORBERS.contains(actionGadget)) {
                    final Absorber action = (Absorber) actionGadget;
                    board.setTarget(action, trigger);
                }
                else if (FLIPPERS.contains(actionGadget)) {
                    final Flipper action = (Flipper) actionGadget;
                    board.setTarget(action, trigger);
                }
            }
            catch (IllegalArgumentException e) {
                PROTO_TRIGGERS.add(triggerName + " " + actionName);
            }
            
            break;
        }
        case FLIPPER: // 'leftFlipper' 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('orientation=' INTEGER)?;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String flipperName = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final String name = findValue(children.get(PARSE_TREE_SECOND_ELEM));
            final Integer x = Integer.parseInt(findValue(children.get(PARSE_TREE_THIRD_ELEM)));
            final Integer y = Integer.parseInt(findValue(children.get(PARSE_TREE_FOURTH_ELEM)));
            
            Angle orientation = new Angle(0);
            final int sizeWithOrientation = 5;
            if (children.size() == sizeWithOrientation) {
                final String exactOrientation = findValue(children.get(PARSE_TREE_FIFTH_ELEM));
                switch (exactOrientation) {
                case "90":
                    orientation = Angle.DEG_90;
                    break;
                case "180":
                    orientation = Angle.DEG_180;
                    break;
                case "270":
                    orientation = Angle.DEG_270;
                    break;
                default:
                    orientation = new Angle(0);
                    break;
                }
            }
            
            switch(flipperName) {
            case ("leftFlipper"):
            {
                final Flipper leftFlipper = new Flipper(false, name, new Vect(x, y), orientation, new Angle(0), false, ANGULAR_VELOCITY);
                GADGETS.add(leftFlipper);
                FLIPPERS.add(leftFlipper);
                board.addFlipper(leftFlipper);
                break;
            }
            case ("rightFlipper"):
            {
                final Flipper rightFlipper = new Flipper(true, name, new Vect(x, y), orientation, new Angle(0), false, -ANGULAR_VELOCITY);
                GADGETS.add(rightFlipper);
                FLIPPERS.add(rightFlipper);
                board.addFlipper(rightFlipper);
                break;
            }
            default:
                throw new AssertionError("should never get here default end: " + flipperName);
            }
            break;
        }
        case PORTAL: // 'portal' 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('otherBoard=' NAME)? 'otherPortal=' NAME;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String name = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final Integer x = Integer.parseInt(findValue(children.get(PARSE_TREE_SECOND_ELEM)));
            final Integer y = Integer.parseInt(findValue(children.get(PARSE_TREE_THIRD_ELEM)));
            Optional<String> otherBoard = Optional.empty();
            final String otherPortal;
            final int sizeWithoutOtherBoard = 4;
            if (children.size() == sizeWithoutOtherBoard) {
                otherPortal = findValue(children.get(PARSE_TREE_FOURTH_ELEM));
            } else {
                otherBoard = Optional.of(findValue(children.get(PARSE_TREE_FOURTH_ELEM)));
                otherPortal = findValue(children.get(PARSE_TREE_FIFTH_ELEM));
            }
            final Portal portal = new Portal(name, new Vect(x, y), otherBoard, otherPortal);
            GADGETS.add(portal);
            board.addPortal(portal);
            if ((otherBoard.isPresent() && otherBoard.get().equals(board.getName())) || !otherBoard.isPresent()) {
                board.addLocalPortal(portal);
            }
            break;
        }
        case KEYACTION: // upOrDown 'key='KEY 'action='NAME;
        {
            final List<ParseTree<FlingballGrammar>> children = parseTree.children();
            final String keyUpOrDown = findValue(children.get(PARSE_TREE_FIRST_ELEM));
            final String key = findValue(children.get(PARSE_TREE_SECOND_ELEM));
            final String gadget = findValue(children.get(PARSE_TREE_THIRD_ELEM));
            final String listenerString = keyUpOrDown + " " + key + " " + gadget;
            board.addProtoListener(listenerString);
        }
        case COMMENT: {
            break;
        }
        case WHITESPACE: {
            break;
        }
        default:
            throw new AssertionError("Abstract Syntax Tree shouldn't be called on: " + parseTree.name());
        }
        for (String triggerString : PROTO_TRIGGERS) {
            final String[] splitString = triggerString.split("\\s+");
            final String triggerName = splitString[0];
            final String actionName = splitString[1];
            try {
                final Gadget triggerGadget = board.getGadgetByName(triggerName);
                final Gadget actionGadget = board.getGadgetByName(actionName);
                if (ABSORBERS.contains(actionGadget)) {
                    board.setTarget((Absorber) actionGadget, triggerGadget);
                }
                else {
                    board.setTarget((Flipper) actionGadget, triggerGadget); 
                }
            }
            catch (IllegalArgumentException e) {
                continue;
            }
        }
        return board;
    }
}
