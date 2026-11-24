
package ru.ccfit.nsu.navigator.dataservice.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;

public final class SrvBinaryReader {

    private final ByteBuffer buffer;

    public SrvBinaryReader(byte[] data) {
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] readBytes(int count) {
        requireBytes(count);

        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    private void requireBytes(int count) {
        if (count < 0 || count > buffer.remaining()) {
            throw new SrvParsingException(
                    "Cannot read " + count + " bytes at offset " + buffer.position()
            );
        }
    }

    public int readUnsignedByte() {
        requireBytes(Byte.BYTES);
        return Byte.toUnsignedInt(buffer.get());
    }

    public int readUnsignedShort() {
        requireBytes(Short.BYTES);
        return Short.toUnsignedInt(buffer.getShort());
    }

    public long readUnsignedInt() {
        requireBytes(Integer.BYTES);
        return Integer.toUnsignedLong(buffer.getInt());
    }

    public int position() {
        return buffer.position();
    }

    public short readShort() {
        requireBytes(Short.BYTES);
        return buffer.getShort();
    }

    public int readInt() {
        requireBytes(Integer.BYTES);
        return buffer.getInt();
    }

    public double readDouble() {
        requireBytes(Double.BYTES);
        return buffer.getDouble();
    }

    public long readLong() {
        requireBytes(Long.BYTES);
        return buffer.getLong();
    }

    public float readFloat() {
        requireBytes(Float.BYTES);
        return buffer.getFloat();
    }

    public int remaining() {
        return buffer.remaining();
    }
}
