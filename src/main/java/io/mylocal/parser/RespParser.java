package io.mylocal.parser;

import io.mylocal.streams.MyRedisInputStream;

import java.util.ArrayList;
import java.util.List;

public class RespParser {

    private final MyRedisInputStream in;

    public static final byte ASTERISK_BYTE = '*';
    public static final byte COLON_BYTE = ':';
    public static final byte COMMA_BYTE = ',';
    public static final byte DOLLAR_BYTE = '$';
    public static final byte EQUAL_BYTE = '=';
    public static final byte GREATER_THAN_BYTE = '>';
    public static final byte HASH_BYTE = '#';
    public static final byte LEFT_BRACE_BYTE = '(';
    public static final byte MINUS_BYTE = '-';
    public static final byte PERCENT_BYTE = '%';
    public static final byte PLUS_BYTE = '+';
    public static final byte TILDE_BYTE = '~';
    public static final byte UNDERSCORE_BYTE = '_';

    public RespParser(MyRedisInputStream in) {
        this.in = in;
    }

    public static Object read(final MyRedisInputStream is) {
        try {
            return process(is);
        }catch (Exception e){
            System.out.println("Error while reading: " + e.getMessage());
        }
        return null;
    }

    private static Object process(final MyRedisInputStream is) throws Exception {
        final byte b = is.readByte();

        return switch (b) {
            case PLUS_BYTE -> is.readLine();
            case DOLLAR_BYTE -> processBulkReply(is);
            case ASTERISK_BYTE -> processMultiBulkReply(is);
            default -> throw new Exception("Unknown reply: " + (char) b);
        };
    }

    private static byte[] processBulkReply(final MyRedisInputStream is) throws Exception {
        final int len = is.readIntCrLf();
        if (len == -1) {
            return null;
        }

        final byte[] read = new byte[len];
        int offset = 0;
        while (offset < len) {
            final int size = is.read(read, offset, (len - offset));
            if (size == -1) {
                throw new Exception("It seems like server has closed the connection.");
            }
            offset += size;
        }

        // read 2 more bytes for the command delimiter
        is.readByte();
        is.readByte();

        return read;
    }

    private static List<Object> processMultiBulkReply(final MyRedisInputStream is) throws Exception {
        // private static List<Object> processMultiBulkReply(final int num, final RedisInputStream is) {
        final int num = is.readIntCrLf();
        if (num == -1) return null;
        final List<Object> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            try {
                ret.add(process(is));
            } catch (Exception e) {
                ret.add(e);
            }
        }
        return ret;
    }


}
