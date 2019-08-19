package myapp;

import java.net.*;
import java.io.*;

import java.util.List;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;


// [START example]
@SuppressWarnings("serial")
// [START annotations]
@MultipartConfig
@WebServlet(name = "delete", urlPatterns = {"/delete"})
// [END annotations]
public class DeleteEquityServlet extends HttpServlet {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    req.setAttribute("action", "Add");          // Part of the Header in form.jsp
    req.setAttribute("destination", "delete");  // The urlPattern to invoke (this Servlet)
    int PAGE_SIZE = 10;
    Query q = new Query("Equity").addSort("Ticker", SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(q);
    QueryResultList<Entity> results;
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(PAGE_SIZE);
    try {
      results = pq.asQueryResultList(fetchOptions);
    } catch (IllegalArgumentException e) {
      resp.sendRedirect("index.html");
      return;
    }
    resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
    PrintWriter w = resp.getWriter();
    w.println("<!DOCTYPE html>");
    w.println("<meta charset=\"utf-8\">");
    w.println("<title>Portfolio</title>");
    w.println("<ul>");
    w.println("<h1>Equities</h1>");
    for (Entity entity : results) {
     StockInfo stock = new StockInfo((String)entity.getProperty("Ticker"));
     w.println("<p> Ticker:" + entity.getProperty("Ticker") + "    Quantity:" + entity.getProperty("Quantity") + "    Price:"+ stock.priceOf()+ "</p>");
    }
    w.println("</ul>");
   req.getRequestDispatcher("/deleteEquity.jsp").forward(req, resp);
  }
  // [END setup]

  // [START formpost]
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    PrintWriter out= resp.getWriter(); 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);
    Query q = new Query("Equity").addSort("Ticker", SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(q);

    QueryResultList<Entity> results;
    try {
      results = pq.asQueryResultList(fetchOptions);
    } catch (IllegalArgumentException e) {
      // IllegalArgumentException happens when an invalid cursor is used.
      // A user could have manually entered a bad cursor in the URL or there
      // may have been an internal implementation detail change in App Engine.
      // Redirect to the page without the cursor parameter to show something
      // rather than an error.
      resp.sendRedirect("index.html");
      return;
    }
    for (Entity entity : results) {
    if(((String)entity.getProperty("Ticker")).equals(((String)req.getParameter("ticker")))){
        datastore.delete(entity.getKey());
        resp.sendRedirect("/delete");
        return;
    }
    }
}


    }
