package com.hongframe.raft.storage.snapshot.io;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.apache.commons.lang3.SerializationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-23 13:16
 */
public class ProtobufMsgFactory {

    private static Map<String/* class name in proto file */, MethodHandle> PARSE_METHODS_4PROTO = new HashMap<>();
    private static Map<String/* class name in java file */, MethodHandle>  PARSE_METHODS_4J     = new HashMap<>();

    static {
        try {
            final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(ProtoBufFile.class
                    .getResourceAsStream("/raft.protobin"));
            final List<Descriptors.FileDescriptor> resolveFDs = new ArrayList<>();
            for (final DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {

                final Descriptors.FileDescriptor[] dependencies = new Descriptors.FileDescriptor[resolveFDs.size()];
                resolveFDs.toArray(dependencies);

                final Descriptors.FileDescriptor fd = Descriptors.FileDescriptor.buildFrom(fdp, dependencies);
                resolveFDs.add(fd);
                for (final Descriptors.Descriptor descriptor : fd.getMessageTypes()) {

                    final String className = fdp.getOptions().getJavaPackage() + "."
                            + fdp.getOptions().getJavaOuterClassname() + "$" + descriptor.getName();
                    final Class<?> clazz = Class.forName(className);
                    final MethodHandle methodHandle = MethodHandles.lookup().findStatic(clazz, "parseFrom",
                            methodType(clazz, byte[].class));
                    PARSE_METHODS_4PROTO.put(descriptor.getFullName(), methodHandle);
                    PARSE_METHODS_4J.put(className, methodHandle);
                }

            }
        } catch (final Exception e) {
            e.printStackTrace(); // NOPMD
        }
    }

    public static void load() {
        if (PARSE_METHODS_4J.isEmpty() || PARSE_METHODS_4PROTO.isEmpty()) {
            throw new IllegalStateException("Parse protocol file failed.");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T newMessageByJavaClassName(final String className, final byte[] bs) {
        final MethodHandle handle = PARSE_METHODS_4J.get(className);
        if (handle == null) {
            throw new RuntimeException(className + " not found");
        }
        try {
            return (T) handle.invoke(bs);
        } catch (Throwable t) {
            throw new SerializationException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T newMessageByProtoClassName(final String className, final byte[] bs) {
        final MethodHandle handle = PARSE_METHODS_4PROTO.get(className);
        if (handle == null) {
            throw new RuntimeException(className + " not found");
        }
        try {
            return (T) handle.invoke(bs);
        } catch (Throwable t) {
            throw new SerializationException(t);
        }
    }

}
