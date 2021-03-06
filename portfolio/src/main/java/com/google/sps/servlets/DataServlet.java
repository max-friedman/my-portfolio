// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.util.ArrayList;
import com.google.gson.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments;
    try {
      numComments = Integer.parseInt(request.getParameter("number-comments"));
    } catch(Exception e) {
      numComments = 5;
    }
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (comments.size() >= numComments) {
        break;
      }

      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String message = (String) entity.getProperty("message");
      String timestamp = formatTimestamp((long) entity.getProperty("timestamp"));
      String sentiment = (String) entity.getProperty("sentiment");

      comments.add(new Comment(id, name, message, timestamp, sentiment));
    }
    
    response.setContentType("text/html;");
    String json = new Gson().toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long timestamp = System.currentTimeMillis();
    String name = request.getParameter("name");
    String message = request.getParameter("message");
    
    Document doc =
        Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    String sentimentString = String.valueOf(score);
    languageService.close();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("sentiment", sentimentString);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.setContentType("text/html;");
    
    response.sendRedirect("/index.html");
  }

  private String formatTimestamp(long millis) {
    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, YYYY 'at' hh:mm aaa");
    return sdf.format(millis);
  }
}

class Comment {
  String name, message, timestamp;
  String sentiment;
  long id;

  public Comment(long id, String name, String message, String timestamp, String sentiment) {
    this.message = message;
    this.timestamp = timestamp;
    this.id = id;
    this.name = name;
    this.sentiment = sentiment;
  }
}
