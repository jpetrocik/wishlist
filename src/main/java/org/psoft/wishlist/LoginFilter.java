package org.psoft.wishlist;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

public class LoginFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain filterChain) throws IOException, ServletException {
		 HttpServletRequest request = (HttpServletRequest) _request;
	     HttpServletResponse response = (HttpServletResponse) _response;
	     HttpSession session = request.getSession();
	        
	     String path = request.getServletPath();

	    //unsecured assets
	    if (StringUtils.startsWith(path, "/login.html") || StringUtils.startsWith(path, "/images") || StringUtils.endsWith(path, "css")) {
		    filterChain.doFilter(_request, _response);

		//signout
	    } else if (StringUtils.startsWith(path, "/signout")) {
			session.removeAttribute("user");
		    response.sendRedirect("/login.html");
		    return;

		//signin
	    } else if (StringUtils.startsWith(path, "/signin")) {

			//check password
			String password = request.getParameter("password");
			if (!"2018".equals(StringUtils.trim(password))) {
			    response.sendRedirect("/login.html");
			    return;
			}

			//set user or redirect to login
			String user = request.getParameter("userId");
			if (StringUtils.isNotBlank(user)){
				session.setAttribute("user", StringUtils.upperCase(user));
				response.sendRedirect("/wishlist.html");
			} else {
				response.sendRedirect("/login.html");
				return;
			}

		//confirmed logged in and continue
		} else {

	    	String user = (String) session.getAttribute("user");
		    if (StringUtils.isBlank(user)){
		        response.sendRedirect("/login.html");
		        return;
			}
		     
		     filterChain.doFilter(_request, _response);
	     }
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
