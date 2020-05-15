package com.hongframe.raft.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-15 21:57
 */
public class Bytes implements Comparable<Bytes> {

    private static final char[] HEX_CHARS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F'};

    private static final ByteArrayComparator BYTES_LEXICO_COMPARATOR = new LexicographicByteArrayComparator();

    private final byte[] bytes;

    // cache the hash code for the string, default to 0
    private int hashCode;

    public static Bytes wrap(byte[] bytes) {
        return new Bytes(bytes);
    }

    /**
     * Create a Bytes using the byte array.
     *
     * @param bytes This array becomes the backing storage for the object.
     */
    public Bytes(byte[] bytes) {
        this.bytes = bytes;

        // initialize hash code to 0
        hashCode = 0;
    }

    /**
     * Get the data from the Bytes.
     *
     * @return The underlying byte array
     */
    public byte[] get() {
        return this.bytes;
    }

    /**
     * The hashcode is cached except for the case where it is computed as 0, in which
     * case we compute the hashcode on every call.
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(bytes);
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        // we intentionally use the function to compute hashcode here
        return this.hashCode() == other.hashCode() && other instanceof Bytes
                && Arrays.equals(this.bytes, ((Bytes) other).get());
    }

    @Override
    public int compareTo(Bytes that) {
        return BYTES_LEXICO_COMPARATOR.compare(this.bytes, that.bytes);
    }

    @Override
    public String toString() {
        return Bytes.toString(bytes, 0, bytes.length);
    }

    /**
     * Write a printable representation of a byte array. Non-printable
     * characters are hex escaped in the format \\x%02X, eg:
     * \x00 \x05 etc.
     * <p>
     * This function is brought from org.apache.hadoop.hbase.util.Bytes
     *
     * @param b   array to write out
     * @param off offset to start at
     * @param len length to write
     * @return string output
     */
    @SuppressWarnings("SameParameterValue")
    private static String toString(final byte[] b, int off, int len) {
        final StringBuilder result = new StringBuilder();

        if (b == null) {
            return result.toString();
        }

        // just in case we are passed a 'len' that is > buffer length...
        if (off >= b.length) {
            return result.toString();
        }

        if (off + len > b.length) {
            len = b.length - off;
        }

        for (int i = off; i < off + len; ++i) {
            final int ch = b[i] & 0xFF;
            if (ch >= ' ' && ch <= '~' && ch != '\\') {
                result.append((char) ch);
            } else {
                result.append("\\x");
                result.append(HEX_CHARS_UPPER[ch / 0x10]);
                result.append(HEX_CHARS_UPPER[ch % 0x10]);
            }
        }
        return result.toString();
    }

    public interface ByteArrayComparator extends Comparator<byte[]>, Serializable {

        int compare(final byte[] buffer1, final int offset1, final int length1, final byte[] buffer2,
                    final int offset2, final int length2);
    }

    private static class LexicographicByteArrayComparator implements ByteArrayComparator {

        private static final long serialVersionUID = -8623342242397267864L;

        @Override
        public int compare(final byte[] buffer1, final byte[] buffer2) {
            return compare(buffer1, 0, buffer1.length, buffer2, 0, buffer2.length);
        }

        @Override
        public int compare(final byte[] buffer1, final int offset1, final int length1, final byte[] buffer2,
                           final int offset2, final int length2) {
            // short circuit equal case
            if (buffer1 == buffer2 && offset1 == offset2 && length1 == length2) {
                return 0;
            }
            // similar to Arrays.compare() but considers offset and length
            final int end1 = offset1 + length1;
            final int end2 = offset2 + length2;
            for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
                int a = buffer1[i] & 0xff;
                int b = buffer2[j] & 0xff;
                if (a != b) {
                    return a - b;
                }
            }
            return length1 - length2;
        }
    }


}
