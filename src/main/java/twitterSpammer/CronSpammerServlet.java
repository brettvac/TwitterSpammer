/*
 * Copyright (c) 2017 daloonik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package twitterSpammer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import twitter4j.IDs;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class CronSpammerServlet extends HttpServlet 
   {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   public static final Random r = new Random();

   private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   private static Entity cursorEntity, oauthEntity, spamEntity;
   public static long cursor;
   
   public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException 
      {
      Twitter bacon;
      IDs ids;
      StringBuilder tweet = new StringBuilder();
      PrintWriter out = response.getWriter();
      
      response.setContentType("text/html; charset=UTF-8");
      
      try 
         {    
         //get Oauth values from Datastore
         oauthEntity = datastore.get(KeyFactory.createKey("oauthEntity", "OA"));
         
         String oAuthConsumerKey = oauthEntity.getProperty("oAuthConsumerKey").toString();
         String oAuthConsumerSecret = oauthEntity.getProperty("oAuthConsumerSecret").toString();   
         String oAuthAccessToken = oauthEntity.getProperty("oAuthAccessToken").toString();
         String oAuthAccessTokenSecret = oauthEntity.getProperty("oAuthAccessTokenSecret").toString();
         
         //Get Spam values from Datastore
         spamEntity = datastore.get(KeyFactory.createKey("spamEntity", "SP"));    
         String host = spamEntity.getProperty("host").toString();
               
         String message = spamEntity.getProperty("message").toString();

         int frequency = Integer.parseInt(spamEntity.getProperty("frequency").toString());

        //Set up the Twitter object
         ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();     
         twitterConfigBuilder.setDebugEnabled(false);
         
         twitterConfigBuilder.setOAuthConsumerKey(oAuthConsumerKey);
         twitterConfigBuilder.setOAuthConsumerSecret(oAuthConsumerSecret);
         twitterConfigBuilder.setOAuthAccessToken(oAuthAccessToken);
         twitterConfigBuilder.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
          
         bacon = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
         
         //Retrieve stored cursor to track who we have messaged
         try
            {
            cursorEntity = datastore.get(KeyFactory.createKey("cursorEntity", "CE"));
            cursor = Long.parseLong(cursorEntity.getProperty("cursor").toString());
            }
         catch (EntityNotFoundException e) 
            {
            // Make new cursorEntity if we haven't spammed anyone yet
            out.println("Creating new cursorEntity for Datastore...<br />");
            cursorEntity = new Entity("cursorEntity", "CE");
            cursorEntity.setProperty("cursor", "-1");
            datastore.put(cursorEntity);
            cursor = -1L; //Set the cursor at -1 for this session
            }
         
         if(cursor == 0)
            {
            out.println("All followers have been notified.");
            return;
            }
         
         //Get the followers that we want to spam
         ids = bacon.getFollowersIDs(host, cursor, frequency);
       
         //Post the message along with a trend
         for (long id : ids.getIDs()) 
            {
            tweet.append("@");
            tweet.append(bacon.showUser(id).getScreenName());
            tweet.append(" ").append(message);
            
            //Get a trend using a WOEID from the list (change this list to match language of your tweet)
            int[] woeids = new int[] { 3444, 580778, 615702, 610264, 23424819 };
            Trends t = bacon.getPlaceTrends(woeids[r.nextInt(woeids.length)]);
            tweet.append(" ").append(t.getTrends()[r.nextInt(t.getTrends().length)].getName());
            
            /* Tweets are maximum 280 characters, so trim our sentence appropriately */
            if(tweet.length() > 280) 
               tweet.setLength(280);
            
            bacon.updateStatus(tweet.toString());
            out.println("Tweet posted successfully: " + tweet.toString());
            out.println("<br />");
            tweet = new StringBuilder(); // Clear out old tweet value
            }
        
         //Retrieve the cursor value for the next time!
         cursor = ids.getNextCursor();
         //Store the value of the cursor
         cursorEntity.setProperty("cursor", cursor);
         datastore.put(cursorEntity);     
         }
      
       catch(TwitterException e)
         {
         out.println("Problem with Twitter!");
         out.println("<br />");
         e.printStackTrace(response.getWriter());
         }
      catch (EntityNotFoundException e) 
         {
         out.println("Error finding Datastore Entities!");
         out.println("<br />"); 
         e.printStackTrace(response.getWriter()); 
         }
      catch(Exception e)
         {
        out.println("Error!!");
        out.println("<br />"); 
        e.printStackTrace(response.getWriter());
         }
      finally 
         {
         out.close();  // Always close the output writer
         }
      
      }
   
   // Redirect POST request to GET request.
   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response)
               throws IOException, ServletException 
      {
      doGet(request, response);
      }   
   }