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
import com.google.codeu.utils.QuerySlices;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
*/
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;


import com.google.api.services.calendar.Calendar;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;//why .java6?

/**
 * Redirects the user to the Google login page or their page if they're already logged in.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  private static final String CREDENTIALS_FILE_PATH = "/client_secret_Calendar_Events.json";
  private static final AppEngineDataStoreFactory DATA_STORE_FACTORY = AppEngineDataStoreFactory.getDefaultInstance();//Not sure about whether it's global version.
  private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance(); //JsonFactory is an abstract class, so here needs a subclass for it.

  //The flow is the overall class for google authorization classes and methods.
  private static GoogleAuthorizationCodeFlow flow = null; 

  public LoginServlet() throws FileNotFoundException, IOException{
    List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    // Load client secrets.
    InputStream in = LoginServlet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    //GoogleCredential credential = GoogleCredential.fromStream(in).createScoped(SCOPES); //for service account with service account keys
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in)); //for oauth2 with client id file

    flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory( DATA_STORE_FACTORY )
      /*  this part is dead. I thought it's great to use but didn't solve the error.
      .setCredentialCreatedListener(new AuthorizationCodeFlow.CredentialCreatedListener() {
        @Override
        public void onCredentialCreated(Credential credential, TokenResponse tokenResponse) throws IOException {
          DATA_STORE_FACTORY.getDataStore("user").set("id_token", tokenResponse.get("id_token").toString());
        }
      })
      .addRefreshListener(new CredentialRefreshListener() {
        @Override
        public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
          DATA_STORE_FACTORY.getDataStore("user").set("id_token", tokenResponse.get("id_token").toString());
        }

        @Override
        public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
          //handle token error response
        })
        */
      .setAccessType("offline").build(); //Not sure about the access type

  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    // If the user is already logged in, redirect to their page
    if (userService.isUserLoggedIn()) {
      String user = userService.getCurrentUser().getEmail();
      String userId = userService.getCurrentUser().getUserId();


      // If the user has already authorized, redirect to their page
      if( isAuthorized(userId) == false ){
        /* 
           The Authorization method will redirect to this page with GET query string
           If this user has authorized but not been recorded, it will direct to get the access code.
        */
        String query = request.getQueryString();
        if(query == null){
          String OAuth2Url = flow.newAuthorizationUrl()
                                .setRedirectUri(request.getRequestURL().toString())
                                .build(); //build for string. otherwise, it'd just be object.
          response.sendRedirect(OAuth2Url);
          return;
        }
        QuerySlices slices = new QuerySlices( query );  //Should we check whether the return tokens match the format from document?
        TokenResponse tokenResponse = requestAccessToken( request.getRequestURL().toString(), slices.get("code") );

        flow.createAndStoreCredential(tokenResponse, userId);
        response.sendRedirect("/user-page.html?user=" + user);
        return;
      }
      
      //When using response.sendRedirect, saving(?) somethings like response.getOutputStream().println(json) will make a bug like it has been committed.
      response.sendRedirect("/user-page.html?user=" + user);  
      return;
    }

    // Redirect to Google login page. That page will then redirect back to /login,
    // which will be handled by the above if statement.
    String googleLoginUrl = userService.createLoginURL("/login");
    response.sendRedirect(googleLoginUrl);
  }

  public boolean isAuthorized( String userId ) throws IOException{
    if (flow.loadCredential(userId) == null) return false;
    return true;
  }

  public Calendar getCalendar( String userId )
    throws IOException, FileNotFoundException{
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(-1).build(); //-1 for unused port
    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        new AuthorizationCodeInstalledApp(flow, receiver).authorize(userId) //The problem
        )
      //.setApplicationName(APPLICATION_NAME) //saved until deciding whether it's necessary or ..
      .build();

  }

  public HttpRequestInitializer getRequestInitializer(){
    return flow.getRequestInitializer();
  }

  /********Private functions********/

  private TokenResponse requestAccessToken( String RedirectUri, String code ) 
    throws TokenResponseException, IOException{
    try {
      return flow.newTokenRequest( code )
                .setRedirectUri( RedirectUri )
                .execute();
    }catch (TokenResponseException e) {
     //I think this error should stop this user to login until fixed.
      if (e.getDetails() != null) {
        System.err.println("Error: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          System.err.println(e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          System.err.println(e.getDetails().getErrorUri());
        }
      } else {
        System.err.println(e.getMessage());
      }
    }
    return null;
  }
}
