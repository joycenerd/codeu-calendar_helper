/*
 This code slice the GET query string and store them as dicstionary for further use - by method, get(String)
 */
package com.google.codeu.utils;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class QuerySlices{ 
  private Map<String, String> slices = new HashMap<>();
  public QuerySlices(String query){
    for (String param : query.split("&")) {
        String[] entry = param.split("=");
        if (entry.length > 1) {
            slices.put(entry[0], entry[1]);
        }else{
            slices.put(entry[0], "");
        }
    }
  }

  public String get(String s){
    return slices.get(s);
  }

}
