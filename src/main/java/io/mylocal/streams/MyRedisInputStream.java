package io.mylocal.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyRedisInputStream extends FilterInputStream {

    // todo: remove hardcoding
    private static final int INPUT_BUFFER_SIZE = 8192;

    protected final byte[] buf;

    protected int count, limit;

    public MyRedisInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    public MyRedisInputStream(InputStream in) {
        this(in, INPUT_BUFFER_SIZE);
    }

    public String readLine() throws Exception {
        final StringBuilder sb = new StringBuilder();
        while (true) {
            ensureFill();

            byte b = buf[count++];
            if (b == '\r') {
                ensureFill(); // Must be one more byte

                byte c = buf[count++];
                if (c == '\n') {
                    break;
                }
                sb.append((char) b);
                sb.append((char) c);
            } else {
                sb.append((char) b);
            }
        }

        final String reply = sb.toString();
        System.out.println("reply is: " + reply);
        if (reply.length() == 0) {
            throw new Exception("It seems like server has closed the connection.");
        }

        return reply;
    }

    public int readIntCrLf() throws Exception {
        return (int) readLongCrLf();
    }

    public long readLongCrLf() throws Exception {
        final byte[] buf = this.buf;

        ensureFill();

        final boolean isNeg = buf[count] == '-';
        if (isNeg) {
            ++count;
        }

        long value = 0;
        while (true) {
            ensureFill();

            System.out.println("Count is: " + count);
            final int b = buf[count++];
            if (b == '\r') {
                ensureFill();

                System.out.println("Count is: " + count);
                if (buf[count++] != '\n') {
                    throw new Exception("Unexpected character!");
                }

                break;
            } else {
                value = value * 10 + b - '0';
            }
        }

        return (isNeg ? -value : value);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        ensureFill();

        final int length = Math.min(limit - count, len);
        System.arraycopy(buf, count, b, off, length);
        count += length;
        return length;
    }

    public Byte readByte() {
        System.out.println("inside read byte: " + count );
        ensureFill();
        System.out.println("buff is :" + buf[count]);
        return buf[count++];
    }

    private void ensureFill() {
        if (count >= limit) {
            try {
                System.out.println("inside ensureFill " + count + " " + limit);
                limit = in.read(buf);
                System.out.println("later ensureFill : " + count + " " + limit);
                count = 0;
                if (limit == -1) {
                    throw new Exception("Unexpected end of stream.");
                }
            } catch (Exception e) {
                System.out.println("Error in ensure fill: " + e.getMessage());
            }
        }
    }

}
