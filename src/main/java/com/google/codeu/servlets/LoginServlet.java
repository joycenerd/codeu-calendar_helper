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
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.calendar.Calendar;

import com.google.codeu.servlets.CredentialServlet;
/**
 * Redirects the user to the Google login page or their page if they're already logged in.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    System.out.println("I'm in Login!");
    // If the user is already logged in, redirect to their page
    if (userService.isUserLoggedIn()) {
      String user = userService.getCurrentUser().getEmail();
      String userId = userService.getCurrentUser().getUserId();

      if( request.getSession().getAttribute("authorized") == null ||
          !((boolean) request.getSession().getAttribute("authorized")) ){
        response.sendRedirect("/dashboard/credential");  
        return;
      }
      
      // If the user has already authorized, redirect to dashboard
      response.sendRedirect("/dashboard.html");  
      return;
    }

    // Redirect to Google login page. That page will then redirect back to /login,
    // which will be handled by the above if statement.
    String googleLoginUrl = userService.createLoginURL("/login");
    response.sendRedirect(googleLoginUrl);
  }
}
