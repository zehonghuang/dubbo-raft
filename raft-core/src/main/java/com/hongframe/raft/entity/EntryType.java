package com.hongframe.raft.entity;

public enum EntryType {

    ENTRY_TYPE_UNKNOWN(1),

    ENTRY_TYPE_NO_OP(2),

    ENTRY_TYPE_DATA(3),

    ENTRY_TYPE_CONFIGURATION(4),;

    private int type;

    EntryType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static EntryType get(int type) {
        switch (type) {
            case 1:
                return ENTRY_TYPE_UNKNOWN;
            case 2:
                return ENTRY_TYPE_NO_OP;
            case 3:
                return ENTRY_TYPE_DATA;
            case 4:
                return ENTRY_TYPE_CONFIGURATION;
        }
        return null;
    }
}
