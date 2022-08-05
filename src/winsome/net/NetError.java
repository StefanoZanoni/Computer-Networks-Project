package winsome.net;

import java.util.HashMap;
import java.util.Map;

public enum NetError {

    NONE(0) {
        @Override
        public void showError() {
            System.out.println("< Operation completed successfully");
            System.out.print("> ");
        }
    },
    USERNAMEALREADYEXISTS(1) {
        @Override
        public void showError() {
            System.out.println("< Error: the inserted username is already present");
            System.out.print("> ");
        }
    },
    EMPTYPASSWORD(2){
        @Override
        public void showError() {
            System.out.println("< Error: the password is empty");
            System.out.print("> ");
        }
    },
    WRONGPASSWORD(3){
        @Override
        public void showError() {
            System.out.println("< Error: wrong password");
            System.out.print("> ");
        }
    },
    VOTEALREADYASSIGNED(4){
        @Override
        public void showError() {
            System.out.println("< Error: You have already rated this post");
            System.out.print("> ");
        }
    },
    POSTNOTINTHEFEED(5){
        @Override
        public void showError() {
            System.out.println("< Error: You cannot rate, comment or rewin a post of a user you not follow");
            System.out.print("> ");
        }
    },
    ISTHEAUTHOR(6){
        @Override
        public void showError() {
            System.out.println("< Error: You cannot rate your own post");
            System.out.print("> ");
        }
    },
    NOTTHEOWNER(7){
        @Override
        public void showError() {
            System.out.println("< Error: You are not the owner of this post");
            System.out.print("> ");
        }
    },
    USERDOESNOTEXIST(8) {
        @Override
        public void showError() {
            System.out.println("< Error: inserted username does not exist");
            System.out.print("> ");
        }
    },
    USERALREADYLOGGEDIN(9) {
        @Override
        public void showError() {
            System.out.println("< Error: user already logged in");
            System.out.print("> ");
        }
    },
    USERNOTYETLOGGEDIN(10) {
        @Override
        public void showError() {
            System.out.println("< Error: user not yet logged in");
            System.out.print("> ");
        }
    },
    POSTDOESNOTEXIST(11) {
        @Override
        public void showError() {
            System.out.println("< Error: post does not exist");
            System.out.print("> ");
        }
    },
    POSTALREADYVOTED(12) {
        @Override
        public void showError() {
            System.out.println("< Error: post already voted");
            System.out.print("> ");
        }
    },
    INVALIDVOTE(13) {
        @Override
        public void showError() {
            System.out.println("< Error: the vote is invalid");
            System.out.print("> ");
        }
    },
    USERNOTFOLLOWED(14) {
        @Override
        public void showError() {
            System.out.println("< Error: you don't follow this user");
            System.out.print("> ");
        }
    },
    SELFFOLLOWING(15) {
        @Override
        public void showError() {
            System.out.println("< Error: you cannot follow yourself");
            System.out.print("> ");
        }
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