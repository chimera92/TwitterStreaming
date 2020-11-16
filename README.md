# TwitterStreaming
Twitter streaming Java App streaming tweets to MongoDb

**What the app does:**
- Reads tweets from the Twitter streaming API using twitter4j
- Applies filters to remove tweets not needed
- Write the tweets to MongoDb collection with TweetId as primary key.
- Logs the distinct and total counts of tweets consumed.

**Runing the App locally:**

    git clone https://github.com/chimera92/TwitterStreaming.git
    cd TwitterStreaming

  **FILL IN PROPERTIES FILE at: ./src/main/resources/twittersearch.properties**


    mvn clean install
    java -jar ./target/TwitterStreaming-1.0-SNAPSHOT-shaded.jar twittersearch.properties


**Notes:**
- A pre-created mongo DB database is requirement.
- The twitter keys can be created at https://developer.twitter.com/en/portal/projects-and-apps (Create a stand alone App outside of a project since we are using the V1 API)
- 1. Filtered out tweets are logged like - filtered out! Id: 1328228948099022848
  2. Total consumed tweets across runs are logged as "Total Unique tweets in mongo collection" 
  3. Total unique tweets in current run is logged as "Total Unique tweets in current session"(This is reset on job restart)
  4. Duplicate keys (occurs on job restarts) are logged as "Skipping record. Duplicate Tweet! id: xxxx"
  
**Questions to answer:**  
1) What are the risks involved in building such a pipeline? 
-  Filtering out messages before persisting in a store has a potential risk of data loss
- The destination DB needs to be highly available else can cause data loss.

2)How would you roll out the pipeline going from proof-of-concept to a production-ready solution?
- Decide the infrastructure to deploy on.. Preferably on Kubernetes.
- Dockerize the App. 
- Set up CI/CD 
- Add monitoring and alerting
- Make sure the App is run in stage and observed with close to real Traffic
- Make sure exception handling for non confirming messages is in place. The pipeline should not fail when there are unexpected faults in the incoming messages. The faulty messages should land in a dead letter queue.
- Setup a sink for logs and setup log rotation.
- Write more tests for better code coverage.


3)What would a production-ready solution entail that a POC wouldn't?
- The filtering of tweets needs to be enhanced 2 fold.
Right now the tweets with the words "music" or "song" in them are being skipped.
  1. More tokens signifying music need to be added.
  2. The tokens need to be lemmatized (both - music tokens and the tokens in the tweet content) for better accuracy: https://stanfordnlp.github.io/CoreNLP/
- Secrets management using tools like Hashicorp Vault.
- Better configuration management so the configs can be changed without redeploying the App.
- Dockerizing the App


4)What is the level of effort required to deliver each phase of the solution?
  
  - LOE would be 1 week for each feature plus 2 weeks to factor for running in stage and blockers.

5)What is your estimated timeline for delivery for a production-ready solution?

  - 4 - 6 weeks.
