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

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class TwitterSpammerServlet extends HttpServlet 
   {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   public static final Random r = new Random();

   private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   private static Entity oauthEntity,spamEntity;
   
   public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException 
      {

      
      PrintWriter out = response.getWriter();
      
      response.setContentType("text/html; charset=UTF-8");
      
  
      try 
         {         
            //Get Twitter OAuth values from the form and save them to Datastore
            oauthEntity = new Entity("oauthEntity", "OA");
            
            String oAuthConsumerKey = request.getParameter("OAuthConsumerKey");      
            oauthEntity.setProperty("oAuthConsumerKey", oAuthConsumerKey);

            String oAuthConsumerSecret = request.getParameter("OAuthConsumerSecret");
            oauthEntity.setProperty("oAuthConsumerSecret", oAuthConsumerSecret);
            
            String oAuthAccessToken = request.getParameter("OAuthAccessToken");
            oauthEntity.setProperty("oAuthAccessToken", oAuthAccessToken);

            String oAuthAccessTokenSecret = request.getParameter("OAuthAccessTokenSecret");
            oauthEntity.setProperty("oAuthAccessTokenSecret", oAuthAccessTokenSecret);

            datastore.put(oauthEntity);
            
            //Get Spamming values from the form and save them to Datastore
            
            spamEntity = new Entity("spamEntity", "SP");
           
            String host = request.getParameter("host");
            spamEntity.setProperty("host", host);
            
            String message = request.getParameter("message");
            spamEntity.setProperty("message", message);

            spamEntity.setProperty("frequency", request.getParameter("frequency"));

            datastore.put(spamEntity);
            
            out.println("<p>Parameters set. Twitter spammer cron job is <a href=\"/cron/CronSpammerServlet\">running here</a>.<br />");
         }
      
      catch(DatastoreFailureException e)
         {
        out.println("Problem with DataStore!");
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