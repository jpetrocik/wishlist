package org.psoft.wishlist.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.psoft.wishlist.torque.News;
import org.psoft.wishlist.torque.NewsPeer;

public class NewsDao {
	private static Log log = LogFactory.getLog(NewsDao.class);
	
	public void save(News news){
		try {
			news.save();
		} catch (Exception e) {
			String msg = "Unable to save news.";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}
	
	public List fetchNews(){
		List news;

		try {
			Criteria crit = new Criteria();
			crit.addDescendingOrderByColumn("NEWS.POSTED_DATE");

			news = NewsPeer.doSelect(crit);
		} catch (TorqueException te) {
			String msg = "Unable to fetch news";
			log.error(msg, te);
			throw new RuntimeException(msg, te);
		}

		return news;
		
	}
}
