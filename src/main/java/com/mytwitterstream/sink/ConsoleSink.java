package com.mytwitterstream.sink;

import com.mytwitterstream.TweetPojo;

public class ConsoleSink implements TwitterSink {

    @Override
    public void handleTweets(TweetPojo tweet) {

        System.out.println(tweet.getAuthor());
        System.out.println(tweet.getTweetText());

    }


}
