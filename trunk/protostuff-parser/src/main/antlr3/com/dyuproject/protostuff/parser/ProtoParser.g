//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

parser grammar ProtoParser;

options {

    // Default language but name it anyway
    //
    language  = Java;

    // Produce an AST
    //
    output    = AST;

    // Use a superclass to implement all helper
    // methods, instance variables and overrides
    // of ANTLR default methods, such as error
    // handling.
    //
    superClass = AbstractParser;

    // Use the vocabulary generated by the accompanying
    // lexer. Maven knows how to work out the relationship
    // between the lexer and parser and will build the 
    // lexer before the parser. It will also rebuild the
    // parser if the lexer changes.
    //
    tokenVocab = ProtoLexer;
}

// Some imaginary tokens for tree rewrites
//

// What package should the generated source exist in?
//
@header {
    package com.dyuproject.protostuff.parser;
}

parse [Proto proto]
    :   (statement[proto])+ EOF! {
            proto.postParse();
        }
    ;
    
statement [Proto proto]
    :   header_syntax[proto]
    |   header_package[proto]
    |   header_import[proto]
    |   header_option[proto]
    |   message_block[proto, null]
    |   enum_block[proto, null]
    |   extend_block[proto]
    |   service_block[proto]
    ;

header_syntax [Proto proto]
    :   SYNTAX ASSIGN STRING_LITERAL SEMICOLON! {
            if(! "proto2".equals(getStringFromStringLiteral($STRING_LITERAL.text)))
                throw new IllegalStateException("Syntax isn't proto2: '"+
                  getStringFromStringLiteral($STRING_LITERAL.text)+"'");
        }
    ;

header_package [Proto proto]
    :   PKG p=(FULL_ID | ID) SEMICOLON! {
            if(proto.getPackageName() != null)
                throw new IllegalStateException("Multiple package definitions.");
            
            proto.setPackageName($p.text);
        }
    ;
    
header_import [Proto proto]
    :   IMPORT STRING_LITERAL SEMICOLON! {
            proto.importProto(getStringFromStringLiteral($STRING_LITERAL.text));
        }
    ;

header_option [Proto proto]
@init {
    boolean standard = false;
    String value = null;
}
    :   OPTION n=ID ASSIGN (
            v=ID { standard = true; value = $v.text; } 
            | STRING_LITERAL { value = getStringFromStringLiteral($STRING_LITERAL.text); }
        ) SEMICOLON! {
            if(standard)
                proto.standardOptions.put($n.text, value);
            else
                proto.extraOptions.put($n.text, value);
        }
    ;
    
message_block [Proto proto, Message parent]
@init {
    Message message = null;
}
    :   MESSAGE ID { 
            message = new Message($ID.text);
            if(parent==null)
                proto.addMessage(message);
            else
                parent.addNestedMessage(message);
        } 
        LEFTCURLY (message_body[proto, message])* RIGHTCURLY
    ;

message_body [Proto proto, Message message]
    :   message_block[proto, message]
    |   message_field[proto, message]
    |   enum_block[proto, message]
    |   extensions_range[proto, message]
    ;
    
extensions_range [Proto proto, Message message]
    :   EXTENSIONS first=(NUMINT | ID) TO last=(NUMINT | ID) {
            System.err.println("extensions not supported @ line " + $EXTENSIONS.line);
        }
    ;
    
message_field [Proto proto, Message message]
@init {
    Field.Modifier modifier = null;
    FieldHolder fieldHolder = null;
}
    :   (OPTIONAL { modifier = Field.Modifier.OPTIONAL;  } 
        |   REQUIRED { modifier = Field.Modifier.REQUIRED; } 
        |   REPEATED { modifier = Field.Modifier.REPEATED; }) {
            fieldHolder = new FieldHolder();
        }
        field_type[proto, message, fieldHolder] 
        ID ASSIGN NUMINT {
            fieldHolder.field.modifier = modifier;
            fieldHolder.field.name = $ID.text;
            fieldHolder.field.number = Integer.parseInt($NUMINT.text);
        } 
        (field_options[proto, message, fieldHolder.field])? SEMICOLON! {
            message.addField(fieldHolder.field);
        }
    ;
    
field_type [Proto proto, Message message, FieldHolder fieldHolder]
    :   INT32 { fieldHolder.setField(new Field.Int32()); }
    |   UINT32 { fieldHolder.setField(new Field.UInt32()); }
    |   SINT32 { fieldHolder.setField(new Field.SInt32()); }
    |   FIXED32 { fieldHolder.setField(new Field.Fixed32()); }
    |   SFIXED32 { fieldHolder.setField(new Field.SFixed32()); }
    |   INT64 { fieldHolder.setField(new Field.Int64()); }
    |   UINT64 { fieldHolder.setField(new Field.UInt64()); }
    |   SINT64 { fieldHolder.setField(new Field.SInt64()); }
    |   FIXED64 { fieldHolder.setField(new Field.Fixed64()); }
    |   SFIXED64 { fieldHolder.setField(new Field.SFixed64()); }
    |   FLOAT { fieldHolder.setField(new Field.Float()); }
    |   DOUBLE { fieldHolder.setField(new Field.Double()); }
    |   BOOL { fieldHolder.setField(new Field.Bool()); }
    |   STRING { fieldHolder.setField(new Field.String()); }
    |   BYTES { fieldHolder.setField(new Field.Bytes()); }
    |   FULL_ID {
            String fullType = $FULL_ID.text;
            int lastDot = fullType.lastIndexOf('.');
            String packageName = fullType.substring(0, lastDot); 
            String type = fullType.substring(lastDot+1);
            fieldHolder.setField(new Field.Reference(packageName, type, message));
        }
    |   ID { 
            String type = $ID.text;
            fieldHolder.setField(new Field.Reference(null, type, message));
        }
    ;
    
field_options [Proto proto, Message message, Field field]
    :   LEFTSQUARE field_options_keyval[proto, message, field] 
        (COMMA field_options_keyval[proto, message, field])* RIGHTSQUARE
    ;
    
field_options_keyval [Proto proto, Message message, Field field]
    :   key=(DEFAULT|ID) ASSIGN (STRING_LITERAL {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.String)
                    field.defaultValue = getStringFromStringLiteral($STRING_LITERAL.text);
                else if(field instanceof Field.Bytes)
                    field.defaultValue = getBytesFromStringLiteral($STRING_LITERAL.text);
                else
                    throw new IllegalStateException("Invalid string default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        } 
    |   NUMFLOAT {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Float)
                    field.defaultValue = Float.valueOf($NUMFLOAT.text);
                else if(field instanceof Field.Double) 
                    field.defaultValue = Double.valueOf($NUMFLOAT.text);
                else
                    throw new IllegalStateException("Invalid float default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        } 
    |   NUMINT {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Number) {
                    if(field.getClass().getSimpleName().endsWith("32"))
                        field.defaultValue = Integer.valueOf($NUMINT.text);
                    else if(field.getClass().getSimpleName().endsWith("64"))
                        field.defaultValue = Long.valueOf($NUMINT.text);
                    else if(field instanceof Field.Float)
                        field.defaultValue = Float.valueOf($NUMINT.text);
                    else if(field instanceof Field.Double) 
                        field.defaultValue = Double.valueOf($NUMINT.text);
                    else
                        throw new IllegalStateException("Invalid numeric default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
                }
                else
                    throw new IllegalStateException("Invalid numeric default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        }
    |   NUMDOUBLE {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");

                if(field instanceof Field.Float)
                    field.defaultValue = Float.valueOf($NUMDOUBLE.text);
                else if(field instanceof Field.Double) 
                    field.defaultValue = Double.valueOf($NUMDOUBLE.text);
                else
                    throw new IllegalStateException("Invalid numeric default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        }
    |   HEX {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Number) {
                    if(field instanceof Field.Int32)
                        field.defaultValue = new Integer(TextFormat.parseInt32($HEX.text));
                    else if(field instanceof Field.UInt32)
                        field.defaultValue = new Integer(TextFormat.parseUInt32($HEX.text));
                    else if(field instanceof Field.Int64)
                        field.defaultValue = new Long(TextFormat.parseInt64($HEX.text));
                    else if(field instanceof Field.UInt64)
                        field.defaultValue = new Long(TextFormat.parseUInt64($HEX.text));
                    else if(field instanceof Field.Float)
                        field.defaultValue = new Float(Long.decode($HEX.text).floatValue());
                    else if(field instanceof Field.Double) 
                        field.defaultValue = new Double(Long.decode($HEX.text).doubleValue());
                }
                else if(field instanceof Field.Bytes) {
                    field.defaultValue = getBytesFromHexString($HEX.text);
                }
                else
                    throw new IllegalStateException("Invalid numeric default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
                
            }
        }
    |   OCTAL {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Number) {
                    if(field instanceof Field.Int32)
                        field.defaultValue = new Integer(TextFormat.parseInt32($OCTAL.text));
                    else if(field instanceof Field.UInt32)
                        field.defaultValue = new Integer(TextFormat.parseUInt32($OCTAL.text));
                    else if(field instanceof Field.Int64)
                        field.defaultValue = new Long(TextFormat.parseInt64($OCTAL.text));
                    else if(field instanceof Field.UInt64)
                        field.defaultValue = new Long(TextFormat.parseUInt64($OCTAL.text));
                    else if(field instanceof Field.Float)
                        field.defaultValue = new Float(Long.decode($OCTAL.text).floatValue());
                    else if(field instanceof Field.Double) 
                        field.defaultValue = new Double(Long.decode($OCTAL.text).doubleValue());
                }
                else
                    throw new IllegalStateException("Invalid numeric default value for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        }
    |   TRUE {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Bool)
                    field.defaultValue = Boolean.TRUE;
                else
                    throw new IllegalStateException("invalid boolean default value for the non-boolean field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        }    
    |   FALSE {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                if(field instanceof Field.Bool)
                    field.defaultValue = Boolean.FALSE;
                else
                    throw new IllegalStateException("invalid boolean default value for the non-boolean field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        }
    |   val=ID {
            if("default".equals($key.text)) {
                if(field.defaultValue!=null || field.modifier == Field.Modifier.REPEATED)
                    throw new IllegalStateException("a field can only have a single default value");
                
                String refName = $val.text;
                if(field instanceof Field.Reference)
                    field.defaultValue = refName;
                else
                    throw new IllegalStateException("invalid field value '" + refName + "' for the field: " + field.getClass().getSimpleName() + " " + field.name);
            }
        })
    ;
    
enum_block [Proto proto, Message message]
@init {
    EnumGroup enumGroup = null;
}
    :   ENUM ID { enumGroup = new EnumGroup($ID.text); } LEFTCURLY 
        (enum_field[proto, message, enumGroup])* RIGHTCURLY {
            if(message==null)
                proto.addEnumGroup(enumGroup);
            else
                message.addNestedEnumGroup(enumGroup);
        } SEMICOLON?
    ;

enum_field [Proto proto, Message message, EnumGroup enumGroup]
    :   ID ASSIGN NUMINT SEMICOLON! {
            enumGroup.add(new EnumGroup.Value($ID.text, Integer.parseInt($NUMINT.text)));
        }
    ;
    
service_block [Proto proto]
    :   SERVICE ID ignore_block {
            System.err.println("ignoring 'service' at the moment ...");
        }
    ;
    
extend_block [Proto proto]
    :   EXTEND ID ignore_block {
            System.err.println("extensions not supported @ line " + $EXTEND.line);
        }
    ;
    
ignore_block
    :   LEFTCURLY ignore_block_body* RIGHTCURLY
    ;
    
ignore_block_body
    :   (LEFTCURLY)=> ignore_block
    |   ~RIGHTCURLY
    ;
    