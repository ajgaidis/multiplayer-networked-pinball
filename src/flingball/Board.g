ITEM ::= (board|comment|bumper|absorber|flipper|portal|fire|keyAction|ball|whitespace)?;

@skip whitespace {
    comment ::= '#' [^\n]*;
    ball ::= 'ball' 'name=' NAME 'x=' FLOAT 'y=' FLOAT 'xVelocity=' FLOAT 'yVelocity=' FLOAT;
    board ::= 'board' boardAttribute*;
    boardAttribute ::= boardAttributeName '=' (FLOAT | NAME);
    boardAttributeName ::= 'gravity' | 'friction1' | 'friction2' | 'name';
    bumper ::= bumperName 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('orientation=' INTEGER)?;
    absorber ::= 'absorber' 'name=' NAME 'x=' INTEGER 'y=' INTEGER 'width=' INTEGER 'height=' INTEGER;
    fire ::= 'fire' 'trigger=' NAME 'action=' NAME;
    flipper ::= flipperName 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('orientation=' INTEGER)?;
    portal ::= 'portal' 'name=' NAME 'x=' INTEGER 'y=' INTEGER ('otherBoard=' NAME)? 'otherPortal=' NAME;
    keyAction ::= upOrDown 'key=' KEY 'action=' NAME;
} 
bumperName ::= 'squareBumper' | 'circleBumper' | 'triangleBumper';
flipperName ::= 'leftFlipper' | 'rightFlipper';
upOrDown ::= 'keyup' | 'keydown';
INTEGER ::= [0-9]+;
FLOAT ::= '-'?([0-9]+'.'[0-9]*|'.'?[0-9]+);
NAME ::= [A-Za-z_][A-Za-z_0-9]*;
KEY ::=   [a-z] | [0-9] | 'shift' | 'ctrl' | 'alt' | 'meta' | 'space' | 'left' | 'right' | 'up' | 'down' | 'minus' | 'equals' | 'backspace' | 'openbracket' | 'closebracket' | 'backslash' | 'semicolon' | 'quote' | 'enter' | 'comma' | 'period' | 'slash';
whitespace ::= [ \t]+;