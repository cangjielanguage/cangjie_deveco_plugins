/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

parser grammar CJPMTomlParser;

options {
    tokenVocab = CJPMTomlLexer;
}

cjpmtoml : Next_Line* expression (Next_Line+ expression)* EOF;

expression : (keyValue | table) Next_Line*;

keyValue : key Equals value;

key : simpleKey | dottedKey;

simpleKey : quotedKey | unquotedKey;

unquotedKey : Unquoted_Key;

quotedKey : String_Basic | String_Literal;

dottedKey : simpleKey (Dot simpleKey)+;

value : stringVal | integerVal | floatVal | boolVal | arrayVal | inlineTable;

stringVal : String_Basic | MultiLine_String_Basic | String_Literal | MultiLine_String_Literal;

integerVal : Dec_Integer | Hex_Integer | Oct_Integer | Bin_Integer;

floatVal : Float | Infinity | NaN;

boolVal : Boolean;

arrayVal : L_Bracket arrayValues?  R_Bracket;

arrayValues :  value  (Comma  value )*;

table : standardTable | arrayTable;

standardTable : L_Bracket key R_Bracket;

inlineTable : Inline_Table_L_Brace inlineTableKeyvals Inline_Table_R_Brace;

inlineTableKeyvals : inlineTableKeyvalsNonEmpty?;

inlineTableKeyvalsNonEmpty : keyValue (Comma keyValue)*;

arrayTable : Array_Table_L_Bracket key Array_Table_R_Bracket;