package com.mytwitterstream;

import com.mytwitterstream.filter.TwitterFilter;
import com.mytwitterstream.sink.TwitterSink;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.ArrayList;
import java.util.List;

public class MyTwitterClient {

    private Logger logger;
    private final String CONSUMER_KEY;
    private final String CONSUMER_SECRET;
    private final String ACCESS_TOKEN;
    private final String ACCESS_TOKEN_SECRET;
    private List<TwitterSink> sinks;
    private List<TwitterFilter> filters;

    private Twitter twitter;
    private Long tweetsConsumedCount; //Todo Handle overflow


    public MyTwitterClient(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
    {
        this.CONSUMER_KEY=consumerKey;
        this.CONSUMER_SECRET=consumerSecret;
        this.ACCESS_TOKEN=accessToken;
        this.ACCESS_TOKEN_SECRET=accessTokenSecret;
        this.sinks = new ArrayList<TwitterSink>();
        this.filters = new ArrayList<TwitterFilter>();
        this.twitter = this.getTwitterInstance();
        this.tweetsConsumedCount = 0L;
        logger = Logger.getLogger(MyTwitterClient.class);
    }

    private boolean isFiltered(TweetPojo tweetPojo) {
        for(TwitterFilter filter:this.filters)
        {
            if(filter.isTrue(tweetPojo)){
                return true;
            }
        }
        return false;
    }


    private Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    public void addSink(TwitterSink sink)
    {
        this.sinks.add(sink);
    }
    public void addFilter(TwitterFilter filter)
    {
        this.filters.add(filter);
    }

    private TweetPojo twitter4jStatusToTweetPojo(Status tweet)
    {
        TweetPojo tweetPojo = new TweetPojo();
        tweetPojo.setId(tweet.getId());
        tweetPojo.setAuthor(tweet.getUser().getName());
        tweetPojo.setTweetText(tweet.getText());
        return tweetPojo;
    }


    private void writeTweetToSinks(TweetPojo tweet)
    {
        for(TwitterSink sink:this.sinks)
        {
            sink.handleTweets(tweet);
        }
    }

    public void startSearchTweets(String searchTerm) throws InterruptedException, TwitterException {
            Query query = new Query(searchTerm);
            QueryResult result = null;

            do {
                try {
                    result = twitter.search(query);
                    this.tweetsConsumedCount += result.getCount();
                    logger.info("Total Tweets consumed in current session: ",tweetsConsumedCount.toString());

                    List<Status> tweets = result.getTweets();
                    for (Status tweet : tweets) {

                        TweetPojo tweetPojo = twitter4jStatusToTweetPojo(tweet);
                        if(isFiltered(tweetPojo)) {
                            logger.info("Tweet filtered out! Id: ",tweetPojo.getId().toString());
                            continue ;
                        }
                        writeTweetToSinks(tweetPojo);

                    }
                }catch (TwitterException te) {

                        if(te.exceededRateLimitation())
                        {
                            long secsUntilReset = te.getRateLimitStatus().getSecondsUntilReset();
                            logger.warn("Rate limit reached! Seconds to reset: ",Long.toString(secsUntilReset));
                            logger.warn("Sleeping for ",Long.toString(secsUntilReset));
                            Thread.sleep(secsUntilReset*1000);
                        }else{
                            throw te;
                        }
                    }
            } while (result == null || (query = result.nextQuery()) != null  );
    }




}
