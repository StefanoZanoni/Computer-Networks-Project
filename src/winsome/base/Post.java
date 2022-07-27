package winsome.base;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.*;

public final class Post {

    public static class Comment {

        private final String author;
        private final String text;
        private final String timestamp;

        public Comment(String author, String text) {

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

        public String toString() {
            return timestamp + " " + author + ": " + text + "\n";
        }

    }

    private static final AtomicInteger IDCounter = new AtomicInteger(1000);
    private final int ID;
    private final String owner;
    private final String title;
    private final String content;
    private final List<String> shares = new LinkedList<>();
    private final List<String> upvotes = new LinkedList<>();
    private final List<String> downvotes = new LinkedList<>();
    private final List<Comment> comments = new LinkedList<>();

    public Post(String owner, String title, String content) {

        if (owner == null || title == null || content == null)
            throw new NullPointerException();

        ID = IDCounter.getAndIncrement();
        this.owner = owner;
        this.title = title;
        this.content = content;

    }

    public int getID() {
        return ID;
    }

    public String getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public synchronized void addShare(String username) {
        shares.add(username);
    }

    public List<String> getShares() {
        return shares;
    }

    public synchronized void addUpvote(String username) {
        upvotes.add(username);
    }

    public List<String> getUpvotes() { return upvotes; }

    public synchronized void addDownvote(String username) {
        downvotes.add(username);
    }

    public List<String> getDownvotes() {
        return downvotes;
    }

    public synchronized void addComment(Comment comment) { comments.add(comment); }

    public List<Comment> getComments() {
        return comments;
    }

    public String toString() {
        return  "Title: " + title + "\n"
                + "Content: " + content + "\n"
                + "Votes: " + upvotes.size() + " positive" + ", " +  downvotes.size() + " negative\n"
                + "Comments: " + comments;
    }

    /**
     * @param numberOfPreviousUpvotes   the number of previous upvotes that is stored by the server
     * @param numberOfPreviousDownvotes the number of previous downvotes that is stored by the server
     * @param numberOfPreviousComments  the number of previous comments that is stored by the server
     * @param age                       the number of times the reward was computed
     * @return the computed reward for this post
     *
     * Since a user cannot upvote or downvote two times the same post and
     * upvote and downvote can only assume the value +1 and -1 respectively, the first summation
     * inside the first log is well-defined
     */
    public float computeReward(int numberOfPreviousUpvotes, int numberOfPreviousDownvotes,
                               int numberOfPreviousComments, int age) {

        return (float) ( (log(max((upvotes.size() - numberOfPreviousUpvotes - (downvotes.size() - numberOfPreviousDownvotes)), 0) + 1)
                + log((computeCommentsValue(numberOfPreviousComments)) + 1)) / age );

    }

    /**
     * @param numberOfPreviousComments the number of previous comments that is stored by the server
     * @return the total value of new comments
     * <p>
     * The amount of comments of every user is computed with the help of a HashMap
     */
    private float computeCommentsValue(int numberOfPreviousComments) {

        float commentsValue = 0;

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

            for (Comment comment : comments.subList(numberOfPreviousComments + 1, comments.size())) {

                String author = comment.getAuthor();
                UserCounter userCounter = peopleChecked.get(author);
                if (userCounter == null)
                    peopleChecked.put(author, new UserCounter());
                else
                    userCounter.increment();

            }

            for (Comment comment : comments.subList(numberOfPreviousComments + 1, comments.size())) {

                String author = comment.getAuthor();
                commentsValue += (2 / (1 + exp(1 - peopleChecked.get(author).getValue())));

            }

        }

        return commentsValue;

    }

}
