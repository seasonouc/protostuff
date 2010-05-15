// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!

package com.dyuproject.protostuff.benchmark.generated;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;

import com.dyuproject.protostuff.json.ProtobufConvertor;
import com.dyuproject.protostuff.json.ProtobufJSON;

import com.dyuproject.protostuff.benchmark.V22LiteMedia.Image;
import com.dyuproject.protostuff.benchmark.V22LiteMedia.Media;
import com.dyuproject.protostuff.benchmark.V22LiteMedia.MediaContent;


public final class V22LiteMediaNumericJSON extends ProtobufJSON
{

    public V22LiteMediaNumericJSON()
    {
        super();
    }

    public V22LiteMediaNumericJSON(JsonFactory factory)
    {
        super(factory);
    }

    @SuppressWarnings("unchecked")
    protected <T extends MessageLite, B extends Builder> ProtobufConvertor<T, B> getConvertor(Class<?> messageType)
    {
        
        if(messageType==Image.class)
            return (ProtobufConvertor<T, B>)CONVERTOR_Image;
        
        if(messageType==Media.class)
            return (ProtobufConvertor<T, B>)CONVERTOR_Media;
        
        if(messageType==MediaContent.class)
            return (ProtobufConvertor<T, B>)CONVERTOR_MediaContent;
        
        return null;
    }

    
    static final ProtobufConvertor<Image,Image.Builder> CONVERTOR_Image = new ProtobufConvertor<Image,Image.Builder>()
    {

        public final void generateTo(JsonGenerator generator, Image message) throws IOException
        {
            generator.writeStartObject();
                        
            if(message.hasUri())
                generator.writeStringField("1", message.getUri());
                                    
            if(message.hasTitle())
                generator.writeStringField("2", message.getTitle());
                                    
            if(message.hasWidth())
                generator.writeNumberField("3", message.getWidth());
                                    
            if(message.hasHeight())
                generator.writeNumberField("4", message.getHeight());
                                    
            if(message.hasSize())
                generator.writeNumberField("5", message.getSize().getNumber());
                        
            generator.writeEndObject();
        }

        public final Image.Builder parseFrom(JsonParser parser) throws IOException
        {
            Image.Builder builder = Image.newBuilder();
            mergeFrom(parser, builder);
            return builder;
        }

        public final void mergeFrom(JsonParser parser, Image.Builder builder) throws IOException
        {
            for(JsonToken t = parser.nextToken(); t!=JsonToken.END_OBJECT; t=parser.nextToken())
            {
                if(t!=JsonToken.FIELD_NAME)
                {
                    throw new IOException("Expected token: field_name but was " + 
                            parser.getCurrentToken() + " on message " + 
                            Image.class);
                }
                String name = parser.getCurrentName();
                switch( Integer.parseInt(name) )
                {
                    
                    case 1:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setUri(parser.getText());
                        
                        break;
                    
                    case 2:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setTitle(parser.getText());
                        
                        break;
                    
                    case 3:
                        
                        parser.nextToken();
                        builder.setWidth(parser.getIntValue());
                        
                        break;
                    
                    case 4:
                        
                        parser.nextToken();
                        builder.setHeight(parser.getIntValue());
                        
                        break;
                    
                    case 5:
                        
                        parser.nextToken();
                        builder.setSize(Image.Size.valueOf(parser.getIntValue()));
                        
                        break;
                    
                    default:
                        throw new IOException("Field unknown: " + name + " on message " + Image.class);
                }
            }
        }

    };

    
    static final ProtobufConvertor<Media,Media.Builder> CONVERTOR_Media = new ProtobufConvertor<Media,Media.Builder>()
    {

        public final void generateTo(JsonGenerator generator, Media message) throws IOException
        {
            generator.writeStartObject();
                        
            if(message.hasUri())
                generator.writeStringField("1", message.getUri());
                                    
            if(message.hasTitle())
                generator.writeStringField("2", message.getTitle());
                                    
            if(message.hasWidth())
                generator.writeNumberField("3", message.getWidth());
                                    
            if(message.hasHeight())
                generator.writeNumberField("4", message.getHeight());
                                    
            if(message.hasFormat())
                generator.writeStringField("5", message.getFormat());
                                    
            if(message.hasDuration())
                generator.writeNumberField("6", message.getDuration());
                                    
            if(message.hasSize())
                generator.writeNumberField("7", message.getSize());
                                    
            if(message.hasBitrate())
                generator.writeNumberField("8", message.getBitrate());
                                    
            generator.writeFieldName("9");
            generator.writeStartArray();
            
            for (String t : message.getPersonList())
                generator.writeString(t);
            
            generator.writeEndArray();
                                    
            if(message.hasPlayer())
                generator.writeNumberField("10", message.getPlayer().getNumber());
                                    
            if(message.hasCopyright())
                generator.writeStringField("11", message.getCopyright());
                        
            generator.writeEndObject();
        }

        public final Media.Builder parseFrom(JsonParser parser) throws IOException
        {
            Media.Builder builder = Media.newBuilder();
            mergeFrom(parser, builder);
            return builder;
        }

        public final void mergeFrom(JsonParser parser, Media.Builder builder) throws IOException
        {
            for(JsonToken t = parser.nextToken(); t!=JsonToken.END_OBJECT; t=parser.nextToken())
            {
                if(t!=JsonToken.FIELD_NAME)
                {
                    throw new IOException("Expected token: field_name but was " + 
                            parser.getCurrentToken() + " on message " + 
                            Media.class);
                }
                String name = parser.getCurrentName();
                switch( Integer.parseInt(name) )
                {
                    
                    case 1:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setUri(parser.getText());
                        
                        break;
                    
                    case 2:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setTitle(parser.getText());
                        
                        break;
                    
                    case 3:
                        
                        parser.nextToken();
                        builder.setWidth(parser.getIntValue());
                        
                        break;
                    
                    case 4:
                        
                        parser.nextToken();
                        builder.setHeight(parser.getIntValue());
                        
                        break;
                    
                    case 5:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setFormat(parser.getText());
                        
                        break;
                    
                    case 6:
                        
                        parser.nextToken();
                        builder.setDuration(parser.getLongValue());
                        
                        break;
                    
                    case 7:
                        
                        parser.nextToken();
                        builder.setSize(parser.getLongValue());
                        
                        break;
                    
                    case 8:
                        
                        parser.nextToken();
                        builder.setBitrate(parser.getIntValue());
                        
                        break;
                    
                    case 9:
                                                
                        if(parser.nextToken()!=JsonToken.START_ARRAY)
                        {
                            throw new IOException("Expected token: [ but was " + 
                                    parser.getCurrentToken() + " on message " + 
                                    Media.class);
                        }
                        for(JsonToken t1=parser.nextToken(); t1!=JsonToken.END_ARRAY; t1=parser.nextToken())
                        {
                                                        
                            if(t1 != JsonToken.VALUE_STRING)
                                throw new IOException("Expected token: string but was " + t1);
                            builder.addPerson(parser.getText());
                            
                        }
                        
                        break;
                    
                    case 10:
                        
                        parser.nextToken();
                        builder.setPlayer(Media.Player.valueOf(parser.getIntValue()));
                        
                        break;
                    
                    case 11:
                                                
                        if(parser.nextToken() != JsonToken.VALUE_STRING)
                            throw new IOException("Expected token: string but was " + parser.getCurrentToken());
                        builder.setCopyright(parser.getText());
                        
                        break;
                    
                    default:
                        throw new IOException("Field unknown: " + name + " on message " + Media.class);
                }
            }
        }

    };

    
    static final ProtobufConvertor<MediaContent,MediaContent.Builder> CONVERTOR_MediaContent = new ProtobufConvertor<MediaContent,MediaContent.Builder>()
    {

        public final void generateTo(JsonGenerator generator, MediaContent message) throws IOException
        {
            generator.writeStartObject();
                        
            generator.writeFieldName("1");
            generator.writeStartArray();
            
            for (Image t : message.getImageList())
                CONVERTOR_Image.generateTo(generator, t);
            
            generator.writeEndArray();
                                    
            if (message.hasMedia())
            {
                generator.writeFieldName("2");
                CONVERTOR_Media.generateTo(generator, message.getMedia());
            }
                        
            generator.writeEndObject();
        }

        public final MediaContent.Builder parseFrom(JsonParser parser) throws IOException
        {
            MediaContent.Builder builder = MediaContent.newBuilder();
            mergeFrom(parser, builder);
            return builder;
        }

        public final void mergeFrom(JsonParser parser, MediaContent.Builder builder) throws IOException
        {
            for(JsonToken t = parser.nextToken(); t!=JsonToken.END_OBJECT; t=parser.nextToken())
            {
                if(t!=JsonToken.FIELD_NAME)
                {
                    throw new IOException("Expected token: field_name but was " + 
                            parser.getCurrentToken() + " on message " + 
                            MediaContent.class);
                }
                String name = parser.getCurrentName();
                switch( Integer.parseInt(name) )
                {
                    
                    case 1:
                                                
                        if(parser.nextToken()!=JsonToken.START_ARRAY)
                        {
                            throw new IOException("Expected token: [ but was " + 
                                    parser.getCurrentToken() + " on message " + 
                                    MediaContent.class);
                        }
                        for(JsonToken t1=parser.nextToken(); t1!=JsonToken.END_ARRAY; t1=parser.nextToken())
                        {
                                                        
                            builder.addImage(CONVERTOR_Image.parseFrom(parser));
                            
                        }
                        
                        break;
                    
                    case 2:
                        
                        parser.nextToken();
                        builder.setMedia(CONVERTOR_Media.parseFrom(parser));
                        
                        break;
                    
                    default:
                        throw new IOException("Field unknown: " + name + " on message " + MediaContent.class);
                }
            }
        }

    };

    
}
