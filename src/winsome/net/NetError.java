package winsome.net;

public enum NetError {

    NONE(0) {
        @Override
        public void showError() { System.out.println("Operation completed successfully\n"); }
    },
    USERNAMEALREADYEXISTS(1) {
        @Override
        public void showError() {
            System.err.println("Error: the inserted username is already present\n");
        }
    },
    EMPTYPASSWORD(2){
        @Override
        public void showError() {
            System.err.println("Error: the password is empty\n");
        }
    },
    WRONGPASSWORD(3){
        @Override
        public void showError() {
            System.err.println("Error: wrong password\n");
        }
    },
    VOTEALREADYASSIGNED(4){
        @Override
        public void showError() {
            System.err.println("Error: You have already rated this post\n");
        }
    },
    POSTNOTINTHEFEED(5){
        @Override
        public void showError() {
            System.err.println("Error: You cannot rate a post of a user you not follow\n");
        }
    },
    ISTHEAUTHOR(6){
        @Override
        public void showError() {
            System.err.println("Error: You cannot rate your own post\n");
        }
    },
    NOTTHEOWNER(7){
        @Override
        public void showError() {
            System.err.println("Error: You are not the owner of this post\n");
        }
    },
    USERDOESNOTEXIST(8) {
        @Override
        public void showError() {
            System.err.println("Error: inserted username does not exist\n");
        }
    },
    USERALREADYLOGGEDIN(9) {
        @Override
        public void showError() {
            System.err.println("Error: user already logged in\n");
        }
    },
    USERNOTYETLOGGEDIN(10) {
        @Override
        public void showError() { System.err.println("Error: user not yet logged in\n"); }
    },
    POSTDOESNOTEXIST(11) {
        @Override
        public void showError() { System.err.println("Error: post does not exist\n"); }
    },
    POSTALREADYVOTED(12) {
        @Override
        public void showError() { System.err.println("Error: post already voted\n"); }
    },
    INVALIDVOTE(13) {
        @Override
        public void showError() { System.err.println("Error: the vote is invalid\n"); }
    }
    ;

    private final int code;

    NetError(int code) {
        this.code = code;
    }

    public int getCode(){ return code; }

    public abstract void showError();

}
