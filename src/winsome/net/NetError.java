package winsome.net;

import java.util.HashMap;
import java.util.Map;

public enum NetError {

    NONE(0) {
        @Override
        public void showError() { System.out.println("< Operation completed successfully"); }
    },
    USERNAMEALREADYEXISTS(1) {
        @Override
        public void showError() {
            System.err.println("< Error: the inserted username is already present");
        }
    },
    EMPTYPASSWORD(2){
        @Override
        public void showError() {
            System.err.println("< Error: the password is empty");
        }
    },
    WRONGPASSWORD(3){
        @Override
        public void showError() {
            System.err.println("< Error: wrong password");
        }
    },
    VOTEALREADYASSIGNED(4){
        @Override
        public void showError() {
            System.err.println("< Error: You have already rated this post");
        }
    },
    POSTNOTINTHEFEED(5){
        @Override
        public void showError() {
            System.err.println("< Error: You cannot rate, comment or rewin a post of a user you not follow");
        }
    },
    ISTHEAUTHOR(6){
        @Override
        public void showError() {
            System.err.println("< Error: You cannot rate your own post");
        }
    },
    NOTTHEOWNER(7){
        @Override
        public void showError() {
            System.err.println("< Error: You are not the owner of this post");
        }
    },
    USERDOESNOTEXIST(8) {
        @Override
        public void showError() {
            System.err.println("< Error: inserted username does not exist");
        }
    },
    USERALREADYLOGGEDIN(9) {
        @Override
        public void showError() {
            System.err.println("< Error: user already logged in");
        }
    },
    USERNOTYETLOGGEDIN(10) {
        @Override
        public void showError() { System.err.println("< Error: user not yet logged in"); }
    },
    POSTDOESNOTEXIST(11) {
        @Override
        public void showError() { System.err.println("< Error: post does not exist"); }
    },
    POSTALREADYVOTED(12) {
        @Override
        public void showError() { System.err.println("< Error: post already voted"); }
    },
    INVALIDVOTE(13) {
        @Override
        public void showError() { System.err.println("< Error: the vote is invalid"); }
    },
    USERNOTFOLLOWED(14) {
        @Override
        public void showError() { System.err.println("< Error: you don't follow this user"); }
    },
    SELFFOLLOWING(15) {
        @Override
        public void showError() { System.err.println("< Error: you cannot follow yourself"); }
    }
    ;

    private final int code;
    private static final Map<Integer, NetError> map = new HashMap<>();

    NetError(int code) {
        this.code = code;
    }

    public int getCode(){ return code; }

    public abstract void showError();

    static {
        for (NetError error : NetError.values()) {
            map.put(error.code, error);
        }
    }

    public static NetError valueOf(int error) {
        return map.get(error);
    }

}