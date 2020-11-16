package com.mytwitterstream.filter;

import com.mytwitterstream.TweetPojo;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class MusicFilter implements TwitterFilter{

    //Todo add lemmatization to improve accuracy (Maybe Stanford core NLP api)

    private Set<String> musicTokens = new HashSet<String>();

    @Override
    public boolean isTrue(TweetPojo tweet) {

        StringTokenizer st = new StringTokenizer(tweet.getTweetText(), " ");
        while (st.hasMoreTokens()) {

            String token = st.nextToken();
            if(musicTokens.contains(token))
            {
                return true;
            }
        }
        return false;
    }

    public void addToken(String token)
    {
        //Todo: Future work - add stemming and lemmatization
        musicTokens.add(token.toLowerCase());
    }


}
