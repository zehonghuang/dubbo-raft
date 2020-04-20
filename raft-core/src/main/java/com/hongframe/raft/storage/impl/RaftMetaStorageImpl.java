package com.hongframe.raft.storage.impl;

import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.storage.RaftMetaStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class RaftMetaStorageImpl implements RaftMetaStorage {

    private long term;
    private PeerId voteFor;
    private final String path;

    public RaftMetaStorageImpl(String path) {
        this.path = path + ".temp";
        this.voteFor = new PeerId();
        load();
    }

    @Override
    public long getTerm() {
        return this.term;
    }

    @Override
    public PeerId getVotedFor() {
        return this.voteFor;
    }

    @Override
    public boolean setTermAndVotedFor(long term, PeerId peerId) {
        this.term = term;
        this.voteFor = peerId;
        return save();
    }

    private boolean save() {
        String t = this.term + "-" + this.voteFor.toString();
        try {
            FileUtils.write(new File(this.path), t, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean load() {
        File file = new File(this.path);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            String t = FileUtils.readFileToString(file, Charset.defaultCharset());
            if(StringUtils.isBlank(t)) {
                return false;
            }
            String[] s = t.split("-");
            this.term = Long.valueOf(s[0]);
            voteFor.parse(s[1]);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
