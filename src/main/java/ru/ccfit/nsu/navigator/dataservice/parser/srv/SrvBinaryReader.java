
package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SrvBinaryReader {

    private final ByteBuffer buffer;

    public SrvBinaryReader(byte[] data) {
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] readBytes(int count) {
        if (count < 0 || count > buffer.remaining()) {
            throw new IllegalArgumentException(
                    "Cannot read " + count + " bytes at offset " + buffer.position()
            );
        }

        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    public int readUnsignedByte() {
        return Byte.toUnsignedInt(buffer.get());
    }

    public int readUnsignedShort() {
        return Short.toUnsignedInt(buffer.getShort());
    }

    public long readUnsignedInt() {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    public int position() {
        return buffer.position();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        return buffer.getInt();
    }
}
