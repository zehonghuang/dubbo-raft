package com.hongframe.raft.storage;

import com.hongframe.raft.entity.PeerId;

public interface RaftMetaStorage {

    long getTerm();

    PeerId getVotedFor();

    boolean setTermAndVotedFor(final long term, final PeerId peerId);

}
