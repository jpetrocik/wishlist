package org.psoft.wishlist.actions;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;
import org.psoft.wishlist.dao.NewsDao;
import org.psoft.wishlist.forms.NewsForm;
import org.psoft.wishlist.torque.News;

public class NewsAction extends MappingDispatchAction {

	private NewsDao newsDao = new NewsDao();
	
	public ActionForward postAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		NewsForm newsForm = (NewsForm) form;
		
		News news = new News();
		news.setIntials(newsForm.getIntials());
		news.setInfo(newsForm.getInfo());
		news.setPostedDate(new Date());
		news.setPostedBy(request.getUserPrincipal().getName());
		
		newsDao.save(news);
		
		newsForm.setIntials(null);
		newsForm.setInfo(null);
		
		return newsAction(mapping, form, request, response);
	}

	public ActionForward newsAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		NewsForm newsForm = (NewsForm) form;
		
		List news = newsDao.fetchNews();

		request.getSession().setAttribute("news", news);
		
		return mapping.findForward("news");
	}
}
