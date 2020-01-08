@skipwhitespace { // Client to Server communication has no 'success'/'failure' precedant
    request ::= (('success' [ ]+)? ('joinHorizontal=' [ ]+ BOARDNAME [ ]+ BOARDNAME) | 
                                    ('joinVertical=' [ ]+ BOARDNAME [ ]+ BOARDNAME) |
                                    ('teleportPortal=' [ ]+ ball [ ]+ PORTALNAME) | 
                                     ('teleportWall=' [ ]+ ball [ ]+ vect [ ]+ WALLNAME) | 
                                     ('connectPortal=' [ ]+ PORTALNAME) | 
                                     ('disconnectPortal=' [ ]+ PORTALNAME) |
                                     ('disconnectWall=' [ ]+ BOARDNAME [ ]+ WALLNAME) | 
                                     ('disconnect') |
                                     ('getClientBoardName') | // used by server to ask the client to send their board name
                                     (BOARDNAME) | // used to tell the server what board is connected to what socket
                                     ('allConnectedBoards=' ([ ]+ BOARDNAME)+)) |
                  'failure';
}
ball ::= BOARDNAME [ ]+ BALLNAME [ ]+ vect; /* NOTE: Portals cannot be named left, right, top, or bottom */
NAME ::= [A-Za-z_][A-Za-z_0-9]*;

BOARDNAME ::= NAME;
BALLNAME ::= NAME;
WALLNAME ::= 'left' | 'right' | 'top' | 'bottom'
PORTALNAME ::= NAME;

vect ::= FLOAT [ ]+ FLOAT;
FLOAT ::= '-'?([0-9]+'.'[0-9]*|'.'?[0-9]+);