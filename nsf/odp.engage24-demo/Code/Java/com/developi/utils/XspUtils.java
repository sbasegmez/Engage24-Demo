package com.developi.utils;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.designer.context.XSPUrl;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * 
 * @author sbasegmez
 *
 */
public class XspUtils {

	/**
	 * Look for the base URL of the server root. It strips the database url out of the current url.
	 * 
	 * @return Server url, like "http(s)://www.domain.com"
	 */
	public static String getServerURL() {
		XSPContext xspContext = XSPContext.getXSPContext(FacesContext.getCurrentInstance());
		XSPUrl url = xspContext.getUrl();

		String base = StringUtils.substringBefore(url.getAddress(), getDatabaseURL());

		return base;
	}

	/**
	 * Get the application (NSF) uri
	 * 
	 * @return in format of "/directory/dbname.nsf"
	 */
	public static String getDatabaseURL() {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getExternalContext().getRequestContextPath();
	}

	/**
	 * 
	 * Thanks to Tim Tripcony and Nathan Freeman for help with this code.
	 * 
	 * The code getXspProperty() looks to the NSF's xsp.properties, then the server's xsp.properties, then the server's
	 * notes.ini file. If the property isn't available or is blank in all three, the default value passed in initially is used
	 * instead.
	 * 
	 * Source: https://openntf.org/XSnippets.nsf/snippet.xsp?id=retrieve-property-from-xsp.properties-in-nsf-server-or-notes.ini
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static String getXspProperty(String propertyName, String defaultValue) {
		String retVal = ApplicationEx.getInstance().getApplicationProperty(propertyName, getIniVar(propertyName, defaultValue));
		return retVal;
	}

	/**
	 * 
	 * Get value from the notes.ini
	 * 
	 * Source: https://openntf.org/XSnippets.nsf/snippet.xsp?id=retrieve-property-from-xsp.properties-in-nsf-server-or-notes.ini
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static String getIniVar(String propertyName, String defaultValue) {
		try {
			Session session = ExtLibUtil.getCurrentSession();
			String newVal = session.getEnvironmentString(propertyName, true);
			if (StringUtils.isEmpty(newVal)) {
				return newVal;
			} else {
				return defaultValue;
			}
		} catch (NotesException e) {
			return defaultValue;
		}
	}

}
