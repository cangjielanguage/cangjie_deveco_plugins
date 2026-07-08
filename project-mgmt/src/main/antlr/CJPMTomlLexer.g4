/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

lexer grammar CJPMTomlLexer;

White_Space: [ \t]+ -> channel(HIDDEN);
Next_Line: ('\r'? '\n')+;
Comment: '#' (~[\n])* -> channel(HIDDEN);
L_Bracket: '[';
R_Bracket: ']';
Array_Table_L_Bracket: '[[';
Array_Table_R_Bracket: ']]';
Equals: '=' -> pushMode(Simple_Value_Mode);
Dot: '.';
Comma: ',';

fragment Digit: [0-9];
fragment Alpha_Beta: [A-Za-z];

// strings
fragment Escape: '\\' (["\\/bfnrt] | Unicode | Extra_Unicode);
fragment Unicode: 'u' Hex_Degit Hex_Degit Hex_Degit Hex_Degit;
fragment Extra_Unicode:
    'U' Hex_Degit Hex_Degit Hex_Degit Hex_Degit Hex_Degit Hex_Degit Hex_Degit Hex_Degit
;
String_Basic: '"' (Escape | ~["\\\n])*? '"';
String_Literal: '\'' (~['\n])*? '\'';

Unquoted_Key: (Alpha_Beta | Digit | '-' | '_')+;

mode Simple_Value_Mode;

Value_White_Space: White_Space -> channel(HIDDEN);

Boolean: ('true' | 'false') -> popMode;

fragment MultiLine_Escape: '\\' '\r'? '\n' | Escape;
String_Basic_Val: String_Basic -> type(String_Basic), popMode;
MultiLine_String_Basic: '"""' (MultiLine_Escape | ~["\\])*? '"""' -> popMode;
String_Literal_Val: String_Literal -> type(String_Literal), popMode;
MultiLine_String_Literal: '\'\'\'' (.)*? '\'\'\'' -> popMode;

fragment Exponent: ('e' | 'E') [+-]? Zero_Prefix_Integer;
fragment Zero_Prefix_Integer: Digit (Digit | '_' Digit)*;
fragment Fraction: '.' Zero_Prefix_Integer;
Float: Dec_Integer ( Exponent | Fraction Exponent?) -> popMode;
Infinity: [+-]? 'inf' -> popMode;
NaN: [+-]? 'nan' -> popMode;

fragment Hex_Degit: [A-Fa-f] | Digit;
fragment Digit_1_9: [1-9];
fragment Digit_0_7: [0-7];
fragment Digit_0_1: [0-1];
Dec_Integer: [+-]? (Digit | (Digit_1_9 (Digit | '_' Digit)+)) -> popMode;
Hex_Integer: '0x' Hex_Degit (Hex_Degit | '_' Hex_Degit)* -> popMode;
Oct_Integer: '0o' Digit_0_7 (Digit_0_7 | '_' Digit_0_7)* -> popMode;
Bin_Integer: '0b' Digit_0_1 (Digit_0_1 | '_' Digit_0_1)* -> popMode;

Array_Begin: L_Bracket -> type(L_Bracket), mode(Array_Mode);
Inline_Table_L_Brace: '{' -> mode(Inline_Table_Mod);

// inline table mode
mode Inline_Table_Mod;

// tokens in inline table
Inline_Table_WS: White_Space -> channel(HIDDEN);
Inline_Table_Dot: Dot -> type(Dot);
Inline_Table_Comma: Comma -> type(Comma);
Inline_Table_String_Basic: String_Basic -> type(String_Basic);
Inline_Table_String_Literal: String_Literal -> type(String_Literal);
Inline_Table_Unquoated_Key: Unquoted_Key -> type(Unquoted_Key);
Inline_Table_Equals: Equals -> type(Equals), pushMode(Simple_Value_Mode);

Inline_Table_R_Brace: '}' -> popMode;

// array mode
mode Array_Mode;

Array_Whitespace: White_Space -> channel(HIDDEN);
Array_NextLine: Next_Line -> type(Next_Line);
Array_Comment: Comment -> type(Comment);
Array_Comma: Comma -> type(Comma);

Array_Inline_Table_Begin: Inline_Table_L_Brace -> type(Inline_Table_L_Brace), pushMode(Inline_Table_Mod);
Nested_Array_Begin: L_Bracket -> type(L_Bracket), pushMode(Array_Mode);
Array_End: R_Bracket -> type(R_Bracket), popMode;

// tokens in array
Array_String_Basic: String_Basic -> type(String_Basic);
Array_MultiLine_String_Basic: MultiLine_String_Basic -> type(MultiLine_String_Basic);
Array_Literal_String: String_Literal -> type(String_Literal);
Array_MultiLine_String_Literal: MultiLine_String_Literal -> type(MultiLine_String_Literal);