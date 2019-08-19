package myapp;

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
@WebServlet(name = "create", urlPatterns = {"/create"})
// [END annotations]
public class CreatePortfolioServlet extends HttpServlet {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  // [START setup]
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    req.setAttribute("action", "Add");          // Part of the Header in form.jsp
    req.setAttribute("destination", "create");  // The urlPattern to invoke (this Servlet)
   req.getRequestDispatcher("/addEquity.jsp").forward(req, resp);
  }
  // [END setup]

  // [START formpost]
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
   Entity equity = new Entity("Equity"); // create a new entity

    equity.setProperty("Ticker",req.getParameter("ticker"));
    equity.setProperty("Quantity",req.getParameter("quantity"));
    equity.setProperty("OwnerId","Demo");

  try {
    int PAGE_SIZE = 10;
    datastore.put(equity); // store the entity
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
    w.println("<a href='index.html'><button>Go Back</button></a>");
    w.println("<title>Portfolio</title>");
    w.println("<ul>");
    w.println("<h1>Equities</h1>");
    for (Entity entity : results) {
      w.println("<p> Ticker:" + entity.getProperty("Ticker") + "</p>");
      w.println("<p> Quantity:" + entity.getProperty("quantity") + "</p>");
    }
    w.println("</ul>");
    req.getRequestDispatcher("/addEquity.jsp").forward(req, resp);
  } catch (DatastoreFailureException e) {
    throw new ServletException("Datastore error", e);
  }
}


    }
