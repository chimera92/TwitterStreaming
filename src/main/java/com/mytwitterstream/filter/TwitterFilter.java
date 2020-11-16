package com.mytwitterstream.filter;

import com.mytwitterstream.TweetPojo;

public interface TwitterFilter {

    /**
        Return True to filter out tweet, false to retain.
     */
    public boolean isTrue(TweetPojo tweet);


}
