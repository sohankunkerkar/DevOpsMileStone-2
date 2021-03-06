package edu.ncsu.csc.itrust.server;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.logger.TransactionLogger;
import edu.ncsu.csc.itrust.model.old.dao.DAOFactory;
import edu.ncsu.csc.itrust.model.old.enums.TransactionType;

/**
 * A listener which will time the user out after a pre-specified time limit. 
 */
public class SessionTimeoutListener implements HttpSessionListener {
	private DAOFactory factory;

	/**
	 * The default constructor.
	 */
	public SessionTimeoutListener() {
		this.factory = DAOFactory.getProductionInstance();
	}

	/**
	 * The specialized constructor, which takes a particular DAOFactory to be used when checking for the pre-specified
	 * timeout limit.
	 * @param factory The DAOFactory to be used.
	 */
	public SessionTimeoutListener(DAOFactory factory) {
		this.factory = factory;
	}

	/**
	 * Called when the HttpSession is created, this method pulls the pre-specified limit from the
	 * database and sets it as a property of the HttpSession.
	 * @param arg0 The HttpSessionEven which just occurred.
	 */
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		HttpSession session = arg0.getSession();
		int mins = 20;
		try {
			mins = factory.getAccessDAO().getSessionTimeoutMins();
		} catch (DBException e) {
			System.err.println("Unable to set session timeout, defaulting to 20 minutes");
			
		}
		if (mins < 1)
			mins = 1;
		session.setMaxInactiveInterval(mins * 60);
	}

	/**
	 * Must be declared for compliance with the interface. Not implemented.
	 * @param arg0 arg0
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		// nothing to do here
		HttpSession session = arg0.getSession();
		Long mid = (Long) session.getAttribute("loggedInMID");
		if (mid == null) {
			TransactionLogger.getInstance().logTransaction(TransactionType.LOGOUT_INACTIVE, mid, null, "");
		}
	}
}
