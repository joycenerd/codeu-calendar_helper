package com.google.codeu.servlets;

import com.google.api.client.http.HttpRequestInitializer;
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
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.servlets.LoginServlet;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  private Calendar service = null;
  private LoginServlet Login = null;
  private String userId = null;

  public CalendarServlet() throws IOException, FileNotFoundException{
    Login = new LoginServlet();
    userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    service = Login.getCalendar(userId);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, FileNotFoundException{
    if (UserServiceFactory.getUserService().isUserLoggedIn() == false){
      response.sendRedirect("/login");
      return;
    }

    if( Login.isAuthorized(userId) == false){
      response.sendRedirect("/login");
      return;
    }

    //List<Event> items = null;
    // Build a new authorized API client service.

    // List the next 10 events from the primary calendar.
    DateTime now = new DateTime(System.currentTimeMillis());
    if(service == null) System.out.println("angry bird");
    Events events = service.events().list("primary")
        .setMaxResults(10)
        .setTimeMin(now)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
      //items = events.getItems();

    response.setContentType("application/json");

    Gson gson = new Gson();
    //String json = gson.toJson(items);

    response.getOutputStream().println(events.toString());

  }
}
