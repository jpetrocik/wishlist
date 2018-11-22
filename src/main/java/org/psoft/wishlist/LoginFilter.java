package org.psoft.wishlist;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.psoft.wishlist.dao.AccountDao;
import org.psoft.wishlist.dao.data.Account;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginFilter implements Filter {

	@Autowired
	AccountDao userDao;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain filterChain) throws IOException, ServletException {
			HttpServletRequest request = (HttpServletRequest) _request;
			HttpServletResponse response = (HttpServletResponse) _response;
			HttpSession session = request.getSession();

		String path = request.getServletPath();

		//check is authentication is being requested
		String authToken = request.getParameter("authorizationToken");
		if (authToken != null) {
			Account wishlistUser = userDao.validateAuthtoken(authToken);
			if (wishlistUser == null){
				response.sendError(403, "Unauthorized");
				return;
			}

			//return auth cookie for later remeber me
			Cookie authCookie = new Cookie("user-token", authToken);
			authCookie.setPath("/");
			authCookie.setMaxAge(180 * 24 * 60 * 60);
			response.addCookie(authCookie);

			session.setAttribute("user", wishlistUser);
		}

		//check is cookie or header available and user in not authenticated
		if ( session.getAttribute("user") == null ) {

			//check cookie auth then check header
			String authorizationToken = checkAuthCookie(request.getCookies());
			if (StringUtils.isNotBlank(authorizationToken)){
				Account wishlistUser = userDao.validateAuthtoken(authorizationToken);
				if (wishlistUser != null){
					session.setAttribute("user", wishlistUser);
				}
			}
		}

		//unsecured assets
		if ( (path != null) && (path.startsWith("/api/invitation") ||
				path.startsWith("/api/start") ||
				path.startsWith("/api/register") ||
				path.startsWith("/api/mfa") ||
				path.matches("/api/group/(.*)/invitation"))) {

			filterChain.doFilter(_request, _response);
			return;

			//signouts
		} else if (StringUtils.startsWith(path, "/signout")) {
			session.removeAttribute("user");
			response.setStatus(200);
			return;
		}

		//    	//user invitation login, check for user/token in params
		//	    //always login if provided
		//		String email = request.getParameter("email");
		//		String token = request.getParameter("token");
		//		if (email != null && token != null) {
		//			String authorizationToken = userDao.validateUser(email, token);
		//		    if (authorizationToken == null){
		//		    	response.sendError(403, "Unauthorized");
		//		        return;
		//		    }
		//
		//		    //return auth cookie for later remeber me
		//		    Cookie authCookie = new Cookie("user-token", authorizationToken);
		//		    authCookie.setPath("/");
		//		    authCookie.setMaxAge(180 * 24 * 60 * 60);
		//		    response.addCookie(authCookie);
		//
		//			Account wishlistUser = userDao.validateAuthtoken(authorizationToken);
		//	    	session.setAttribute("user", wishlistUser);
		//		}
		//
		//		if (StringUtils.isBlank(authorizationToken)) {
		//			authorizationToken = request.getHeader("auth-token");
		//		}


		if ( session.getAttribute("user") == null ) {
		    	response.sendError(403, "Unauthorized");
		        return;
		}

		filterChain.doFilter(_request, _response);
	}

	private String checkAuthCookie(Cookie[] cookies) {
		if (cookies == null)
			return null;

		Optional<Cookie> userTokenCookie = Arrays.stream(cookies).filter(c -> c.getName().equals("user-token")).findFirst();
		if (!userTokenCookie.isPresent()) {
			return null;
		}

		return userTokenCookie.get().getValue();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
