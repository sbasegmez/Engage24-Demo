package com.developi.utils.collections;

import java.util.Map;

import com.developi.beans.UserBean;

import jakarta.inject.Inject;

/**
 * This is an expiring map to keep config values for a configurable time.
 * 
 * @author sbasegmez
 *
 */
public abstract class AbstractTimedMap extends AbstractSmartMap {

	private static final long serialVersionUID = 1L;
	
	private boolean loaded;
	private long lastLoadedMills;
	private Throwable lastError;

	@Inject
	private UserBean user;
	
	public AbstractTimedMap() {
		super();

		this.loaded = false;
		this.lastLoadedMills = 0;
	}

	@Override
	protected Map<String, Object> getValues() {
		if (!this.loaded) {
			System.out.println("Loading configuration values...");
			_updateValues();
		} else if (needsReloading()) {
			System.out.println("Refreshing configuration values...");
			_updateValues();
		}

		return super.getValues();
	}

	private boolean needsReloading() {
		if (this.lastLoadedMills > 0) {
			long elapsedMills = System.currentTimeMillis() - this.lastLoadedMills;

			return elapsedMills > (getRefreshIntervalMins() * 60 * 1000);
		}

		return false;

	}

	// Internal delegate function for values update.
	private void _updateValues() {
		try {
			updateValues();

			this.loaded = true;
			this.lastLoadedMills = System.currentTimeMillis();

		} catch (Throwable t) {
			t.printStackTrace();
			this.setLastError(t);
		}
	}

	protected void setLastError(Throwable lastError) {
		this.lastError = lastError;
	}

	public void refresh() {
		if(user.isAdmin()) {
			_updateValues();
		}
	}
	
	public Throwable getLastError() {
		return lastError;
	}

	public String getLastErrorMessage() {
		return (null == getLastError()) ? null : getLastError().getMessage();
	}

	// Abstract functions

	public abstract long getRefreshIntervalMins();
	public abstract void updateValues();

}
