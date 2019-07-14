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

@WebFilter(filterName="CredentialFilter")
public class CredentialFilter extends HttpFilter {
    @Override
    protected void doFilter(
         HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            getServletContext().log("CredentialFilter - request.url = " + request.getRequestURL().toString() + request.getQueryString());
            if( request.getSession().getAttribute("authorized") == null ||
                !((boolean) request.getSession().getAttribute("authorized")) ){
              if( isAJAXRequest(request) || isFetch(request) ){
                String to = "/dashboard/credential";
                String message = "Unauthorized request to calendar";
                response.setContentType("text/json; charset=UTF-8");
                response.getOutputStream().println("{\"to\":\""+to+"\",\"error\":\""+message+"\"}");
                return;
              }
              response.sendRedirect("/dashboard/credential");
              return;
            }
            chain.doFilter(request, response);
        }
    public void destroy() {}

    private boolean isAJAXRequest(HttpServletRequest request) {
      String facesRequest = request.getHeader("Faces-Request");
      if (facesRequest != null && facesRequest.equals("partial/ajax")) {
        return true;
      }
      return false;
    }

    private boolean isFetch(HttpServletRequest request){
      return "true".equals( request.getHeader("isFetch") ) ;
    }
}
