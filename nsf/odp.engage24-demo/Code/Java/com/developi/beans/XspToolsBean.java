package com.developi.beans;

import javax.faces.context.FacesContext;

import com.ibm.xsp.designer.context.XSPContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@RequestScoped
@Named("tools")
public class XspToolsBean {

	public void openPage(String pageName) {
		
		XSPContext.getXSPContext(FacesContext.getCurrentInstance()).redirectToPage(pageName, true);
	}
	
}
