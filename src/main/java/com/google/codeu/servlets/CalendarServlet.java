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
 *
 * Usage:
 *   - Redirect to this page to get the data of one's next 10 events.
 *   - Future features: Options through GET parameters.
 */
package com.google.codeu.servlets;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;    //Different from Calendar.Event which is a request
import com.google.api.services.calendar.model.Events;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.FileNotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import java.util.List;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

import com.google.codeu.servlets.CredentialServlet;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  private Calendar service = null;
  private String userId = null;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, FileNotFoundException{
    if (UserServiceFactory.getUserService().isUserLoggedIn() == false){
      response.sendRedirect("/login");
      return;
    }

    userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();

    if( userId == null ){
      response.sendRedirect("/login");
      return;
    }

    if( request.getSession().getAttribute("authorized") == null ||
        !((boolean) request.getSession().getAttribute("authorized")) ){
      forwardToAuth(request, response);
      return;
    }

    service = CredentialServlet.getCalendar(userId);

    //List<Event> items = null;
    // Build a new authorized API client service.

    // List the next 10 events from the primary calendar.
    DateTime now = new DateTime(System.currentTimeMillis());
    if(service == null){
      System.err.println("Calendar Service is null");
      return;
    }
    Events events;
    try{
      events = service.events().list("primary")
          .setMaxResults(10)
          .setTimeMin(now)
          .setOrderBy("startTime")
          .setSingleEvents(true)
          .execute();
        //items = events.getItems();
    }catch( GoogleJsonResponseException e ){
      System.err.println( "Calendar request fail:" + e );
      forwardToAuth( request, response );
      return;
    }

    response.setContentType("application/json");

    Gson gson = new Gson();
    //String json = gson.toJson(items);

    response.getOutputStream().println(events.toString());

  }


  private void forwardToAuth(HttpServletRequest request, HttpServletResponse response) throws IOException{
    try{
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/credential");
      request.setAttribute("from", "/calendar");
      dispatcher.forward(request, response);
      return;
    }catch(javax.servlet.ServletException e){
      System.err.println( "Calendar forward failed: " + e);
      response.sendRedirect("/index.html");
      return;
    }
  }
}
