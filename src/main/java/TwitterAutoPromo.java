import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAutoPromo extends HttpServlet 
   {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   //This is the message that you will tweet out
   public static final String message = "Your message"; 
   //Twitter handle of person who has a lot of followers
   public static final String host = "wolfblitzer"; 

   public void doGet(HttpServletRequest request, HttpServletResponse resp) 
      throws IOException 
      {
      Twitter bacon;
      IDs ids;
      long cursor = -1L;
      String tweet = "";
         
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity cursorEntity;
      
      resp.setContentType("text/plain");

      try 
         {  
         ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();     
         twitterConfigBuilder.setDebugEnabled(false);
         
         //Replace these values with your own
         twitterConfigBuilder.setOAuthConsumerKey("OAUTH_CONSUMER_KEY");
         twitterConfigBuilder.setOAuthConsumerSecret("OAUTH_CONSUMER_SECRET");
         twitterConfigBuilder.setOAuthAccessToken("OAUTH_ACCESS_TOKEN");
         twitterConfigBuilder.setOAuthAccessTokenSecret("OAUTH_ACCESS_SECRET");
          
         bacon = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
         
         //Retrieve stored cursor
         cursorEntity = datastore.get(KeyFactory.createKey("cursorEntity", "cursor"));
         cursor = Long.parseLong(cursorEntity.getProperty("cursor").toString());
         
         if(cursor == 0)
            resp.getWriter().println("All followers have been notified.");
         
         ids = bacon.getFollowersIDs(host, cursor, 5); //Do 5 at a time
         
         for (long id : ids.getIDs()) 
            {
            tweet = "@" + bacon.showUser(id).getScreenName() + " "+message;
            bacon.updateStatus(tweet);
            resp.getWriter().println("Tweet posted: "+tweet);
            tweet = new String(""); // Clear out old tweet value
            }
        
         //Retrieve the cursor value for the next time!
         cursor = ids.getNextCursor();
         //Store the value of the cursor
         cursorEntity.setProperty("cursor", cursor);
         datastore.put(cursorEntity);     
         }
      
      catch (EntityNotFoundException e) 
         {
         // Make new ResponseIDentity
         resp.getWriter().println("Creating cursorEntity...");
         cursorEntity = new Entity("cursorEntity", "cursor");
         cursorEntity.setProperty("cursor", "-1");
         datastore.put(cursorEntity);
         }
       catch(TwitterException e)
         {
         resp.getWriter().println("Problem with Twitter");
         resp.getWriter().println("<pre>");
         e.printStackTrace(resp.getWriter());
         resp.getWriter().println("</pre>");
         }
       catch(Exception e)
         {
         e.printStackTrace(System.err);
         resp.getWriter().println("<pre>");
         e.printStackTrace(resp.getWriter());
         resp.getWriter().println("</pre>");
         }
  }
}
