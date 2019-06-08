/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.servlets;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import java.net.URL;
import java.util.regex.Matcher; // video 
import java.util.regex.Pattern;
import java.io.FileNotFoundException;// valid
import java.io.FileReader;
import javax.script.*;
import org.apache.commons.validator.routines.UrlValidator;
import javax.swing.JOptionPane;
/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {
  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Message} data for a specific user. Responds with
   * an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    String user = request.getParameter("user");

    if (user == null || user.equals("")) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    List<Message> messages = datastore.getMessages(user);
    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.none());
        
    String regex = "(https?://\\S+\\.(png|jpg|gif))";
    String replacement = "<img src=\"$1\" />";
    String textWithImagesReplaced = userText.replaceAll(regex, replacement);
    int size = textWithImagesReplaced.length();
    char[] charArray = textWithImagesReplaced.toCharArray();
    int count=0;
    for (int i = 0; i < size; i++) {
         if(charArray[i]!='<'){
              count++;
         }else{
              break;
         }
    }
     String text ="";
    if(count == textWithImagesReplaced.length()) text = textWithImagesReplaced;
    if(count!=0){
      text = textWithImagesReplaced.substring(0,count);
    }else{
      text = textWithImagesReplaced;
    }
    System.out.println(textWithImagesReplaced.length()+"leg");
    System.out.println(count+"count");
    String img = textWithImagesReplaced.substring(count,textWithImagesReplaced.length());
    //String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    System.out.println(text+"textbefore");
    System.out.println(img+"imagelink");
    
    // if input is text  +  invalid_img -> wrong
    // input is invalid_img ->print the url
   if(img.length() > 0 ){ // has some img , test it
        String testlink = img.replace("<img src=", "");
        String seclink = testlink.replace(" />","");
        String newlink = seclink.substring(1,seclink.length()-1);
        if (urlValidator(newlink)) { // right url (with or without text)
          System.out.println("The url " + newlink + " is valid");
        }else{ // only invalid url
          textWithImagesReplaced = "";
        }
    }else{
      textWithImagesReplaced = text;
      System.out.println("The url is not valid");
    }
    
    Message message = new Message(user, textWithImagesReplaced);
    datastore.storeMessage(message);
    response.sendRedirect("/user-page.html?user=" + user);

  }

  public boolean urlValidator(String url)
	{
		// Get an UrlValidator using default schemes
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url);
	}
}