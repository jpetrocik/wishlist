package org.psoft.wishlist.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorActionForm;

public class NewsForm extends ValidatorActionForm {

	private String intials;
	
	private String info;
	
	public void reset(ActionMapping arg0, HttpServletRequest arg1) {
		intials = null;
		info = null;
		
		super.reset(arg0, arg1);
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getIntials() {
		return intials;
	}

	public void setIntials(String intials) {
		this.intials = intials;
	}

}
