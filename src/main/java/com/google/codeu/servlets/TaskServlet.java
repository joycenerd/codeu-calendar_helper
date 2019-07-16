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
import com.google.codeu.data.Task;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
/** Handles fetching and saving {@link Message} instances. */
@WebServlet({"/dashboard/tasks", "/dashboard/taskManage"})
public class TaskServlet extends HttpServlet {
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
    if("/dashboard/taskManage".equals( request.getRequestURI() ) ){
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

    List<Task> tasks = datastore.getTasks(userId);
    Gson gson = new Gson();
    String json = gson.toJson(tasks);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if("/dashboard/tasks".equals( request.getRequestURI() ) ){
      return;
    }

    if( checkParam(request, "method") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid method\"}");
      return;
    }

    if( "add".equals( request.getParameter("method")) ) addTask(request, response);
    else if( "delete".equals( request.getParameter("method")) ) deleteTask(request, response);

  }

  private void addTask(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");

    if( checkParam(request, "summary") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid summary\"}");
      return;
    }
    String summary = Jsoup.clean( request.getParameter("summary"), Whitelist.none());
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    Task task = new Task(userId, summary);
    datastore.storeTask(task);

    Gson gson = new Gson();
    String json = gson.toJson(task);

    response.getWriter().println(json);
  }

  private void deleteTask(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");

    if( checkParam(request, "id") == false ){
      response.getOutputStream().println("{\"error\":\"Invalid id\"}");
      return;
    }
    String id = Jsoup.clean( request.getParameter("id"), Whitelist.none());
    datastore.deleteTask(UUID.fromString(id));

    response.getOutputStream().println("{\"status\":\"Success\"}");
  }

  private boolean checkParam(HttpServletRequest request, String str){
    return request.getParameter( str ) != null && !request.getParameter( str ).equals("");
  }
}
