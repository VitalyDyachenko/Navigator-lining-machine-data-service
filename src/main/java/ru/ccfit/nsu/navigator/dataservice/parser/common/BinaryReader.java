package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.ParserException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public final class BinaryReader {

    private static final Charset CP1251 = Charset.forName("windows-1251");

    private final ByteBuffer buffer;
    private final Function<String, ? extends ParserException> exceptionFactory;

    public BinaryReader(
            byte[] data,
            Function<String, ? extends ParserException> exceptionFactory
    ) {
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.exceptionFactory = exceptionFactory;
    }

    public ParserException fail(String message) {
        return exceptionFactory.apply(message);
    }

    public byte[] readBytes(int count) {
        requireBytes(count);

        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    public void skip(int count) {
        requireBytes(count);
        buffer.position(buffer.position() + count);
    }

    public String readAsciiString(int length) {
        return new String(readBytes(length), StandardCharsets.US_ASCII);
    }

    // Чтение строки фиксированной длины в кодировке Windows-1251 до нулевого байта
    public String readFixedString(int length) {
        byte[] bytes = readBytes(length);

        int end = 0;
        while (end < bytes.length && bytes[end] != 0) {
            end++;
        }

        return new String(bytes, 0, end, CP1251).trim();
    }

    public void requireRecordCount(long count, int recordSize, String recordName) {
        if (count < 0 || count > remaining() / recordSize) {
            throw fail("Invalid " + recordName + " count: " + count);
        }
    }

    private void requireBytes(int count) {
        if (count < 0 || count > buffer.remaining()) {
            throw fail("Cannot read " + count + " bytes at offset " + buffer.position());
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