## Running

* Compile the application

<br/>

    cd path/to/RobotSimulator
    javac JavaRobotSimulator.java

IMPORTANT:The file “grammar” must be adjacent to the compiled application.
* Launch the java Executable.

    java JavaRobotSimulator

* Load the world “sample.world”
* Load the script “sample.program” into either of the robots and click execute to watch them solve the level.


    java JavaRobotSimulator


 *    Load the world "sample.world"

 *   Load the script "sample.program" into either of the robots and
 click execute to watch them solve the level.


## Grammar
 Instead of using a programmed parser for a specific grammar, the program generates a parser on-the-fly
 from a grammar specified in the adjacent text-file "grammar". This allows you to make trivial changes to the scripting
 language in seconds. Specifically in terms of delimiters, brackets, and comments.

 The parser generator can generate a full parser for any deterministic grammar that sticks to its defined format.
 However many values in the supplied grammar are deep-rooted into the system and changing them would result
 in loss of much functionality.

 the grammar is fairly complex and is structured as follows:

    PROGRAM ::= INSTRUCTION_STATEMENT+
    INSTRUCTION_STATEMENT ::= LINE_DELIMITER+
                | if BOOLEAN_EXPR IF_STATEMENT
                | INSTRUCTION DELIMITER
                | // COMMENT+ LINE_DELIMITER
                | ''' MULTILINECOMMENT
    INSTRUCTION ::= move NUMBER_EXPRESSION
                     | backtrack NUMBER_EXPRESSION
                     | turn NUMBER_EXPRESSION
                     | pickup
                     | drop
                        | travel_along_wall
                        | reverse_along_wall
                        | while BOOLEAN_EXPR { INSTRUCTION_STATEMENT* }
                        | declare VARIABLE_NAME = NUMBER
                        | print PRINT_STATEMENT
                        | turn_towards_first_thing
                        | turn_towards_first_box
                        | turn_towards_closest_thing
                        | turn_towards_visible_thing
                        | VARIABLE_NAME ASSIGNMENT


    IF_STATEMENT ::= IF_BODY ELSE_STATEMENT?
    ELSE_STATEMENT ::= else IF_BODY
    IF_BODY ::=  { INSTRUCTION_STATEMENT* } LINE_DELIMITER? | NEW_LINE* INSTRUCTION_STATEMENT
    NEW_LINE ::= [\n\r]
    DELIMITER ::= // COMMENT+ | LINE_DELIMITER
    LINE_DELIMITER ::= [;]+ | NEW_LINE
    ASSIGNMENT ::= | = NUMBER_EXPRESSION
                   | ++
                   | --
                   | += NUMBER_EXPRESSION
                   | -= NUMBER_EXPRESSION
                   | *= NUMBER_EXPRESSION
                   | /= NUMBER_EXPRESSION


    PRINT_STATEMENT ::= STRING | NUMBER_EXPRESSION
    STRING ::= " NO_QUOTE* "
    NO_QUOTE ::= [^\"]*
    COMMENT ::= [^;\n\r]*
    MULTILINECOMMENT ::= '?'?[^']+'?[^']*'?[^']* NEW_LINE* MULTILINECOMMENT NEW_LINE* | '''
    NUMBER_EXPRESSION ::= NUMBER OPERATOR_EXPRESSION*
    OPERATOR_EXPRESSION ::= + NUMBER | - NUMBER | / NUMBER | * NUMBER
    BOOLEAN_EXPR ::= BOOLEAN BOOLEAN_COND*
    BOOLEAN_COND ::= or BOOLEAN_EXPR | and BOOLEAN_EXPR
    BOOLEAN ::= true | false | not BOOLEAN | touching_wall
                | touching_thing
                | touching_box
                | touching_robot
                | thing_is_visible
                | NUMBER_EXPRESSION CONDITIONAL*
    CONDITIONAL ::= COMPARISON NUMBER_EXPRESSION
    COMPARISON ::= < | > | == | !=
    NUMBER ::= INTEGER
               | number_of_things_on_ground
               | VARIABLE_NAME
               | distance_to_first_thing
               | distance_to_closest_thing
               | distance_to_first_box
               | distance_to_visible_thing
    INTEGER ::= [0-9-]+
    WORD ::= [A-Za-z0-9]*
    VARIABLE_NAME ::= [A-Za-z][A-Za-z0-9]*

The language it supports implements many standard features of common scripting languages.


1) Multi-line and Single-line comments

    ''' This is a multi line comment, it starts with 3 (')s
    and ends with 3(') somewhere further down the road. The parser
    will happily accept this
    '''

    x = 1 //here we initialize x (single line comment)

2) Line-delimited and semi-colon delimited

    //semi colons are optional if statements are on separate lines
    x = 1
    y = 2

    //they are not optional if they are on the same line
    x = 1; y = 2

3) Optional curly braces on short if-else statements.
These can be mixed and matched but there can only be one statement following each if/else statement with no braces.

    if x > 3
        nobraces = true
    else{
        nobraces = false
        x = 1 // need braces here as there are 2 statements!
    }

4) Supports increment, decrement, multiplication and division syntax shorthands

    x++ // this works

    x+= 1 + 2 // so does this

    '''
    And all of these
    work too!
    '''
    y /= 4; z *= 10; y--
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
5) Print statements

    x = 10

    print x
    ------> 10

    print "hello world\n"
    ------>"hello world
    "

6) Variable are initialized upon first assignment, no separate declare statements needed.

    print x
    -------> ''

    x = 10

    print x
    ------>'10'

7) Variables aren't restricted to Integers(allows booleans too)
booleans and integers are the same data-type, but treated differently according to context.
e.g


    x = 10
    y = true
    z = 0

    if y
        print "here!"
    ----> "here!"

    if x
        print "still here"
    ---> "still here"

    if z or false
        print "here too?"
    else
        print "0 == false!"
    ---> "0 == false!"

    alive = 10; damage = 2
    while alive{
        alive -= damage
        print alive
    }
    print "dead"
    -----> 10, 8, 6, 4, 2, "dead"

## The World
To make the robot more clever I added a few key abilities.
The key assumption I chose to go along with is that the robot has some form of vision
and can see objects around it, but has no built-in path-finding abilities, so it is up to the scripter
to decide what to do with the things the robot sees.

I gave the robot functions to

* Scan a space for visible objects.
* Look to the end of a wall and travel along-side it.
* Point itself to the next-nearest object.
* Ability to backtrack (undo - stack)

With these three added abilities I was able to have the robot solve a number of fairly complex scenarios
relatively quickly while still using only a very simple script (~ 150 lines with comments)

I also changed the robot speed constants in the World class to speed the simulation up a bit,
as it can take a while to test more complex scenarios with 20+ walls, many things.. etc
