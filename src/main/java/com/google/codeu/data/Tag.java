package com.google.codeu.data;

import java.util.UUID;

public class Tag {
  private UUID id;
  private String userId;
  private String tag;
  private long eventDateTime;
  private long timestamp;

  public Tag(String userId, String tag, long eventDateTime) {
    this( userId, tag, eventDateTime, System.currentTimeMillis());
  }

  public Tag(String userId, String tag, long eventDateTime, long timestamp) {
    this.userId = userId;
    this.tag = tag;
    this.eventDateTime = eventDateTime;
    this.timestamp = timestamp;
    this.id = UUID.nameUUIDFromBytes( (userId+tag).getBytes() );
  }
  public UUID getId() {
    return id; 
  }
  public String getUserId() {
    return userId;
  }
  public String getTag() {
    return tag;
  }
  public long getEventDateTime(){
    return eventDateTime;
  }
  public long getTimestamp() {
    return timestamp;
  }
}
