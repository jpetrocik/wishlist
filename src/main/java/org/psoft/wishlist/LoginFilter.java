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
import javax.xml.ws.http.HTTPBinding;

import org.apache.commons.lang3.StringUtils;
import org.psoft.wishlist.dao.UserDao;
import org.psoft.wishlist.dao.data.WishlistUser;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginFilter implements Filter {

	@Autowired
	UserDao userDao;

	public void destroy() {
	}

	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain filterChain) throws IOException, ServletException {
		 HttpServletRequest request = (HttpServletRequest) _request;
	     HttpServletResponse response = (HttpServletResponse) _response;
	     HttpSession session = request.getSession();
	        
	     String path = request.getServletPath();

	    //unsecured assets
	    if ( StringUtils.startsWith(path, "/api/invitation/") || 
	    		StringUtils.startsWith(path, "/api/start") || 
	    		StringUtils.startsWith(path, "/api/register")) {
		    filterChain.doFilter(_request, _response);
		    return;
		    
		//signouts
	    } else if (StringUtils.startsWith(path, "/signout")) {
			session.removeAttribute("user");
			response.setStatus(200);
		    return;
		    
		//signin
	    } else if (StringUtils.startsWith(path, "/api/signin") ) {
			String email = request.getParameter("email");
			String token = request.getParameter("token");

			try {
				String authorizationToken = userDao.validateUser(email, token);
				WishlistUser wishlistUser = userDao.validateAuthtoken(authorizationToken);
		    	session.setAttribute("user", wishlistUser);
				response.addHeader("auth-token", authorizationToken);
				response.setStatus(200);
				return;
			} catch (Exception e){
		    	response.sendError(403, "Unauthorized");
		    	return;
			}
			
	    }
	    
	    if ( session.getAttribute("user") == null ) {
			String authorizationToken = request.getHeader("auth-token");
		    if (authorizationToken == null){
		    	response.sendError(403, "Unauthorized");
		        return;
		    }
		    
			WishlistUser wishlistUser = userDao.validateAuthtoken(authorizationToken);
		    if (wishlistUser == null){
		    	response.sendError(403, "Unauthorized");
		        return;
		    }
	    	session.setAttribute("user", wishlistUser);
	    }
	    
		filterChain.doFilter(_request, _response);
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
