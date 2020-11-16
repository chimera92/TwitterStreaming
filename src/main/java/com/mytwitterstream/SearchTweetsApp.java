package com.mytwitterstream;

import com.mytwitterstream.filter.MusicFilter;
import com.mytwitterstream.sink.MongoSink;
import twitter4j.*;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Hemanth Gowda
 */
public class SearchTweetsApp {
    /**
     * Usage: java com.mytwitterstream.SearchTweets [query]
     *
     * @param args search query
     *
     */
    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {

        Logger logger = Logger.getLogger(SearchTweetsApp.class);

        Properties prop = new Properties();

        if(args.length<1)
        {
            logger.error("Pass in resource filename as arg. Ex java -jar ./target/TwitterStreaming-1.0-SNAPSHOT-shaded.jar twittersearch.properties");
            System.exit(-1);
        }

        String fileName = args[0];
        prop.load(SearchTweetsApp.class.getClassLoader().getResourceAsStream(fileName));

        MongoSink mongoSink = new MongoSink(prop.getProperty("mongo.uri"),
                prop.getProperty("mongo.database"),
                prop.getProperty("mongo.collection"));

        try{

            MyTwitterClient twitterConnectionHandler= new MyTwitterClient(prop.getProperty("consumerkey"),
                    prop.getProperty("consumersecret"),
                    prop.getProperty("accesstoken"),
                    prop.getProperty("accesstokensecret"));



        twitterConnectionHandler.addSink(mongoSink);
        //twitterConnectionHandler.addSink(new ConsoleSink()); // Additional sinks can be added this way
        MusicFilter musicFilter = new MusicFilter();
        musicFilter.addToken("music");
        musicFilter.addToken("song");
        twitterConnectionHandler.addFilter(musicFilter);

        twitterConnectionHandler.startSearchTweets(prop.getProperty("search.term"));

        }
        finally {
            mongoSink.closeConnection();
        }

    }
}
