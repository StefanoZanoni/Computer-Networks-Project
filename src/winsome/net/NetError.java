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
            System.err.println("< Error: the inserted username is already present");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    EMPTYPASSWORD(2){
        @Override
        public void showError() {
            System.err.println("< Error: the password is empty");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    WRONGPASSWORD(3){
        @Override
        public void showError() {
            System.err.println("< Error: wrong password");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    VOTEALREADYASSIGNED(4){
        @Override
        public void showError() {
            System.err.println("< Error: You have already rated this post");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    POSTNOTINTHEFEED(5){
        @Override
        public void showError() {
            System.err.println("< Error: You cannot rate, comment or rewin a post of a user you not follow");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    ISTHEAUTHOR(6){
        @Override
        public void showError() {
            System.err.println("< Error: You cannot rate your own post");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    NOTTHEOWNER(7){
        @Override
        public void showError() {
            System.err.println("< Error: You are not the owner of this post");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    USERDOESNOTEXIST(8) {
        @Override
        public void showError() {
            System.err.println("< Error: inserted username does not exist");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    USERALREADYLOGGEDIN(9) {
        @Override
        public void showError() {
            System.err.println("< Error: user already logged in");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    USERNOTYETLOGGEDIN(10) {
        @Override
        public void showError() {
            System.err.println("< Error: user not yet logged in");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    POSTDOESNOTEXIST(11) {
        @Override
        public void showError() {
            System.err.println("< Error: post does not exist");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    POSTALREADYVOTED(12) {
        @Override
        public void showError() {
            System.err.println("< Error: post already voted");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    INVALIDVOTE(13) {
        @Override
        public void showError() {
            System.err.println("< Error: the vote is invalid");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    USERNOTFOLLOWED(14) {
        @Override
        public void showError() {
            System.err.println("< Error: you don't follow this user");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("> ");
        }
    },
    SELFFOLLOWING(15) {
        @Override
        public void showError() {
            System.err.println("< Error: you cannot follow yourself");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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