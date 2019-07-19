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
import com.google.appengine.api.datastore.Entity;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Tag;

import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
/** Handles fetching and saving {@link Message} instances. */
@WebServlet({"/dashboard/tags", "/dashboard/tagManage"})
public class TagServlet extends HttpServlet {
  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Task} data for a specific user. Responds with
   * an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if("/dashboard/tagManage".equals( request.getRequestURI() ) ){
      return;
    }
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    if (userId == null || userId.equals("")) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    Date currDate = new Date();
    List<Tag> tags = datastore.getTags(userId);
    for(Tag tag: tags){
      if(tag.getEventDateTime() <= currDate.getTime() ){
        datastore.deleteTag(tag);
      }
    }
    tags = datastore.getTags(userId);
    Gson gson = new Gson();
    String json = gson.toJson(tags);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if("/dashboard/tags".equals( request.getRequestURI() ) ){
      return;
    }

    if( checkParam(request, "method") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid method\"}");
      return;
    }

    if( "update".equals( request.getParameter("method")) ) updateTag(request, response);
    else if( "delete".equals( request.getParameter("method")) ) deleteTag(request, response);

  }

  /*
    Create/Update a tag.
    @param tag, String
    @param eventDateTime, String in ISO

    @return JSON - tag/error
   */
  private void updateTag(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");

    if( checkParam(request, "tag") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid tag\"}");
      return;
    }
    if( checkParam(request, "eventDateTime") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid eventDateTime\"}");
      return;
    }
    String tagName = Jsoup.clean( request.getParameter("tag"), Whitelist.none());
    DateTime eventDateTime = new DateTime( Jsoup.clean( request.getParameter("eventDateTime"), Whitelist.none()) );
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    Tag tag = new Tag(userId, tagName, eventDateTime.getValue());
    Entity tagEntity = datastore.getTagEntity(userId, tagName);
    if( tagEntity != null ){
      if( (long) tagEntity.getProperty("eventDateTime") < eventDateTime.getValue() ){
        tagEntity.setProperty("eventDateTime", eventDateTime.getValue());
      }
      try{
        datastore.update(tagEntity);
      }catch( IllegalArgumentException e ){
        System.err.printf("We got a fake tag entity!!");
        System.err.printf(e.toString());
      }
    }else datastore.storeTag(tag);

    Gson gson = new Gson();
    String json = gson.toJson(tag);

    response.getWriter().println(json);
  }

  /*
    Delete a certain tag under current user
    @parm tag, Stirng

    @return succeed message
   */
  private void deleteTag(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");

    if( checkParam(request, "tag") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid tag\"}");
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();
    String tag = Jsoup.clean( request.getParameter("tag"), Whitelist.none());
    datastore.deleteTag(UUID.nameUUIDFromBytes( (userId+tag).getBytes() ) );

    response.getOutputStream().println("{\"status\":\"Success\"}");
  }

  private boolean checkParam(HttpServletRequest request, String str){
    return request.getParameter( str ) != null && !request.getParameter( str ).equals("");
  }
}
