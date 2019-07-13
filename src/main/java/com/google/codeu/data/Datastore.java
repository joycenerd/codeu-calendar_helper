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

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Provides access to the data stored in Datastore. */
public class Datastore {
  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("timestamp", message.getTimestamp());

    datastore.put(messageEntity);
  }
  public int getTotalMessageCount() {
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }
  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String user) {
    List<Message> messages = new ArrayList<>();

    Query query = new Query("Message")
                      .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
                      .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  /**
   * Gets all messages stored in database.
   *
   * @return a list of messages posted, or empty list if no one has ever posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getAllMessages() {
    List<Message> messages = new ArrayList<>();

    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty("user");
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  /**
   * Gets all Users stored in database.
   *
   * @return a Set of Strings indicating Users who has posted, or empty list if no one has posted.
   *
   */
  public Set<String> getUsers() {
    Set<String> users = new HashSet<>();
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      users.add((String) entity.getProperty("user"));
    }
    return users;
  }

  // Store the User in DataStore

  public void storeUser(User user) {
    Entity userEntity = new Entity("User", user.getEmail());
    userEntity.setProperty("email", user.getEmail());
    userEntity.setProperty("aboutMe", user.getAboutMe());
    datastore.put(userEntity);
  }

  // Returns the User owned by the email address, or NULL if no matching was found
  public User getUser(String email) {
    Query query = new Query("User").setFilter(
        new Query.FilterPredicate("email", FilterOperator.EQUAL, email));
    PreparedQuery results = datastore.prepare(query);
    Entity usereEntity = results.asSingleEntity();
    if (usereEntity == null) {
      return null;
    }
    String aboutMe = (String) usereEntity.getProperty("aboutMe");
    User user = new User(email, aboutMe);
    return user;
  }

  public void storeTask(Task task) {
    Entity taskEntity = new Entity("Task", task.getId().toString());
    taskEntity.setProperty("userId", task.getUserId());
    taskEntity.setProperty("summary", task.getSummary());
    taskEntity.setProperty("timestamp", task.getTimestamp());

    datastore.put(taskEntity);
  }
  public void deleteTask(UUID taskId) {
    datastore.delete(KeyFactory.createKey("Task", taskId.toString()));
  }
  /**
   * Gets tasks posted by a specific user.
   *
   * @return a list of tasks created by the user, or empty list if user has never created a
   *     task. List is sorted by added time ascending.
   */
  public List<Task> getTasks(String userId) {
    List<Task> tasks = new ArrayList<>();

    Query query = new Query("Task")
                      .setFilter(new Query.FilterPredicate("userId", FilterOperator.EQUAL, userId))
                      .addSort("timestamp", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    // asIterable is more efficient than asList
    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String summary = (String) entity.getProperty("summary");
        long timestamp = (long) entity.getProperty("timestamp");

        Task task = new Task(id, userId, summary, timestamp);
        tasks.add(task);
      } catch (Exception e) {
        System.err.println("Error reading tasks.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return tasks;
  }
}
