package com.mytwitterstream.sink;

import com.mytwitterstream.TweetPojo;

public interface TwitterSink {

    public void handleTweets(TweetPojo tweet);
}
