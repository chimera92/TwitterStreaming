package com.mytwitterstream;


import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mytwitterstream.filter.MusicFilter;
import com.mytwitterstream.sink.MongoSink;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import twitter4j.Status;
import twitter4j.User;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MyTwitterClientTest {

    private static final String DATABASE_NAME = "embedded";

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private MongoClient mongo;

    @Before
    public void beforeEach() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        String bindIp = "localhost";
        int port = 12345;
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                .build();
        this.mongodExe = starter.prepare(mongodConfig);
        this.mongod = mongodExe.start();
        this.mongo = new MongoClient(bindIp, port);
    }

    @After
    public void afterEach() throws Exception {
        if (this.mongod != null) {
            this.mongod.stop();
            this.mongodExe.stop();
        }
    }


    @Test
    public void happyPath()
    {

        List<Status> tweets = new ArrayList<Status>(4);

        Status tweet1 = mock(Status.class);
        Status tweet2 = mock(Status.class);
        Status tweet3 = mock(Status.class);
        Status tweet1Duplicate = mock(Status.class);

        when(tweet1.getId()).thenReturn(1L);
        when(tweet2.getId()).thenReturn(2L);
        when(tweet3.getId()).thenReturn(3L);
        when(tweet1Duplicate.getId()).thenReturn(1L);

        User u1 = mock(User.class);
        User u2 = mock(User.class);
        User u3 = mock(User.class);

        when(tweet1.getUser()).thenReturn(u1);
        when(tweet2.getUser()).thenReturn(u2);
        when(tweet3.getUser()).thenReturn(u3);
        when(tweet1Duplicate.getUser()).thenReturn(u1);


        when(u1.getName()).thenReturn("A");
        when(u2.getName()).thenReturn("B");
        when(u3.getName()).thenReturn("C");


        when(tweet1.getText()).thenReturn("I am A"); // Will be inserted to Mongo collection
        when(tweet2.getText()).thenReturn("I am music"); // Will be filtered out because of the word "music"
        when(tweet3.getText()).thenReturn("I am a song"); // Will be filtered out because of the word "song"
        when(tweet1Duplicate.getText()).thenReturn("I am duplicate A"); // Will fail safely on insert because its a duplicate key

        tweets.add(tweet1);
        tweets.add(tweet2);
        tweets.add(tweet3);
        tweets.add(tweet1Duplicate);

        MongoSink mongoSink = new MongoSink("mongodb://localhost:12345",
                DATABASE_NAME,
                "tweets");


        MyTwitterClient myTwitterClient = new MyTwitterClient("","","","");
        MusicFilter musicFilter = new MusicFilter();
        musicFilter.addToken("music");
        musicFilter.addToken("song");
        myTwitterClient.addFilter(musicFilter);
        myTwitterClient.addSink(mongoSink);
        myTwitterClient.processTweets(tweets); // tweet 2 and 3 are filtered out



        MongoDatabase db = mongo.getDatabase(DATABASE_NAME);
        MongoCollection<Document> c = db.getCollection("tweets");

        assert(c.countDocuments() == 1);

        String collection_json = c.find().cursor().next().toJson();
        System.out.println(collection_json);
        Gson gson = new Gson();
        TweetPojo tweetPojo= gson.fromJson(collection_json, TweetPojo.class);



        assert(tweetPojo.getId() == 1L);
        assert(tweetPojo.getTweetText().equals("I am A"));
        assert(tweetPojo.getAuthor().equals("A"));

    }

}