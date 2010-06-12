//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
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

package com.dyuproject.protostuff;

import static com.dyuproject.protostuff.CodedOutput.LITTLE_ENDIAN_32_SIZE;
import static com.dyuproject.protostuff.CodedOutput.LITTLE_ENDIAN_64_SIZE;
import static com.dyuproject.protostuff.CodedOutput.computeRawVarint32Size;
import static com.dyuproject.protostuff.CodedOutput.computeRawVarint64Size;
import static com.dyuproject.protostuff.CodedOutput.encodeZigZag32;
import static com.dyuproject.protostuff.CodedOutput.getTagAndRawVarInt32Bytes;
import static com.dyuproject.protostuff.CodedOutput.writeRawLittleEndian32;
import static com.dyuproject.protostuff.CodedOutput.writeRawLittleEndian64;
import static com.dyuproject.protostuff.StringSerializer.STRING;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Maintains a decent-sized byte buffer for writing.  If the delimited field's byte-array-value 
 * is too large, it is wrapped by another buffer and linked together.
 *
 * @author David Yu
 * @created May 18, 2010
 */
public final class BufferedOutput implements Output
{

    static final int DEFAULT_BUFFER_SIZE = Integer.getInteger(
            "bufferedoutput.default_buffer_size", 256);
    
    static final int ARRAY_COPY_SIZE_LIMIT = Integer.getInteger(
            "bufferedoutput.array_copy_size_limit", 64);
    
    private final OutputBuffer root;
    private OutputBuffer current;
    private final int bufferSize;
    private int size = 0;
    
    public BufferedOutput()
    {
        this(DEFAULT_BUFFER_SIZE);
    }
    
    public BufferedOutput(int bufferSize)
    {
        current = root = new OutputBuffer(new byte[bufferSize]);
        this.bufferSize = bufferSize;
    }
    
    /**
     * Gets the current size of this output.
     */
    public int getSize()
    {
        return size;
    }
    
    /**
     * Resets this output for re-use.
     */
    public BufferedOutput reset()
    {
        // dereference for gc
        root.next = null;
        // reuse the byte array, offset reset to 0
        root.offset = 0;
        size = 0;
        current = root;
        return this;
    }
    
    /**
     * Writes the raw bytes into the {@link OutputStream}.
     */
    public void streamTo(OutputStream out) throws IOException
    {
        for(OutputBuffer node = root; node != null; node = node.next)
        {
            int len = node.offset - node.start;
            if(len > 0)
                out.write(node.buffer, node.start, len);
        }
    }
    
    /**
     * Returns the data written to this output as a single byte array.
     */
    public byte[] toByteArray()
    {
        int start = 0;
        byte[] buffer = new byte[size];        
        for(OutputBuffer node = root; node != null; node = node.next)
        {
            int len = node.offset - node.start;
            if(len > 0)
            {
                System.arraycopy(node.buffer, node.start, buffer, start, len);
                start += len;
            }
        }
        return buffer;
    }
    
    public void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException
    {
        if(value < 0)
        {
            current = writeTagAndRawVarInt64Bytes(
                    WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                    value, 
                    current);
        }
        else
        {
            current = writeTagAndRawVarInt32Bytes(
                    WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                    value, 
                    current);
        }
    }
    
    public void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                value, 
                current);
    }
    
    public void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                encodeZigZag32(value), 
                current);
    }
    
    public void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32), 
                value, 
                current);
    }
    
    public void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32), 
                value, 
                current);
    }

    public void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                value, 
                current);
    }
    
    public void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                value, 
                current);
    }
    
    public void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                value, 
                current);
    }
    
    public void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64), 
                value, 
                current);
    }
    
    public void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64), 
                value, 
                current);
    }

    public void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32), 
                Float.floatToRawIntBits(value), 
                current);
    }

    public void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException
    {
        current = writeTagAndRawLittleEndian64Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64), 
                Double.doubleToRawLongBits(value), 
                current);
    }

    public void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                value ? 1 : 0, 
                current);
    }

    public void writeEnum(int fieldNumber, int number, boolean repeated) throws IOException
    {
        current = writeTagAndRawVarInt32Bytes(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT), 
                number, 
                current);
    }

    public void writeString(int fieldNumber, String value, boolean repeated) throws IOException
    {
        current = writeTagAndByteArray(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED), 
                STRING.ser(value), 
                current);
    }

    public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException
    {
        writeByteArray(fieldNumber, value.getBytes(), repeated);
    }
    
    public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated) throws IOException
    {
        current = writeTagAndByteArray(
                WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED), 
                bytes, 
                current);
    }

    public <T extends Message<T>> void writeMessage(int fieldNumber, T value, 
            boolean repeated) throws IOException
    {
        writeObject(fieldNumber, value, value.cachedSchema(), repeated);
    }
    
    public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, 
            boolean repeated) throws IOException
    {
        OutputBuffer lastBuffer = current;
        int lastSize = size;
        // view
        lastBuffer.next = current = new OutputBuffer(lastBuffer);
        
        schema.writeTo(this, value);
        
        int msgSize = size - lastSize;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = getTagAndRawVarInt32Bytes(tag, msgSize);
        
        size += delimited.length;
        
        // the first tag of the inner message
        OutputBuffer inner = lastBuffer.next;
        
        // wrap the byte array (delimited) and insert
        OutputBuffer wrap = new OutputBuffer(delimited, lastBuffer);
        wrap.offset = delimited.length;
        wrap.next = inner;
    }
    
    /* ----------------------------------------------------------------- */
    
    /** Returns the output buffer encoded with the tag and byte array */
    private OutputBuffer writeTagAndByteArray(int tag, byte[] value, OutputBuffer ob)
    {
        int valueLen = value.length;
        OutputBuffer rb = writeTagAndRawVarInt32Bytes(tag, valueLen, ob);

        this.size += valueLen;
        
        if(valueLen > ARRAY_COPY_SIZE_LIMIT || rb.offset + valueLen > rb.buffer.length)
        {
            // huge string/byte array.
            OutputBuffer wrap = new OutputBuffer(value, rb);
            wrap.offset = valueLen;
            
            // view
            return (wrap.next = new OutputBuffer(rb));
        }

        System.arraycopy(value, 0, rb.buffer, rb.offset, valueLen);
        
        rb.offset += valueLen;
        
        return rb;
    }

    /** Returns the output buffer encoded with the tag and var int 32 */
    private OutputBuffer writeTagAndRawVarInt32Bytes(int tag, int value, OutputBuffer ob)
    {
        int tagSize = computeRawVarint32Size(tag);
        int size = computeRawVarint32Size(value);
        int totalSize = tagSize + size;
        
        OutputBuffer rb = ob.offset + totalSize > ob.buffer.length ? 
                new OutputBuffer(new byte[bufferSize], ob) : ob;

        byte[] buffer = rb.buffer;
        int offset = rb.offset;
        rb.offset += totalSize;
        this.size += totalSize;
        
        if (tagSize == 1)
            buffer[offset++] = (byte)tag;
        else
        {
            for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7)
                buffer[offset++] = (byte)((tag & 0x7F) | 0x80);

            buffer[offset++] = (byte)tag;
        }

        if (size == 1)
            buffer[offset] = (byte)value;
        else
        {
            for (int i = 0, last = size - 1; i < last; i++, value >>>= 7)
                buffer[offset++] = (byte)((value & 0x7F) | 0x80);

            buffer[offset] = (byte)value;
        }
        
        return rb;
    }

    /** Returns the output buffer encoded with the tag and var int 64 */
    private OutputBuffer writeTagAndRawVarInt64Bytes(int tag, long value, OutputBuffer ob)
    {
        int tagSize = computeRawVarint32Size(tag);
        int size = computeRawVarint64Size(value);
        int totalSize = tagSize + size;
        
        OutputBuffer rb = ob.offset + totalSize > ob.buffer.length ? 
                new OutputBuffer(new byte[bufferSize], ob) : ob;

        byte[] buffer = rb.buffer;
        int offset = rb.offset;
        rb.offset += totalSize;
        this.size += totalSize;
        
        if (tagSize == 1)
            buffer[offset++] = (byte)tag;
        else
        {
            for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7)
                buffer[offset++] = (byte)((tag & 0x7F) | 0x80);

            buffer[offset++] = (byte)tag;
        }

        if (size == 1)
            buffer[offset] = (byte)value;
        else
        {
            for (int i = 0, last = size - 1; i < last; i++, value >>>= 7)
                buffer[offset++] = (byte)(((int)value & 0x7F) | 0x80);

            buffer[offset] = (byte)value;
        }
        
        return rb;
    }
    

    /** Returns the output buffer encoded with the tag and little endian 32 */
    private OutputBuffer writeTagAndRawLittleEndian32Bytes(int tag, int value, OutputBuffer ob)
    {
        int tagSize = computeRawVarint32Size(tag);
        int totalSize = tagSize + LITTLE_ENDIAN_32_SIZE;
        
        OutputBuffer rb = ob.offset + totalSize > ob.buffer.length ? 
                new OutputBuffer(new byte[bufferSize], ob) : ob;

        byte[] buffer = rb.buffer;
        int offset = rb.offset;
        rb.offset += totalSize;
        this.size += totalSize;
        
        if (tagSize == 1)
            buffer[offset++] = (byte)tag;
        else
        {
            for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7)
                buffer[offset++] = (byte)((tag & 0x7F) | 0x80);

            buffer[offset++] = (byte)tag;
        }

        writeRawLittleEndian32(value, buffer, offset);
        
        return rb;
    }

    /** Returns the output buffer encoded with the tag and little endian 64 */
    private OutputBuffer writeTagAndRawLittleEndian64Bytes(int tag, long value, OutputBuffer ob)
    {
        int tagSize = computeRawVarint32Size(tag);
        int totalSize = tagSize + LITTLE_ENDIAN_64_SIZE;
        
        OutputBuffer rb = ob.offset + totalSize > ob.buffer.length ? 
                new OutputBuffer(new byte[bufferSize], ob) : ob;

        byte[] buffer = rb.buffer;
        int offset = rb.offset;
        rb.offset += totalSize;
        this.size += totalSize;

        if (tagSize == 1)
            buffer[offset++] = (byte)tag;
        else
        {
            for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7)
                buffer[offset++] = (byte)((tag & 0x7F) | 0x80);

            buffer[offset++] = (byte)tag;
        }

        writeRawLittleEndian64(value, buffer, offset);

        return rb;
    }

}