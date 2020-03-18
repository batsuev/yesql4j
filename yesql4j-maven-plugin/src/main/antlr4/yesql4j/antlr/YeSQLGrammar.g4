grammar YeSQLGrammar;

queries: query* EOF;
query: docsting? name docsting? statement;

statement: line (line | comment)*;
docsting: comment+;

name: WS? COMMENT_MARKER WS? NAME_TAG WS? NON_WS WS? NEWLINE;
comment: WS? COMMENT_MARKER WS? ~NAME_TAG (NON_WS|WS)+ NEWLINE;
line: WS? ~COMMENT_MARKER (NON_WS|WS)* (COMMENT_MARKER (NON_WS|WS)*)? NEWLINE;
emptyline: WS? NEWLINE;

NAME_TAG: 'name:';
COMMENT_MARKER: '--';
NEWLINE: ('\n' | '\r\n')+;
WS: (' ' | '\t')+;
NON_WS: ~(' ' | '\t' | '\n')+;