import com.google.gson.Gson;
import winsome.base.Post;
import winsome.base.User;
import winsome.base.Wallet;
import winsome.net.*;
import winsome.net.exceptions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public enum Task implements Runnable {

    REGISTER {

        String username;
        char[] password;
        final List<String> tags = new ArrayList<>(5);

        @Override
        public final void setAttributes(List<String> attributes) {
            char[] password = attributes.get(1).toCharArray();
            username = attributes.get(0);
            this.password = password.clone();
            Arrays.fill(password, (char) 0);
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
    LOGIN {

        String username;
        char[] password;

        @Override
        public final void setAttributes(List<String> attributes) {
            char[] password = attributes.get(1).toCharArray();
            username = attributes.get(0);
            this.password = password.clone();
            Arrays.fill(password, (char) 0);
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
    LOGOUT {

        String username;

        @Override
        public final void setAttributes(List<String> attributes) {
            username = attributes.get(0);
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
                SocialNetworkManager.uncouple(client, username);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            }

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
    LISTFOLLOWING {
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
    FOLLOW {

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
    UNFOLLOW {

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
    BLOG {

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
    POST {

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
    SHOWFEED {

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
    SHOWPOST {

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
    DELETE {

        int idPost;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
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
                SocialNetworkManager.deletePost(client, idPost);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (PostDoesNotExistException e) {
                buffer.putInt( NetError.POSTDOESNOTEXIST.getCode() );
            } catch (UserIsNotTheOwnerException e) {
                buffer.putInt( NetError.NOTTHEOWNER.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    REWIN {

        int idPost;

        public void setAttributes(List<String> attributes) { idPost = Integer.parseInt( attributes.get(0) ); }

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
                SocialNetworkManager.rewin(client, idPost);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (PostNotInTheFeedException e) {
                buffer.putInt( NetError.POSTNOTINTHEFEED.getCode() );
            } catch (PostDoesNotExistException e) {
                buffer.putInt(NetError.POSTDOESNOTEXIST.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    RATE {

        int idPost, vote;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
            vote = Integer.parseInt( attributes.get(1) );
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
                SocialNetworkManager.ratePost(client, idPost, vote);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (PostDoesNotExistException e) {
                buffer.putInt( NetError.POSTDOESNOTEXIST.getCode() );
            } catch (PostNotInTheFeedException e) {
                buffer.putInt( NetError.POSTNOTINTHEFEED.getCode() );
            } catch (PostAlreadyVotedException e) {
                buffer.putInt( NetError.POSTALREADYVOTED.getCode() );
            } catch (UserIsTheAuthorException e) {
                buffer.putInt( NetError.ISTHEAUTHOR.getCode() );
            } catch (InvalidVoteException e) {
                buffer.putInt( NetError.INVALIDVOTE.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    COMMENT {

        int idPost;
        String comment;

        public void setAttributes(List<String> attributes) {
            idPost = Integer.parseInt( attributes.get(0) );
            comment = attributes.get(1);
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
                SocialNetworkManager.addComment(client, idPost, comment);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (UserIsTheAuthorException e) {
                buffer.putInt( NetError.ISTHEAUTHOR.getCode() );
            } catch (PostNotInTheFeedException e) {
                buffer.putInt( NetError.POSTNOTINTHEFEED.getCode() );
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    },
    WALLET {

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                Wallet wallet = SocialNetworkManager.getWallet(client);
                Gson gson = new Gson();
                String jsonWallet = gson.toJson(wallet);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonWallet.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonWallet.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonWallet;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                    buffer.clear();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    WALLETBTC{

        public void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                Wallet wallet = SocialNetworkManager.getWalletBTC(client);
                Gson gson = new Gson();
                String jsonWallet = gson.toJson(wallet);
                buffer.putInt(Integer.BYTES + Character.BYTES + jsonWallet.length());
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                bufferCapacity = Integer.BYTES + Character.BYTES + jsonWallet.length();
                buffer = ByteBuffer.allocate(bufferCapacity);
                String outcome = NetError.NONE.getCode() + "|" + jsonWallet;
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    client.write(buffer);
                    buffer.clear();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
    ;

    protected SocketChannel client;
    public abstract void setAttributes(List<String> attributes);
    public void setClient(SocketChannel client) { this.client = client; }

}