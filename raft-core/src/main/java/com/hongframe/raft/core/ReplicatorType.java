package com.hongframe.raft.core;

public enum ReplicatorType {

    Follower, Learner;

    public final boolean isFollower() {
        return this == Follower;
    }

    public final boolean isLearner() {
        return this == Learner;
    }

}
