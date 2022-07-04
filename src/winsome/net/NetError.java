package winsome.net;

public enum NetError {

    NONE(0) {
        @Override
        public void showError() {}
    },
    USERNAMEALREADYPRESENT(1) {
        @Override
        public void showError() {
            System.out.println("Error: the inserted username is already present");
        }
    },
    EMPTYPASSWORD(2){
        @Override
        public void showError() {
            System.out.println("Error: the password is empty");
        }
    },
    LOGINALREADYDONE(3){
        @Override
        public void showError() {
            System.out.println("Error: login already done");
        }
    },
    WRONGPASSWORD(4){
        @Override
        public void showError() {
            System.out.println("Error: wrong password");
        }
    },
    VOTEALREADYASSIGNED(5){
        @Override
        public void showError() {
            System.out.println("Error: You have already rated this post");
        }
    },
    POSTNOTINTHEFEED(6){
        @Override
        public void showError() {
            System.out.println("Error: You cannot rate a post of a user you not follow");
        }
    },
    ISTHEAUTHOR(7){
        @Override
        public void showError() {
            System.out.println("Error: You cannot rate your own post");
        }
    },
    NOTTHEOWNER(8){
        @Override
        public void showError() {
            System.out.println("Error: You are not the owner of this post");
        }
    },
    ;

    private final int code;

    NetError(int code) {
        this.code = code;
    }

    public int getCode(){ return code; }

    public abstract void showError();

}
