package com.hongframe.raft.entity;

public enum EntryType {

    ENTRY_TYPE_UNKNOWN,

    ENTRY_TYPE_NO_OP,

    ENTRY_TYPE_DATA,

    ENTRY_TYPE_CONFIGURATION,;
}
