grammar Arduinoml;


/******************
 ** Parser rules **
 ******************/

root            :   signalstuff bricks states declaration EOF;

declaration     :   'application' name=IDENTIFIER;

bricks          :   (sensor|actuator)+;
    sensor      :   'sensor'   location ;
    actuator    :   'actuator' location ;
    location    :   id=IDENTIFIER ':' port=PORT_NUMBER;

states          :   state+;
    state       :   initial? name=IDENTIFIER '{'  action+ transition '}';
    action      :   receiver=IDENTIFIER '<=' value=SIGNAL;
    transition  :   trigger=IDENTIFIER 'is' value=SIGNAL '=>' next=IDENTIFIER | transition2;
    transition2 :   trigger=IDENTIFIER 'is' value=SIGNAL '=>' next=IDENTIFIER value=OPERATOR trigger=IDENTIFIER 'is' value=SIGNAL '=>' next=IDENTIFIER;
    initial     :   '->';

signalstuff     :   'signal' receiver=IDENTIFIER 'on' next=IDENTIFIER;

/*****************
 ** Lexer rules **
 *****************/

PORT_NUMBER     :   [1-9] | '11' | '12';
IDENTIFIER      :   LOWERCASE (LOWERCASE|UPPERCASE)+;
SIGNAL          :   'HIGH' | 'LOW';
OPERATOR        :   'and' | 'or';

/*************
 ** Helpers **
 *************/

fragment LOWERCASE  : [a-z];                                 // abstract rule, does not really exists
fragment UPPERCASE  : [A-Z];
NEWLINE             : ('\r'? '\n' | '\r')+      -> skip;
WS                  : ((' ' | '\t')+)           -> skip;     // who cares about whitespaces?
COMMENT             : '#' ~( '\r' | '\n' )*     -> skip;     // Single line comments, starting with a #
