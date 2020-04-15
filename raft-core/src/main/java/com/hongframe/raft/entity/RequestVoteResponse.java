package com.hongframe.raft.entity;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 17:49
 */
public class RequestVoteResponse implements java.io.Serializable {

    private Long term;
    private Boolean preVote;

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public Boolean getPreVote() {
        return preVote;
    }

    public void setPreVote(Boolean preVote) {
        this.preVote = preVote;
    }

    @Override
    public String toString() {
        return "RequestVoteResponse{" +
                "term=" + term +
                ", preVote=" + preVote +
                '}';
    }
}
