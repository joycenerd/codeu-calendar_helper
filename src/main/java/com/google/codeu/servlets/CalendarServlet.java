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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserService;

import java.io.IOException;
import java.io.FileNotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.servlets.CredentialServlet;

@WebServlet("/dashboard/calendar")
public class CalendarServlet extends HttpServlet {
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

  /*
    Get events of one/all calendar(s) from start time to end time with limited/unlimited maximum number.
    @param from: This POST is from which site. E.g. "/index.html" or "/testabc.html"
    
    optional - 
    @param calendar:  The name of calendar that user asks for events.
                      Null stands for all calendars.
    @param orderBy:   The order of the resulats. Default is stable order.
                      E.g. "startTime"(only for single events) or "updated"
    @param maxResults: The number of max results. Default is unlimited.
    @param timeMin:   The earliest time of return event. Default is from now. Require RFC3339 format.
    @param timeMax:   The latest time of return event.  Default is unlimited. Require RFC3339 format.
    @param timezone:  Return events in timezone. Default is where this user is.
    @param prettyPrint: true/false. Resaults will contain indentataion and line breaks.
    @param tag: For search certain events that contains this tag, which is stored in the description. E.g. "abc" or "train"
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, FileNotFoundException{

    /*
       When returns null, it means this user hasn't logged in or authorized yet,
       and redirects this user to those pages.
    */
    boolean isGet = true;
    Calendar service = getService( request, response,  isGet);
    if( service == null ) return; 

    List<String> calendarIDs = new ArrayList<>();
    String calendarSummary = null;
    if( checkParam(request, "calendar") ) calendarSummary = Jsoup.clean( request.getParameter("calendar"), Whitelist.none());
    // Iterate through entries in calendar list for calendarID
    String pageToken = null;
    do {
      try{
        CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
        List<CalendarListEntry> items = calendarList.getItems();

        pageToken = calendarList.getNextPageToken();
        for (CalendarListEntry calendarListEntry : items) {
          if( calendarSummary == null ) calendarIDs.add( calendarListEntry.getId() );
          else if( calendarListEntry.getSummary().equals(calendarSummary) ){
            calendarIDs.add( calendarListEntry.getId() );
            pageToken = null;
            break;
          }
        }
      }catch( GoogleJsonResponseException e ){
        System.err.println("Calendar POST calendarList request fail - " + e);
        //redirectToAuth( request, response );
        errMessage( response, "/dashboard/credential", "Authorizaion failed");
        return;
      }
    } while (pageToken != null );

    DateTime timeMin = new DateTime(System.currentTimeMillis());
    if( checkParam(request, "timeMin") ) timeMin = new DateTime( request.getParameter("timeMin") );

    Calendar.Events.List list = service.events().list(calendarIDs.get(0)).setTimeMin(timeMin);
    if( checkParam(request, "orderBy") ) list.setOrderBy( request.getParameter("orderBy") );
    if( checkParam(request, "maxResults") ){
      try{
        Integer maxResults = Integer.parseInt( request.getParameter("maxResults") );
        list.setMaxResults( maxResults );
      }catch(NumberFormatException e){
        System.err.println("@param maxResults in Calendar doGet(): " + e );
      }
    }
    DateTime timeMax = null;
    if( checkParam(request, "timeMax") ){
      timeMax = new DateTime( request.getParameter("timeMax") );
      list.setTimeMax( timeMax );
    }
    String timezone = null;
    if( checkParam(request, "timezone") ){
      timezone = request.getParameter("timezone");
      list.setTimeZone( timezone );
    }
    if( checkParam(request, "prettyPrint") ) list.setPrettyPrint( Boolean.parseBoolean( request.getParameter("prettyPrint")));
    String tag = null;
    if( checkParam(request, "tag") ) list.setQ( "#" + request.getParameter("tag") );

    List<Event> eventList = new ArrayList<Event>(); //ArrayList is not synchronized not like vector. It works better in multiple threads
    for(int i = 0; i < calendarIDs.size(); ++i){
      list.setCalendarId( calendarIDs.get(i) );
      try{
        // Iterate over the events in the specified calendar
        pageToken = null;
        do {
          Events events = list.setPageToken(pageToken).execute();

          for( Event event: events.getItems() ){
            if(event.getRecurrence() != null){ //get Instances from a recurrent event
              Calendar.Events.Instances instances = service.events().
                instances(calendarIDs.get(i), event.getId())
                .setTimeMin(timeMin);
              if(timeMax != null) instances.setTimeMax( timeMax );
              if(timezone != null) instances.setTimeZone( timezone );

              eventList.addAll( getInstances( instances ) );
              
            }else eventList.add( event );
          }

          pageToken = events.getNextPageToken();
        } while (pageToken != null);
      }catch( GoogleJsonResponseException e ){
        System.err.println( "Calendar GET request fail:" + e );
        //redirectToAuth( request, response );
        /*
        String from = request.getParameter("from");
        if(from.equals("")) from = "index.html";
        */
        errMessage( response, "/dashboard/credential", "Authorizaion failed");
        return;
      }
    }



    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(eventList.toString());

    //Gson gson = new Gson();
    //String json = gson.toJson(eventList);


  }

  /*
      Only accept one event/calendar update at one time.
      Value regulation: only accept plain text. //Would process here, just remind you whether you want to tell the user about it.
      @param summary: Event title/summary.
      @param startDateTime: start date time in RFC3339 format
      //@param startDate: start date  
      //@param startTime: start time   - null for whole day event 
      @param endDateTime: end date time in RFC3339 format
      //@param endDate: end date
      //@param endTime: end time  - null for whole day event 
      
      optional - 
      @param calendar:  The name of calendar that user wants to update/insert.
                        If this calendar hasn't been created, it would be created here.
                        Null stands for primary.
      @param description: Event description.
      @param timezone: timezone of this event, default would be the user's time zone
                       The time zone in which the time is specified. (Formatted as an IANA Time Zone Database name, e.g. "Europe/Zurich".) For recurring events this field is required and specifies the time zone in which the recurrence is expanded. For single events this field is optional and indicates a custom time zone for the event start/end.
      @param tags: Event tags, which will be stored in description as #abc or #[String].
                   Please use comma to seperate.
                   E.g. tags=a,b,c -> it would be processed as "#a #b #c" into description.

      @return - printed in the site
        Succeed - an event's details
        Fail    - **unclear, needs to be found out.
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
    if( request.getParameter("calendar") == null || request.getParameter("calendar").equals("")) calendarID = "primary";
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
          //redirectToAuth( request, response );
          errMessage( response, "/dashboard/credential", "Authorizaion failed");
          return;
        }
      } while (pageToken != null && calendarID == null );
    }

    System.out.println("calendarID = " + calendarID);

    if( checkParam(request, "summary") == false ){
      throw new IllegalArgumentException("Missed summary in Calendar POST request");
    }
    String eventSummary = Jsoup.clean( request.getParameter("summary"), Whitelist.none());
    String[] tags = {};
    if( checkParam(request, "tags") ) tags = Jsoup.clean( request.getParameter("tags"), Whitelist.none()).split("\\s*,\\s*"); //regex expression for "   ,   " or ","
    StringBuilder description = new StringBuilder( Jsoup.clean( request.getParameter("description"), Whitelist.none()) );
    Arrays.asList(tags).forEach((tag) -> description.append(" #"+tag));

    Event event = new Event()
                  .setSummary( eventSummary)
                  //.setLocation("800 Howard St., San Francisco, CA 94103")
                  .setDescription( description.toString() );

    String timezone = request.getParameter("timezone");
    if( timezone == null ) timezone = "";
    timezone = Jsoup.clean( timezone, Whitelist.none() );
    DateTime startDateTime = new DateTime( Jsoup.clean( request.getParameter("startDateTime"), Whitelist.none()) );
    EventDateTime start = new EventDateTime()
      .setDateTime(startDateTime)
      .setTimeZone( null );
    if( !timezone.equals("") ) start.setTimeZone( timezone );
    event.setStart(start);

    DateTime endDateTime = new DateTime(Jsoup.clean( request.getParameter("endDateTime"), Whitelist.none()));
    EventDateTime end = new EventDateTime()
      .setDateTime(endDateTime)
      .setTimeZone( null );
    if( !timezone.equals("") ) start.setTimeZone( timezone );
    event.setEnd(end);

    /*
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
    */

    try{
      event = service.events().insert(calendarID, event).execute();
    }catch(GoogleJsonResponseException e){
      System.out.println(e);
      errMessage( response, "/dashboard/credential", "Authorizaion failed");
      return;
    }
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson( event );
    response.getOutputStream().println(json);

  }

  /* Deprecated 
  private void redirectToAuth(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.getOutputStream().println("[]");
    String from = "/calendar";
    StringBuilder refererURL = new StringBuilder( from  );
    String query = request.getQueryString();
    if (query != null) {
      refererURL.append('?').append(query.replace("&", "%26")); //Encode the URL
    }
    response.sendRedirect("/credential?referer=" + refererURL );
  }
  */

  private Calendar getService(HttpServletRequest request, HttpServletResponse response, boolean isGet)
    throws IOException{
    UserService userService = UserServiceFactory.getUserService();

    if(!userService.isUserLoggedIn()){
      //response.sendRedirect("/login");
      errMessage( response, "/login", "User hasn't logged.");
      return null;
    }
    String userId = userService.getCurrentUser().getUserId();

    if( userId == null ){
      //response.sendRedirect("/login");
      errMessage( response, "/login", "User hasn't logged.");
      return null;
    }

    if( request.getSession().getAttribute("authorized") == null ||
        !((boolean) request.getSession().getAttribute("authorized")) ){
      errMessage( response, "/dashboard/credential", "Authorizaion failed");
      /*
      if(isGet) redirectToAuth(request, response);
      else{
        //We shouldn't forward POST, which may cause unintended re-POSTs
        String from = request.getParameter("from");
        if(from.equals("")) from = "index.html";
        response.sendRedirect("/credential?referer=" + from);  
      }
      */
      return null;
    }

    return CredentialServlet.getCalendar(userId);
  }
  private boolean checkParam(HttpServletRequest request, String str){
    return request.getParameter( str ) != null && !request.getParameter( str ).equals("");
  }
  private void errMessage( HttpServletResponse response, String to, String message )
    throws IOException{
    //response.sendError( response.SC_UNAUTHORIZED );
    response.getOutputStream().println("{\"to\":\""+to+"\",\"error\":\""+message+"\"}");
  }
  private List<Event> getInstances(Calendar.Events.Instances instances )
      throws GoogleJsonResponseException, IOException{
        List<Event> retList = null;
        String pageToken = null;
        do {
          Events events = instances.setPageToken(pageToken).execute();
          if( retList == null ) retList = events.getItems();
          else retList.addAll( events.getItems() );
          pageToken = events.getNextPageToken();
        } while (pageToken != null);
  
        return retList;
  }
}
