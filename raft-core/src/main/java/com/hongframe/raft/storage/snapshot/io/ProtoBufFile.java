package com.hongframe.raft.storage.snapshot.io;

import com.google.protobuf.Message;
import com.hongframe.raft.util.*;

import java.io.*;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-23 10:47
 */
public class ProtoBufFile {

    static {
        ProtobufMsgFactory.load();
    }

    /** file path */
    private final String path;

    public ProtoBufFile(final String path) {
        this.path = path;
    }

    /**
     * Load a protobuf message from file.
     */
    public <T extends Message> T load() throws IOException {
        File file = new File(this.path);

        if (!file.exists()) {
            return null;
        }

        final byte[] lenBytes = new byte[4];
        try (final FileInputStream fin = new FileInputStream(file);
             final BufferedInputStream input = new BufferedInputStream(fin)) {
            readBytes(lenBytes, input);
            final int len = Bits.getInt(lenBytes, 0);
            if (len <= 0) {
                throw new IOException("Invalid message fullName.");
            }
            final byte[] nameBytes = new byte[len];
            readBytes(nameBytes, input);
            final String name = new String(nameBytes);
            readBytes(lenBytes, input);
            final int msgLen = Bits.getInt(lenBytes, 0);
            final byte[] msgBytes = new byte[msgLen];
            readBytes(msgBytes, input);
            return ProtobufMsgFactory.newMessageByProtoClassName(name, msgBytes);
        }
    }

    private void readBytes(final byte[] bs, final InputStream input) throws IOException {
        int read;
        if ((read = input.read(bs)) != bs.length) {
            throw new IOException("Read error, expects " + bs.length + " bytes, but read " + read);
        }
    }

    /**
     * Save a protobuf message to file.
     *
     * @param msg  protobuf message
     * @param sync if sync flush data to disk
     * @return true if save success
     */
    public boolean save(final Message msg, final boolean sync) throws IOException {
        // Write message into temp file
        final File file = new File(this.path + ".tmp");
        try (final FileOutputStream fOut = new FileOutputStream(file);
             final BufferedOutputStream output = new BufferedOutputStream(fOut)) {
            final byte[] lenBytes = new byte[4];

            // name len + name
            final String fullName = msg.getDescriptorForType().getFullName();
            final int nameLen = fullName.length();
            Bits.putInt(lenBytes, 0, nameLen);
            output.write(lenBytes);
            output.write(fullName.getBytes());
            // msg len + msg
            final int msgLen = msg.getSerializedSize();
            Bits.putInt(lenBytes, 0, msgLen);
            output.write(lenBytes);
            msg.writeTo(output);
            if (sync) {
                fOut.getFD().sync();
            }
        }

        return Utils.atomicMoveFile(file, new File(this.path));
    }

}
