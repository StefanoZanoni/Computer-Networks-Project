package winsomeServer.tcp;

import com.google.gson.Gson;
import winsome.base.Post;
import winsome.base.User;
import winsome.base.Wallet;
import winsome.net.*;
import winsome.net.exceptions.*;
import winsomeServer.ServerMain;
import winsomeServer.network.SocialNetworkManager;

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
            if (attributes.size() > 2)
                for (int i = 2; i < attributes.size(); i++) {
                    tags.add(attributes.get(i));
                }

        }

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                List<String> tempTags = new LinkedList<>(tags);
                SocialNetworkManager.addUser(client, username, password, tempTags);
                Gson gson = new Gson();
                String jsonMulticastAddress = gson.toJson(ServerMain.multicastIP);
                String outcome = jsonMulticastAddress + "|" + ServerMain.multicastPort;
                bufferCapacity = outcome.length();
                buffer.putInt(bufferCapacity);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer = ByteBuffer.allocate(bufferCapacity);
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));
                tags.clear();

            } catch (UsernameAlreadyExistsException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNAMEALREADYEXISTS.getCode() );

            }

            try {
                buffer.flip();
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

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {

                SocialNetworkManager.checkUser(client, username, password);
                Gson gson = new Gson();
                String jsonMulticastAddress = gson.toJson(ServerMain.multicastIP);
                String outcome = jsonMulticastAddress + "|" + ServerMain.multicastPort;
                bufferCapacity = outcome.length();
                buffer.putInt(bufferCapacity);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer = ByteBuffer.allocate(bufferCapacity);
                buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

            } catch (UserDoesNotExistException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );

            } catch (UserAlreadyLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERALREADYLOGGEDIN.getCode() );

            } catch (WrongPasswordException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.WRONGPASSWORD.getCode() );

            }

            try {
                buffer.flip();
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    },
    LOGOUT {

        @Override
        public final void setAttributes(List<String> attributes) {}

        @Override
        public void run() {

            int bufferCapacity = Integer.BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            try {
                SocialNetworkManager.uncouple(client);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            }

            try {
                buffer.flip();
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
                if (!userList.isEmpty()) {
                    String outcome = "";
                    Gson gson = new Gson();
                    Iterator<User> iterator = userList.listIterator();
                    while (iterator.hasNext()) {
                        User user = iterator.next();
                        String jsonUserTags = gson.toJson(user.getTags());
                        if (iterator.hasNext())
                            outcome = outcome.concat(user.getUsername() + "|" + jsonUserTags + "|");
                        else
                            outcome = outcome.concat(user.getUsername() + "|" + jsonUserTags);
                    }
                    buffer.putInt(outcome.getBytes(StandardCharsets.UTF_8).length);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    buffer = ByteBuffer.allocate(outcome.length());
                    buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

                }
                else {

                    buffer.putInt(0);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;

                }

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                buffer.flip();
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
                if (!userList.isEmpty()) {

                    String outcome = "";
                    Gson gson = new Gson();
                    Iterator<User> iterator = userList.listIterator();
                    while (iterator.hasNext()) {
                        User user = iterator.next();
                        String jsonUserTags = gson.toJson(user.getTags());
                        if (iterator.hasNext())
                            outcome = outcome.concat(user.getUsername() + "|" + jsonUserTags + "|");
                        else
                            outcome = outcome.concat(user.getUsername() + "|" + jsonUserTags);
                    }
                    buffer.putInt(outcome.getBytes(StandardCharsets.UTF_8).length);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    buffer = ByteBuffer.allocate(outcome.length());
                    buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

                }
                else {

                    buffer.putInt(0);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;

                }

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                buffer.flip();
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

            try {
                SocialNetworkManager.addFollowing(client, idUser);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERALREADYLOGGEDIN.getCode() );
            } catch (UserDoesNotExistException e) {
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );
            } catch (SelfFollowingException e) {
                buffer.putInt( NetError.SELFFOLLOWING.getCode() );
            }

            try {
                buffer.flip();
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

            try {
                SocialNetworkManager.removeFollowing(client, idUser);
                buffer.putInt( NetError.NONE.getCode() );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            } catch (UserDoesNotExistException e) {
                buffer.putInt( NetError.USERDOESNOTEXIST.getCode() );
            } catch (UserNotFollowedException e) {
                buffer.putInt( NetError.USERNOTFOLLOWED.getCode() );
            }

            try {
                buffer.flip();
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
                if (!userPosts.isEmpty()) {

                    Iterator<Post> iterator = userPosts.listIterator();
                    String outcome = "";
                    while (iterator.hasNext()) {
                        Post post = iterator.next();
                        if (iterator.hasNext())
                            outcome = outcome.concat(post.getID() + "|" + post.getOwner() + "|" + post.getTitle() + "|");
                        else
                            outcome = outcome.concat(post.getID() + "|" + post.getOwner() + "|" + post.getTitle());
                    }
                    buffer.putInt(outcome.getBytes(StandardCharsets.UTF_8).length);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    buffer = ByteBuffer.allocate(outcome.length());
                    buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

                }
                else {

                    buffer.putInt(0);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;

                }

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.NONE.getCode() );

            }

            try {
                buffer.flip();
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

            try {
                int ID = SocialNetworkManager.addPost(client, title, content);
                buffer.putInt( ID );
            } catch (UserNotYetLoggedInException e) {
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );
            }

            try {
                buffer.flip();
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
                if (!userPosts.isEmpty()) {

                    Iterator<Post> iterator = userPosts.listIterator();
                    String outcome = "";
                    while (iterator.hasNext()) {
                        Post post = iterator.next();
                        if (iterator.hasNext())
                            outcome = outcome.concat(post.getID() + "|" + post.getOwner() + "|" + post.getTitle() + "|");
                        else
                            outcome = outcome.concat(post.getID() + "|" + post.getOwner() + "|" + post.getTitle());
                    }
                    buffer.putInt(outcome.getBytes(StandardCharsets.UTF_8).length);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    buffer = ByteBuffer.allocate(outcome.length());
                    buffer.put(outcome.getBytes(StandardCharsets.UTF_8));

                }
                else {

                    buffer.putInt(0);
                    try {
                        buffer.flip();
                        client.write(buffer);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;

                }

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.clear();
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                buffer.flip();
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
                buffer.putInt(jsonPost.length());
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer = ByteBuffer.allocate(jsonPost.length());
                buffer.put(jsonPost.getBytes(StandardCharsets.UTF_8));

            } catch (PostDoesNotExistException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                    buffer.clear();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.putInt( NetError.POSTDOESNOTEXIST.getCode() );

            }

            try {
                buffer.flip();
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
                buffer.flip();
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
                buffer.flip();
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
                buffer.flip();
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
                buffer.flip();
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
                buffer.putInt(jsonWallet.length());
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer = ByteBuffer.allocate(jsonWallet.length());
                buffer.put(jsonWallet.getBytes(StandardCharsets.UTF_8));

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
                    client.write(buffer);
                    buffer.clear();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                buffer.putInt( NetError.USERNOTYETLOGGEDIN.getCode() );

            }

            try {
                buffer.flip();
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

                Map<Float, Wallet> pair = SocialNetworkManager.getWalletBTC(client);
                Wallet wallet = pair.values().stream().findFirst().get();
                float exchangeRate = pair.keySet().stream().findFirst().get();
                Gson gson = new Gson();
                String jsonWallet = gson.toJson(wallet);
                buffer.putInt(jsonWallet.length());
                try {
                    buffer.flip();
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer = ByteBuffer.allocate(jsonWallet.length());
                buffer.put(jsonWallet.getBytes(StandardCharsets.UTF_8));
                wallet.setRewardsBTC(wallet.getRewards() / exchangeRate);

            } catch (UserNotYetLoggedInException e) {

                buffer.putInt(Integer.BYTES);
                try {
                    buffer.flip();
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
                buffer.flip();
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