grammar YeSQLGrammar;

queries: query* EOF;
query: docstring? name docstring? param* statement;

statement: line (line | comment)*;
docstring: comment+;

name: WS? COMMENT_MARKER WS? NAME_TAG WS? NON_WS WS? NEWLINE;
param: WS? COMMENT_MARKER WS? PARAM_TAG (NON_WS|WS)+ NEWLINE;
comment: WS? COMMENT_MARKER WS? ~(NAME_TAG|PARAM_TAG) (NON_WS|WS)+ NEWLINE;
line: WS? ~COMMENT_MARKER (NON_WS|WS)* (COMMENT_MARKER (NON_WS|WS)*)? NEWLINE;
emptyline: WS? NEWLINE;

NAME_TAG: 'name:';
PARAM_TAG: '@param ';
COMMENT_MARKER: '--';
NEWLINE: ('\n' | '\r\n')+;
WS: (' ' | '\t')+;
NON_WS: ~(' ' | '\t' | '\n')+;