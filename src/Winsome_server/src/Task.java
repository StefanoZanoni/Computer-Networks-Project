import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import winsome.base.Post;
import winsome.base.User;
import winsome.net.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                SocialNetworkManager.addUser(client, username, password, tags);
                buffer.putInt( NetError.NONE.getCode() );
                SocialNetworkManager.couple(client, username);
            } catch (UsernameAlreadyExistsException e) {
                buffer.putInt( NetError.USERNAMEALREADYEXISTS.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

            SocialNetworkManager.couple(client, username);

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                SocialNetworkManager.checkUser(client, username, password);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserDoesNotExistException e) {
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );
            } catch (UserAlreadyLoggedInException e) {
                buffer.putInt( NetError.USERALREADYLOGGEDIN.getCode() );
            } catch (WrongPasswordException e) {
                buffer.putInt( NetError.WRONGPASSWORD.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

            SocialNetworkManager.uncouple(client, username);

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buffer.putInt( NetError.NONE.getCode() );
            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    LISTUSERS {
        @Override
        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                List<User> userList = SocialNetworkManager.getLikeMindedUsers(client);
                Gson gson = new Gson();
                String jsonUserList = gson.toJson(userList);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonUserList.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonUserList.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonUserList;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    LISTFOLLOWING{
        @Override
        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                List<User> userList = SocialNetworkManager.getFollowed(client);
                Gson gson = new Gson();
                String jsonUserList = gson.toJson(userList);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonUserList.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String outcome = NetError.NONE.getCode() + "|" + jsonUserList;
                bufferCapacity = jsonUserList.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    FOLLOWUSER{

        String idUser;

        @Override
        public void setAttributes(List<String> attributes) { idUser = attributes.get(0); }

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                SocialNetworkManager.addFollowing(client, idUser);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERALREADYLOGGEDIN.getCode() );
            } catch (UserDoesNotExistException e) {
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    UNFOLLOWUSER{

        String idUser;

        public void setAttributes(List<String> attributes) { idUser = attributes.get(0); }

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                SocialNetworkManager.removeFollowing(client, idUser);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (UserDoesNotExistException e) {
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    VIEWBLOG{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                List<Post> userPosts = SocialNetworkManager.getPosts(client);
                Gson gson = new Gson();
                String jsonUserPosts = gson.toJson(userPosts);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonUserPosts.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonUserPosts.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonUserPosts;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.NONE.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            buffer.putInt(Integer.BYTES);
            try {
                client.write(buffer);
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                SocialNetworkManager.addPost(client, title, content);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    SHOWFEED{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                List<Post> userPosts = SocialNetworkManager.getFeed(client);
                Gson gson = new Gson();
                String jsonUserPosts = gson.toJson(userPosts);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonUserPosts.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonUserPosts.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonUserPosts;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    SHOWPOST{

        int idPost;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
        }

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                Post post = SocialNetworkManager.getPost(idPost);
                Gson gson = new Gson();
                String jsonPost = gson.toJson(post);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonPost.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonPost.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonPost;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (PostDoesNotExistException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                    buffer.clear();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.putInt( NetError.POSTDOESNOTEXIST.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


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

    protected SocketChannel client;
    public abstract void setAttributes(List<String> attributes);
    public void setClient(SocketChannel client) { this.client = client; }

}
