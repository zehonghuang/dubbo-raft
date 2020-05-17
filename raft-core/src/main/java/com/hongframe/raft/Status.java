package com.hongframe.raft;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-17 17:27
 */
public class Status {

    private static class State {
        int    code;
        String msg;

        public State(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    private State state;

    public Status() {
        this.state = null;
    }

    public static Status OK() {
        return new Status();
    }

    public Status(Status s) {
        if (s.state != null) {
            this.state = new State(s.state.code, s.state.msg);
        } else {
            this.state = null;
        }
    }

    public int getCode() {
        return this.state == null ? 0 : this.state.code;
    }
    public Status(int code, String errorMsg) {
        this.state = new State(code, errorMsg);
    }

    public boolean isOk() {
        return this.state == null || this.state.code == 0;
    }

    public void setError(int code, String fmt, Object... args) {
        this.state = new State(code, String.format(String.valueOf(fmt), args));
    }

    public String getErrorMsg() {
        return this.state == null ? null : this.state.msg;
    }

}
