package com.google.codeu.servlets;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles fetching all messages for the public feed.
 */
@WebServlet("/taglist")
public class MessageTagServlet extends HttpServlet {
  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of Message data for all users.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    List<Message> messages = datastore.getAllMessages();
    List<String> tags = messages.stream().filter(message -> message.getText().matches(".*#\\S+.*")).flatMap(message -> {
      List<String> allMatches = new ArrayList<>();
      Matcher m = Pattern.compile("#\\S+")
              .matcher(message.getText());
      while (m.find()) {
        allMatches.add(m.group());
      }
      return allMatches.stream();
    }).distinct().collect(Collectors.toList());
    Gson gson = new Gson();
    String json = gson.toJson(tags);

    response.getOutputStream().println(json);
  }
}
