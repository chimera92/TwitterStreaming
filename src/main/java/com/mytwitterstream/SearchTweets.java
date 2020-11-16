package com.mytwitterstream;

import com.mytwitterstream.filter.MusicFilter;
import com.mytwitterstream.sink.MongoSink;
import twitter4j.*;

/**
 * @author Hemanth Gowda
 */
public class SearchTweets {
    /**
     * Usage: java com.mytwitterstream.SearchTweets [query]
     *
     * @param args search query
     *
     */
    public static void main(String[] args) throws TwitterException, InterruptedException {

        Logger logger = Logger.getLogger(SearchTweets.class);

        MongoSink mongoSink = new MongoSink("localhost",
                27017,
                "twitterStreamDB",
                "tweets");

        try{


        MyTwitterClient twitterConnectionHandler= new MyTwitterClient("",
                "",
                "",
                "");



        twitterConnectionHandler.addSink(mongoSink);
//        twitterConnectionHandler.addSink(new ConsoleSink());
        MusicFilter musicFilter = new MusicFilter();
        musicFilter.addToken("music");
        musicFilter.addToken("song");
        twitterConnectionHandler.addFilter(musicFilter);

        twitterConnectionHandler.startSearchTweets("justin bieber");



        }
        finally {
            mongoSink.closeConnection();
        }

    }
}
