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
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.FileNotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import java.util.List;
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.servlets.CredentialServlet;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, FileNotFoundException{
    if (UserServiceFactory.getUserService().isUserLoggedIn() == false){
      response.sendRedirect("/login");
      return;
    }

    /*
       When returns null, it means this user hasn't logged in or authorized yet,
       and redirects this user to those pages.
    */
    boolean isGet = true;
    Calendar service = getService( request, response,  isGet);
    if( service == null ) return; 

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
      System.err.println( "Calendar GET request fail:" + e );
      redirectToAuth( request, response );
      return;
    }

    response.setContentType("application/json");

    Gson gson = new Gson();
    //String json = gson.toJson(items);

    response.getOutputStream().println(events.toString());

  }

  /*
      Only accept one event/calendar update at one time.
      Value regulation: only accept plain text. //Would process here, just remind you whether you want to tell the user about it.
      @param from: This POST is from which site. E.g. "/index.html" or "/testabc.html"
      @param summary: Event title/summary.
      @param startDate: start date  
      @param startTime: start time  
      @param endDate: end date
      @param endTime: end time
      @param tags: Event tags, which will be stored in description as #abc or #[String].
                   Please use comma to seperate.
                   E.g. tags=a,b,c -> it would be processed as "#a #b #c" into description.
      
      optional - 
      @param calendar:  The name of calendar that user wants to update/insert.
                        If this calendar hasn't been created, it would be created here.
                        Null stands for primary.
      @param description: Event description.
      @param timezone: timezone of this event, default would be the user's time zone
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, FileNotFoundException{
    /*
       When returns null, it means this user hasn't logged in or authorized yet,
       and redirects this user to those pages.
    */
    boolean isGet = false;
    Calendar service = getService( request, response,  isGet);
    if( service == null ) return;

    String calendarID = null;
    if(request.getParameter("calendar").equals("")) calendarID = "primary";
    else{
      String calendarSummary = Jsoup.clean( request.getParameter("calendar"), Whitelist.none());
      // Iterate through entries in calendar list for calendarID
      String pageToken = null;
      do {
        try{
          CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
          List<CalendarListEntry> items = calendarList.getItems();

          pageToken = calendarList.getNextPageToken();
          for (CalendarListEntry calendarListEntry : items) {
            if( calendarListEntry.getSummary().equals(calendarSummary) ) calendarID = calendarListEntry.getId();
          }
        }catch( GoogleJsonResponseException e ){
          System.err.println("Calendar POST calendarList request fail - " + e);
          redirectToAuth( request, response );
          return;
        }
      } while (pageToken != null && calendarID == null );
    }

    System.out.println("calendarID = " + calendarID);

    String eventSummary = Jsoup.clean( request.getParameter("summary"), Whitelist.none());
    String[] tags = Jsoup.clean( request.getParameter("tags"), Whitelist.none()).split("\\s*,\\s*"); //regex expression for "   ,   " or ","
    StringBuilder description = new StringBuilder( Jsoup.clean( request.getParameter("description"), Whitelist.none()) );
    Arrays.asList(tags).forEach((tag) -> description.append(" #"+tag));

    System.out.println("description = " + description);

    /*
    Event event = new Event()
                  .setSummary( eventSummary)
                  //.setLocation("800 Howard St., San Francisco, CA 94103")
                  .setDescription( description.toString() );

    DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
    EventDateTime start = new EventDateTime()
      .setDateTime(startDateTime)
      .setTimeZone("America/Los_Angeles");
    event.setStart(start);

    DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
    EventDateTime end = new EventDateTime()
      .setDateTime(endDateTime)
      .setTimeZone("America/Los_Angeles");
    event.setEnd(end);

    String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
    event.setRecurrence(Arrays.asList(recurrence));

    EventAttendee[] attendees = new EventAttendee[] {
      new EventAttendee().setEmail("lpage@example.com"),
          new EventAttendee().setEmail("sbrin@example.com"),
    };
    event.setAttendees(Arrays.asList(attendees));

    EventReminder[] reminderOverrides = new EventReminder[] {
      new EventReminder().setMethod("email").setMinutes(24 * 60),
          new EventReminder().setMethod("popup").setMinutes(10),
    };
    Event.Reminders reminders = new Event.Reminders()
      .setUseDefault(false)
      .setOverrides(Arrays.asList(reminderOverrides));
    event.setReminders(reminders);

    String calendarId = "primary";
    event = service.events().insert(calendarId, event).execute();
    System.out.printf("Event created: %s\n", event.getHtmlLink());
    */

  }

  private void redirectToAuth(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String from = "/calendar";
    StringBuilder refererURL = new StringBuilder( from  );
    String query = request.getQueryString();
    if (query != null && request.getParameter("code") == null ) {
      refererURL.append('?').append(query);
    }
    response.sendRedirect("/credential?referer=" + refererURL );
  }

  private Calendar getService(HttpServletRequest request, HttpServletResponse response, boolean isGet)
    throws IOException{
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();

    if( userId == null ){
      response.sendRedirect("/login");
      return null;
    }

    if( request.getSession().getAttribute("authorized") == null ||
        !((boolean) request.getSession().getAttribute("authorized")) ){
      if(isGet) redirectToAuth(request, response);
      else{
        //We shouldn't forward POST, which may cause unintended re-POSTs
        String from = request.getParameter("from");
        if(from.equals("")) from = "index.html";
        response.sendRedirect("/credential?referer=" + from);  
      }
      return null;
    }

    return CredentialServlet.getCalendar(userId);
  }
}
