package com.google.codeu.filters;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.FileNotFoundException;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

@WebFilter(filterName="LoginFilter")  //url-patterns must be declared in web.xml. For some reason the order will be a mess if decalring here.
public class LoginFilter extends HttpFilter {
    @Override
    protected void doFilter(
         HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            UserService userService = UserServiceFactory.getUserService();

            getServletContext().log("LoginFilter - request.url = " + request.getRequestURL().toString() + request.getQueryString());
            if(!userService.isUserLoggedIn()){
              redirect(request, response, "/login", "Unlogged-in user request");
              return;
            }
            String userId = userService.getCurrentUser().getUserId();

            if( userId == null ){
              redirect(request, response, "/login", "Unlogged-in user request");
              return;
            }

            chain.doFilter(request, response);
        }
    public void destroy() {}

    private void redirect(HttpServletRequest request, HttpServletResponse response, String to, String message) throws IOException{
      if( isAJAXRequest(request) || isFetch(request) ){
        response.setContentType("text/json; charset=UTF-8");
        response.getOutputStream().println("{\"to\":\""+to+"\",\"error\":\""+message+"\"}");
        return;
      }
      response.sendRedirect(to);
    }

    private boolean isAJAXRequest(HttpServletRequest request) {
      String facesRequest = request.getHeader("Faces-Request");
      if ((facesRequest != null && facesRequest.equals("partial/ajax")) ||
          "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
        return true;
      }
      return false;
    }

    private boolean isFetch(HttpServletRequest request){
      return "true".equals( request.getHeader("isFetch") ) ;
    }
}
