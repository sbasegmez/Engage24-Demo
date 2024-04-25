package com.developi.utils;

import org.apache.commons.lang3.StringUtils;
import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * This is to provide inject-able components.
 * 
 * @author sbasegmez
 *
 */
@RequestScoped
public class CdiProvider {
 
    @Produces
    @Named("odaSession")
    public Session getSession() {
        return Factory.getSession(SessionType.CURRENT);
    }
    
    @Produces
    @Named("odaAppDb")
    public Database getApplicationDatabase() {
    	return getSession().getCurrentDatabase();
    }
	
    @Produces
    @Named("openAiApiKey")
    public String getOpenAiApiKey() {
    	//return "demo";
    	String apiKey = ExtLibUtil.getXspProperty("com.developi.llm.OPENAI_API_KEY");
    	
    	if(StringUtils.isNotEmpty(apiKey)) {
    		return apiKey;
    	} 
    	
    	System.out.println("Using Demo api key!");
 
    	return "demo";
    }
    
}
