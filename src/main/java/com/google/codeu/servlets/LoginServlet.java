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
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.Clock;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;


import com.google.api.services.calendar.Calendar;

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

  static{
    List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    // Load client secrets.
    InputStream in = LoginServlet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    //GoogleCredential credential = GoogleCredential.fromStream(in).createScoped(SCOPES); //for service account with service account keys
    if (in == null) {
      //throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
      System.err.println( "Resource not found: " + CREDENTIALS_FILE_PATH );
      System.exit(-1);
    }
    try{
      GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in)); //for oauth2 with client id file

      flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory( DATA_STORE_FACTORY )
        .setCredentialCreatedListener(new GoogleAuthorizationCodeFlow.CredentialCreatedListener() {
          @Override
          public void onCredentialCreated(Credential credential, TokenResponse tokenResponse) throws IOException {
          System.out.println("OnCreated: refresh_token = " + tokenResponse.getRefreshToken() );
            if( tokenResponse.getRefreshToken() == null ){
              System.err.println( "tokenResponse is null");
              return;
            }
            UserService userService = UserServiceFactory.getUserService();
            String userId = userService.getCurrentUser().getUserId();
            DATA_STORE_FACTORY.getDataStore("user").set(userId+"_token", tokenResponse.getRefreshToken());
          }
        }).addRefreshListener(new CredentialRefreshListener() {
          @Override
          public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
          System.out.println("OnRefreshed: refresh_token = " + tokenResponse );
          System.out.println( "Credential from flow:\n\tRefreshToken = " + credential.getRefreshToken()
              + "\n\tExpireTime = " + credential.getExpirationTimeMilliseconds());
            if( tokenResponse.getRefreshToken() == null ){
              System.err.println( "tokenResponse is null");
              return;
            }
            UserService userService = UserServiceFactory.getUserService();
            String userId = userService.getCurrentUser().getUserId();
            DATA_STORE_FACTORY.getDataStore("user").set(userId+"_token", tokenResponse.getRefreshToken());
          }

          @Override
          public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
            System.err.println("OAuth2 Token Error" + tokenErrorResponse );
            UserService userService = UserServiceFactory.getUserService();
            String userId = userService.getCurrentUser().getUserId();
            DATA_STORE_FACTORY.getDataStore("user").delete(userId+"_token");
          }
        }).setAccessType("offline").build();
    }catch(IOException e){
    }

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

  /*
      userId is from userService of Google Account.
      Check if this user has authorized correctly.
  */
  public boolean isAuthorized( String userId ) throws IOException{
    if(userId == null) return false;
    Credential credential = flow.loadCredential(userId);
    if( credential == null ) return false;  //Authorized?
    if( credential.refreshToken() == false ) return false;  //Correctly authorized?
    return true;
  }

  /*
      Get Calendar for requesting calendar data of the user defined by userId.
   */
  public Calendar getCalendar( String userId )
    throws IOException, FileNotFoundException{
      Credential credential = flow.loadCredential(userId);
      if( isAuthorized( userId ) == false ) return null;
      return new Calendar.Builder(
          HTTP_TRANSPORT, JSON_FACTORY, flow.loadCredential(userId))
        .setApplicationName("CodeU team49")
        .build();
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
