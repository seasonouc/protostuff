//========================================================================
//Copyright 2007-2011 David Yu dyuproject@gmail.com
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

package com.dyuproject.protostuff.me;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO Utilities for graph objects (references and cyclic dependencies).
 *
 * @author David Yu
 * @created Jan 19, 2011
 */
public final class GraphIOUtil
{
    
    private GraphIOUtil() {}
    
    /**
     * Merges the {@code message} with the byte array using the given {@code schema}.
     */
    public static void mergeFrom(byte[] data, Object message, Schema schema)
    {
        mergeFrom(data, 0, data.length, message, schema);
    }
    
    /**
     * Merges the {@code message} with the byte array using the given {@code schema}.
     */
    public static void mergeFrom(byte[] data, int offset, int length, Object message, 
            Schema schema)
    {
        try
        {
            final ByteArrayInput input = new ByteArrayInput(data, offset, length, true);
            final GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
            schema.mergeFrom(graphInput, message);
            input.checkLastTagWas(0);
        }
        catch(ArrayIndexOutOfBoundsException ae)
        {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + 
                    "never happen).");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + 
                    "never happen).");
        }
    }
    
    /**
     * Merges the {@code message} from the {@link InputStream} using 
     * the given {@code schema}.
     */
    public static void mergeFrom(InputStream in, Object message, Schema schema) 
    throws IOException
    {
        final CodedInput input = new CodedInput(in, true);
        final GraphCodedInput graphInput = new GraphCodedInput(input);
        schema.mergeFrom(graphInput, message);
        input.checkLastTagWas(0);
    }
    
    /**
     * Merges the {@code message} from the {@link InputStream} using 
     * the given {@code schema}.
     * 
     * The {@code buffer}'s internal byte array will be used for reading the message.
     */
    public static void mergeFrom(InputStream in, Object message, Schema schema, 
            LinkedBuffer buffer) throws IOException
    {
        final CodedInput input = new CodedInput(in, buffer.buffer, true);
        final GraphCodedInput graphInput = new GraphCodedInput(input);
        schema.mergeFrom(graphInput, message);
        input.checkLastTagWas(0);
    }
    
    /**
     * Merges the {@code message} (delimited) from the {@link InputStream} 
     * using the given {@code schema}.
     */
    public static void mergeDelimitedFrom(InputStream in, Object message, Schema schema) 
    throws IOException
    {
        final int size = in.read();
        if(size == -1)
            throw ProtobufException.truncatedMessage();
        
        final int len = size < 0x80 ? size : CodedInput.readRawVarint32(in, size);
        if(len != 0)
        {
            // not an empty message
            if(len > CodedInput.DEFAULT_BUFFER_SIZE)
            {
                // message too big
                final CodedInput input = new CodedInput(new LimitedInputStream(in, len), 
                        true);
                final GraphCodedInput graphInput = new GraphCodedInput(input);
                schema.mergeFrom(graphInput, message);
                input.checkLastTagWas(0);
                return;
            }
            
            final byte[] buf = new byte[len];
            IOUtil.fillBufferFrom(in, buf, 0, len);
            final ByteArrayInput input = new ByteArrayInput(buf, 0, len, 
                    true);
            final GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
            try
            {
                schema.mergeFrom(graphInput, message);
            }
            catch(ArrayIndexOutOfBoundsException e)
            {
                throw ProtobufException.truncatedMessage(e);
            }
            input.checkLastTagWas(0);
        }
    }
    
    /**
     * Merges the {@code message} (delimited) from the {@link InputStream} 
     * using the given {@code schema}.
     * 
     * The delimited message size must not be larger than the 
     * {@code buffer}'s size/capacity.  
     * {@link ProtobufException} "size limit exceeded" is thrown otherwise.
     */
    public static void mergeDelimitedFrom(InputStream in, Object message, Schema schema, 
            LinkedBuffer buffer) throws IOException
    {
        final int size = in.read();
        if(size == -1)
            throw ProtobufException.truncatedMessage();
        
        final byte[] buf = buffer.buffer;
        
        final int len = size < 0x80 ? size : CodedInput.readRawVarint32(in, size);
        if(len != 0)
        {
            // not an empty message
            if(len > buf.length)
            {
                // size limit exceeded.
                throw new ProtobufException("size limit exceeded. " + 
                        len + " > " + buf.length);
            }
            
            IOUtil.fillBufferFrom(in, buf, 0, len);
            final ByteArrayInput input = new ByteArrayInput(buf, 0, len, 
                    true);
            final GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
            try
            {
                schema.mergeFrom(graphInput, message);
            }
            catch(ArrayIndexOutOfBoundsException e)
            {
                throw ProtobufException.truncatedMessage(e);
            }
            input.checkLastTagWas(0);
        }
    }
    
    /**
     * Used by the code generated messages that implement {@link java.io.Externalizable}.
     * Merges from the {@link DataInput}.
     */
    public static void mergeDelimitedFrom(DataInput in, Object message, Schema schema) 
    throws IOException
    {
        final byte size = in.readByte();
        final int len = 0 == (size & 0x80) ? size : CodedInput.readRawVarint32(in, size);
        
        if(len != 0)
        {
            // not an empty message
            if(len > CodedInput.DEFAULT_BUFFER_SIZE && in instanceof InputStream)
            {
                // message too big
                final CodedInput input = new CodedInput(new LimitedInputStream((InputStream)in, len), 
                        true);
                final GraphCodedInput graphInput = new GraphCodedInput(input);
                schema.mergeFrom(graphInput, message);
                input.checkLastTagWas(0);
            }
            else
            {
                final byte[] buf = new byte[len];
                in.readFully(buf, 0, len);
                final ByteArrayInput input = new ByteArrayInput(buf, 0, len, 
                        true);
                final GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
                try
                {
                    schema.mergeFrom(graphInput, message);
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    throw ProtobufException.truncatedMessage(e);
                }
                input.checkLastTagWas(0);
            }
        }

        // check it since this message is embedded in the DataInput.
        if(!schema.isInitialized(message))
            throw new UninitializedMessageException(message, schema);
    }
    
    /**
     * Serializes the {@code message} into a byte array using the given schema.
     * 
     * @return the byte array containing the data.
     */
    public static byte[] toByteArray(Object message, Schema schema, LinkedBuffer buffer)
    {
        if(buffer.start != buffer.offset)
            throw new IllegalArgumentException("Buffer previously used and had not been reset.");
        
        final ProtostuffOutput output = new ProtostuffOutput(buffer);
        final GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
        try
        {
            schema.writeTo(graphOutput, message);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + 
                    "(should never happen).");
        }
        
        return output.toByteArray();
    }
    
    /**
     * Writes the {@code message} into the {@link LinkedBuffer} using the given schema.
     * 
     * @return the size of the message
     */
    public static int writeTo(LinkedBuffer buffer, Object message, Schema schema)
    {
        if(buffer.start != buffer.offset)
            throw new IllegalArgumentException("Buffer previously used and had not been reset.");
        
        final ProtostuffOutput output = new ProtostuffOutput(buffer);
        final GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
        try
        {
            schema.writeTo(graphOutput, message);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Serializing to a LinkedBuffer threw an IOException " + 
                    "(should never happen).");
        }
        
        return output.getSize();
    }
    
    /**
     * Serializes the {@code message} into an {@link OutputStream} using the given schema.
     * 
     * @return the size of the message
     */
    public static int writeTo(final OutputStream out, final Object message, 
            final Schema schema, final LinkedBuffer buffer) throws IOException
    {
        if(buffer.start != buffer.offset)
            throw new IllegalArgumentException("Buffer previously used and had not been reset.");
        
        final ProtostuffOutput output = new ProtostuffOutput(buffer, out);
        final GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
        schema.writeTo(graphOutput, message);
        LinkedBuffer.writeTo(out, buffer);
        return output.size;
    }
    
    /**
     * Serializes the {@code message}, prefixed with its length, into an 
     * {@link OutputStream}.
     * 
     * @return the size of the message
     */
    public static int writeDelimitedTo(final OutputStream out, final Object message, 
            final Schema schema, final LinkedBuffer buffer) throws IOException
    {
        if(buffer.start != buffer.offset)
            throw new IllegalArgumentException("Buffer previously used and had not been reset.");
        
        final ProtostuffOutput output = new ProtostuffOutput(buffer);
        final GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
        schema.writeTo(graphOutput, message);
        ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
        LinkedBuffer.writeTo(out, buffer);
        return output.size;
    }
    
    /**
     * Used by the code generated messages that implement {@link java.io.Externalizable}.
     * Writes to the {@link DataOutput}.
     * 
     * @return the size of the message.
     */
    public static int writeDelimitedTo(DataOutput out, Object message, Schema schema) 
    throws IOException
    {
        final LinkedBuffer buffer = new LinkedBuffer(LinkedBuffer.MIN_BUFFER_SIZE);
        final ProtostuffOutput output = new ProtostuffOutput(buffer);
        final GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
        schema.writeTo(graphOutput, message);
        ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
        LinkedBuffer.writeTo(out, buffer);
        return output.size;
    }

}
