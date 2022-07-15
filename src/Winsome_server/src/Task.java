import java.nio.channels.SocketChannel;
import java.util.*;

public enum Task implements Runnable {

    REGISTER {

        String username;
        char[] password;
        final List<String> tags = new ArrayList<>();

        @Override
        public final void setAttributes(List<String> attributes) {
            username = attributes.get(0);
            password = attributes.get(1).toCharArray().clone();
            for (int i = 2; i < 7; i++) {
                tags.add(attributes.get(i));
            }
        }

        @Override
        public void run() {

        }

    },
    LOGIN{

        String username;
        char[] password;

        @Override
        public final void setAttributes(List<String> attributes) {
            username = attributes.get(0);
            password = attributes.get(1).toCharArray().clone();
        }

        @Override
        public void run() {

        }

    },
    LOGOUT{

        String username;

        @Override
        public final void setAttributes(List<String> attributes) {
            username = attributes.get(0);
        }

        @Override
        public void run() {

        }

    },
    LISTUSERS {
        @Override
        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    LISTFOLLOWERS{
        @Override
        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    LISTFOLLOWING{
        @Override
        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    FOLLOWUSER{

        int idUser;

        @Override
        public void setAttributes(List<String> attributes) {
            idUser = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

        }

    },
    UNFOLLOWUSER{

        int idUser;

        public void setAttributes(List<String> attributes) {
            idUser = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

        }

    },
    VIEWBLOG{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    CREATEPOST {

        String title, content;

        public void setAttributes(List<String> attributes) {
            title = attributes.get(0);
            content = attributes.get(1);
        }

        @Override
        public void run() {

        }

    },
    SHOWFEED{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    SHOWPOST{

        int idPost;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

        }

    },
    DELETEPOST{

        int idPost;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

        }

    },
    REWINPOST{

        int idPost;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

        }

    },
    RATEPOST{

        int idPost, vote;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
            vote = Integer.parseInt( attributes.get(1) );
        }

        @Override
        public void run() {

        }

    },
    ADDCOMMENT{

        int idPost;
        String comment;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
            comment = attributes.get(1);
        }

        @Override
        public void run() {

        }
    },
    GETWALLET{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    },
    GETWALLETBITCOIN{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

        }

    }
    ;

    private SocketChannel client;
    public abstract void setAttributes(List<String> attributes);

}
