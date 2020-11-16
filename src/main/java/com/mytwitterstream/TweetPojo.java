package com.mytwitterstream;

public class TweetPojo {

    private String tweetText;
    private String author;
    private Long _id;


    public String getTweetText() {
        return tweetText;
    }

    public void  setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }


    public String getAuthor() {
        return author;
    }

    public void  setAuthor(String author) {
        this.author = author;
    }


    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }


}
