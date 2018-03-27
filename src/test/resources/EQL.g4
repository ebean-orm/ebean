grammar EQL;

select_statement
   : select_clause? fetch_clause* where_clause? orderby_clause? limit_clause? EOF
   ;

select_clause
   : 'select' distinct? '(' fetch_property_group ')'
   ;

distinct
   : 'distinct'
   ;

fetch_clause
   : fetch_path
   ;

where_clause
   : 'where' conditional_expression
   ;

orderby_clause
   : 'order' 'by' orderby_property (',' orderby_property)*
   ;

orderby_property
   : PATH_VARIABLE asc_desc? nulls_firstlast?
   ;

nulls_firstlast
   : 'nulls' 'first'
   | 'nulls' 'last'
   ;

asc_desc
   : 'asc'
   | 'desc'
   ;

limit_clause
   : 'limit' NUMBER_LITERAL offset_clause?
   ;

offset_clause
   : 'offset' NUMBER_LITERAL
   ;

fetch_path
   : 'fetch' fetch_option? PATH_VARIABLE fetch_property_set?
   ;

fetch_property_set
   : '(' fetch_property_group ')'
   ;

fetch_property_group
   : fetch_property (',' fetch_property)*
   ;

fetch_property
   : PATH_VARIABLE
   | fetch_query_hint
   | fetch_lazy_hint
   ;

fetch_query_hint
   : '+' fetch_query_option
   ;

fetch_lazy_hint
   : '+' fetch_lazy_option
   ;

fetch_option
   : fetch_query_option
   | fetch_lazy_option
   ;

fetch_query_option
   : 'query' fetch_batch_size?
   ;

fetch_lazy_option
   : 'lazy' fetch_batch_size?
   ;

fetch_batch_size
   : '(' NUMBER_LITERAL ')'
   ;

conditional_expression
   : conditional_term ('or' conditional_term)*
   ;

conditional_term
   : conditional_factor ('and' conditional_factor)*
   ;

conditional_factor
   : 'not'? conditional_primary
   ;

conditional_primary
   : any_expression
   | '(' conditional_expression ')'
   ;

any_expression
   : comparison_expression
   | like_expression
   | between_expression
   | propertyBetween_expression
   | in_expression
   | isNull_expression
   | isNotNull_expression
   | isEmpty_expression
   | isNotEmpty_expression
   | '(' any_expression ')'
   ;

in_expression
   : PATH_VARIABLE 'in' in_value
   ;

in_value
   : INPUT_VARIABLE
   | '(' value_expression (',' value_expression)* ')'
   ;

between_expression
   : PATH_VARIABLE 'between' value_expression 'and' value_expression
   ;

propertyBetween_expression
   :  value_expression 'between' PATH_VARIABLE 'and' PATH_VARIABLE
   ;

isNull_expression
   : PATH_VARIABLE 'is' 'null'
   | PATH_VARIABLE 'isNull'
   ;

isNotNull_expression
   : PATH_VARIABLE 'is' 'not' 'null'
   | PATH_VARIABLE 'isNotNull'
   | PATH_VARIABLE 'notNull'
   ;

isEmpty_expression
   : PATH_VARIABLE 'is' 'empty'
   | PATH_VARIABLE 'isEmpty'
   ;

isNotEmpty_expression
   : PATH_VARIABLE 'is' 'not' 'empty'
   | PATH_VARIABLE 'isNotEmpty'
   | PATH_VARIABLE 'notEmpty'
   ;

like_expression
   : PATH_VARIABLE like_op value_expression
   ;

like_op
   : 'like'       | 'ilike'
   | 'contains'   | 'icontains'
   | 'startsWith' | 'istartsWith'
   | 'endsWith'   | 'iendsWith'
   ;

comparison_expression
   : PATH_VARIABLE comparison_operator value_expression
   | value_expression comparison_operator PATH_VARIABLE
   ;

comparison_operator
   : '='  | 'eq'
   | '>'  | 'gt'
   | '>=' | 'ge' | 'gte'
   | '<'  | 'lt'
   | '<=' | 'le' | 'lte'
   | '<>' | '!=' | 'ne'
   | 'ieq'
   ;

value_expression
   : literal
   | INPUT_VARIABLE
   ;

literal
   : STRING_LITERAL
   | BOOLEAN_LITERAL
   | NUMBER_LITERAL
   ;


INPUT_VARIABLE
   : ':' ('a' .. 'z' | 'A' .. 'Z' | '_') ('a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_')*
   ;

PATH_VARIABLE
   : ('a' .. 'z' | 'A' .. 'Z' | '_') ('a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '.')*
   ;

BOOLEAN_LITERAL
   : 'true'
   | 'false'
   ;

NUMBER_LITERAL
   : '-'? DOUBLE
   | '-'? INT
   | ZERO
   ;

DOUBLE
   : [0-9]+ '.' [0-9]*;

INT
   : [1-9] [0-9]*;

ZERO : '0';

STRING_LITERAL
   : '\'' ( ~'\'' | '\'\'' )* '\''
   ;

WS
   : [ \t\r\n] -> skip
   ;
