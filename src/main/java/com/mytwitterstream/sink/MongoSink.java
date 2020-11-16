package com.mytwitterstream.sink;

import com.google.gson.Gson;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mytwitterstream.TweetPojo;
import org.bson.Document;
import twitter4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class MongoSink implements TwitterSink {

    private Logger logger;
    private MongoClient mongo;
    private MongoDatabase db;
    private MongoCollection<Document> collection;
    private Long uniqueCount; //Todo Handle overflow
    private Long uniqueCountThisSession;
    private Long prevUniqueCount;
    private Timer timer;

    private class LogUniqueCountTask extends TimerTask
    {
        public void run()
        {
            if(prevUniqueCount < uniqueCount)
            {
                logger.info("Total Unique tweets in mongo collection: ",uniqueCount.toString());
                logger.info("Total Unique tweets in current session: ",uniqueCountThisSession.toString());
            }
            prevUniqueCount = uniqueCount;
        }
    }

    public MongoSink(String URI,String db,String collection)
    {
        this.logger = Logger.getLogger(MongoSink.class);
        this.mongo = new MongoClient(new MongoClientURI(URI));
        this.db = this.mongo.getDatabase(db);
        this.collection=this.db.getCollection(collection);
        this.uniqueCount = this.collection.countDocuments();
        this.uniqueCountThisSession=0l;
        this.prevUniqueCount = this.collection.countDocuments();
        this.timer = new Timer();
        TimerTask logUniqueCountTask = new LogUniqueCountTask();
        timer.schedule(logUniqueCountTask, 0,1000);  // 2000 - delay (can set to 0 for immediate execution), 5000 is a frequency.
    }


    @Override
    public void handleTweets(TweetPojo tweet) {

        Gson gson = new Gson();
        String json = gson.toJson(tweet);
        Document doc = Document.parse( json );
        try{
            collection.insertOne(doc);
            this.uniqueCount += 1;
            this.uniqueCountThisSession += 1;
        }
        catch(MongoWriteException e)
        {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {

                logger.info("Skipping record. Duplicate Tweet! id: ",tweet.getId().toString());
            }
            else {
                throw e;
            }
        }

    }

    public void closeConnection()
    {
        this.mongo.close();
        this.timer.cancel();
        logger.info("MongoDb connection closed!");
    }

}
