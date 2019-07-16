package com.google.codeu.data;

import java.util.UUID;

public class Task {
  private UUID id;
  private String userId;
  private String summary;
  private long timestamp;

  public Task(String userId, String summary) {
    this( UUID.randomUUID(), userId, summary, System.currentTimeMillis());
  }

  public Task(UUID id, String userId, String summary, long timestamp) {
    this.id = id;
    this.userId = userId;
    this.summary = summary;
    this.timestamp = timestamp;
  }
  public UUID getId() {
    return id;
  }
  public String getUserId() {
    return userId;
  }
  public String getSummary() {
    return summary;
  }
  public long getTimestamp() {
    return timestamp;
  }
}
