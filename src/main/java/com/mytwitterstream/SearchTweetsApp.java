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
     * Usage:
     * mvn clean install
     * java -jar ./target/TwitterStreaming-1.0-SNAPSHOT-shaded.jar <properties file name in resources folder>
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

            MyTwitterClient myTwitterClient= new MyTwitterClient(prop.getProperty("twitterapikey"),
                    prop.getProperty("twitterapirsecret"),
                    prop.getProperty("twitteraccesstoken"),
                    prop.getProperty("twitteraccesstokensecret"));



        myTwitterClient.addSink(mongoSink);
        //twitterConnectionHandler.addSink(new ConsoleSink()); // Additional sinks can be added this way
        MusicFilter musicFilter = new MusicFilter();
        musicFilter.addToken("music");
        musicFilter.addToken("song");
        myTwitterClient.addFilter(musicFilter);

        myTwitterClient.startSearchTweets(prop.getProperty("search.term"));

        }
        finally {
            mongoSink.closeConnection();
        }

    }
}
