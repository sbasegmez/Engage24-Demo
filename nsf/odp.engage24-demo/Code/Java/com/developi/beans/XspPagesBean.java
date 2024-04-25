package com.developi.beans;

import javax.faces.context.FacesContext;

import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.designer.context.XSPUrl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@RequestScoped
@Named("pages")
public class XspPagesBean {

	/**
	 * Called from the layout to run before page load
	 */
	public void beforePageLoad() {
		
	}
	
	/**
	 * Called from the layout to run after page load
	 */
	public void afterPageLoad() {
		
	}
	
	public boolean isActive(String page) {
		XSPUrl currentUrl = XSPContext.getXSPContext(FacesContext.getCurrentInstance()).getUrl();
		
		return currentUrl.getPath().contains(page);
	}
	
}
