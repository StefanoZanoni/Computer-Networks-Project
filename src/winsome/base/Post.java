package winsome.base;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.*;

public final class Post {

    private static class Comment {

        private final String author;
        private final String text;
        private final String timestamp;

        private Comment(String author, String text) {

            if (author == null || text == null)
                throw new NullPointerException();

            this.author = author;
            this.text = text;
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());

        }

        public String getAuthor() {
            return author;
        }
        public String getText() {
            return text;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public String toString() { return this.getTimestamp() + " " + this.getAuthor() + ": " + this.getText(); }

    }
    private final int ID;
    private final String owner;
    private final String title;
    private final String content;
    private final List<String> upvotes = Collections.synchronizedList( new LinkedList<>() );
    private final List<String> downvotes = Collections.synchronizedList( new LinkedList<>() );
    private final List<Comment> comments = Collections.synchronizedList( new LinkedList<>() );

    public Post(String owner, String title, String content, int ID) {

        if (owner == null || title == null || content == null)
            throw new NullPointerException();
        if (ID < 0)
            throw new IllegalArgumentException();

        this.ID = ID + 1000;
        this.owner = owner;
        this.title = title;
        this.content = content;

    }

    public int getID() { return ID; }
    public String getOwner() { return owner; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public void addUpvote(String username) { upvotes.add(username); }
    public List<String> getUpvotes() { return upvotes; }
    public void addDownvote(String username) { downvotes.add(username); }
    public List<String> getDownvotes() { return downvotes; }
    public void addComment(String author, String text) {
        Comment comment = new Comment(author, text);
        comments.add(comment);
    }
    public int getNumberOfComments() { return comments.size(); }
    public List<String> getCommentsAuthors() {

        List<String> commentsAuthors = new LinkedList<>();

        for (Comment comment : comments) {
            String author = comment.getAuthor();
            if (!commentsAuthors.contains(author))
                commentsAuthors.add(author);
        }

        return commentsAuthors;

    }

    public String toString() {
        return  "< Title: " + this.getTitle() + "\n"
                + "< Content: " + this.getContent() + "\n"
                + "< Votes: " + this.getUpvotes().size() + " positive" + ", " +  this.getDownvotes().size() + " negative\n"
                + "< Comments: " + comments;
    }

    /**
     * @param numberOfPreviousUpvotes   the number of previous upvotes that is stored by the server
     * @param numberOfPreviousDownvotes the number of previous downvotes that is stored by the server
     * @param numberOfPreviousComments  the number of previous comments that is stored by the server
     * @param age                       the number of times the reward was computed
     * @return the computed reward for this post
     * <p>
     * Since a user cannot upvote or downvote two times the same post and
     * upvote and downvote can only assume the value +1 and -1 respectively, the first summation
     * inside the first log is well-defined
     */
    public float computeReward(int numberOfPreviousUpvotes, int numberOfPreviousDownvotes,
                               int numberOfPreviousComments, int age) {

        return (float) (    (
                                log( max( (upvotes.size() - numberOfPreviousUpvotes -
                                    (downvotes.size() - numberOfPreviousDownvotes)), 0 ) + 1 )
                                        +
                                log( (computeCommentsValue(numberOfPreviousComments)) + 1)
                            )
                            / age
                        );

    }

    /**
     * @param numberOfPreviousComments the number of previous comments that is stored by the server
     * @return the total value of new comments
     *
     * The amount of comments of every user is computed with the help of a HashMap
     */
    private float computeCommentsValue(int numberOfPreviousComments) {

        float commentsValue = 0;

        // this class counts the number of comments from a user
        class UserCounter {

            int value = 1;
            public void increment() {
                value++;
            }
            public int getValue() {
                return value;
            }

        }

        Map<String, UserCounter> peopleChecked = new HashMap<>();

        if (!comments.isEmpty()) {

            List<Comment> newComments = comments.subList(numberOfPreviousComments, comments.size());

            for ( Comment comment : newComments ) {

                String author = comment.getAuthor();
                UserCounter userCounter = peopleChecked.get(author);
                if (userCounter == null)
                    peopleChecked.put(author, new UserCounter());
                else
                    userCounter.increment();

            }

            for ( Comment comment : newComments ) {

                String author = comment.getAuthor();
                commentsValue += ( 2 / ( 1 + exp( 1 - peopleChecked.get(author).getValue() ) ) );

            }

        }

        return commentsValue;

    }

}