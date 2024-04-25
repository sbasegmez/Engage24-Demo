package com.developi.beans;

import java.io.Serializable;
import java.util.Set;

import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.utils.Names;

import com.developi.utils.NotesName;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("user")
public class UserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	@Named("odaSession")
	private Session session;
	
	@Inject
	@Named("odaAppDb")
	private Database appDb;
	
	
	public NotesName getNotesName() {
		return new NotesName(session.getEffectiveUserName());
	}
	
	public boolean hasRole(String role) {
		Set<String> roles = Names.getRoles(session, appDb);
			
		return null == roles ? false: roles.contains(role);
	}
	
	public String getCommonName() {
		return getNotesName().getCommonName();
	}
	
	public boolean isAdmin() {
		return hasRole("[Admin]");
	}


	
}
