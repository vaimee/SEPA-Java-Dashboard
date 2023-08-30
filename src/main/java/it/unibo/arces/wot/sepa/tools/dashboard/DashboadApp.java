package it.unibo.arces.wot.sepa.tools.dashboard;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class DashboadApp {
	private GenericClient sepaClient;
	
	private final String defaultGraph = "https://sepa.vaimee.com/default/graph";
	private final int timeout = 30000;
	private final int nretry = 1;

	public DashboadApp(JSAP appProfile, DashboardHandler handler) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		sepaClient = new GenericClient(appProfile, handler);
	}
	
	public Response query(String queryID, String sparql,Bindings forced,int timeout,int nretry) {
		try {
			return sepaClient.query(queryID, sparql, forced, timeout, nretry);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			return new ErrorResponse(500, e.getMessage(), e.getMessage());
		}
	}
	
	public Response update(String queryID, String sparql,Bindings forced,int timeout,int nretry) {
		try {
			return sepaClient.update(queryID, sparql, forced, timeout, nretry);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			return new ErrorResponse(500, e.getMessage(), e.getMessage());
		}
	}
	
	private Response query(String queryID, Bindings bindings) {
		try {
			if (bindings.getValue("graph")!= null) 
				if (bindings.getValue("graph").equals(defaultGraph)) return sepaClient.query(queryID+"_DEFAULT", null, bindings, timeout, nretry);
			return sepaClient.query(queryID, null, bindings, timeout, nretry);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			return new ErrorResponse(500, e.getMessage(), e.getMessage());
		}
	}

	private Response update(String updateID, Bindings bindings) {
		try {
			if (bindings.getValue("graph").equals(defaultGraph)) return sepaClient.update(updateID+"_DEFAULT", null, bindings, timeout, nretry);
			else return sepaClient.update(updateID, null, bindings, timeout, nretry);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			return new ErrorResponse(500, e.getMessage(), e.getMessage());
		}
	}
	
	//UPDATES

	public Response dropGraph(Bindings forced) {
		return update("___DASHBOARD_DROP_GRAPH", forced);
	}

	public Response updateLiteral(Bindings forcedBindings) {
		return update("___DASHBOARD_UPDATE_LITERAL", forcedBindings);
	}

	public Response updateLiteralBnode(Bindings forcedBindings) {
		return update("___DASHBOARD_UPDATE_LITERAL_BNODE", forcedBindings);
	}

	public Response updateUri(Bindings forcedBindings) {
		return update("___DASHBOARD_UPDATE_URI", forcedBindings);
	}

	public void subscribe(String queryID, String sparql, Bindings bindings, long timeout, long nretry)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException,
			InterruptedException {
		sepaClient.subscribe(queryID, sparql, bindings, timeout, nretry);
	}

	public void unsubscribe(String spuid, long timeout, long nretry) throws SEPAProtocolException,
			SEPASecurityException, SEPAPropertiesException, SEPABindingsException, InterruptedException {
		sepaClient.unsubscribe(spuid, timeout, nretry);
	}

	public Response graphs() {
		try {
			return sepaClient.query("___DASHBOARD_GRAPHS", null,null, timeout, nretry);
		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException | SEPABindingsException | IOException e) {
			return new ErrorResponse(500, e.getMessage(), e.getMessage());
		}
	}
	
	//QUERIES

	public Response topClasses(Bindings forced) {
		return query("___DASHBOARD_TOP_CLASSES", forced);
	}

	public Response uriGraph(Bindings forced) {
		return query("___DASHBOARD_URI_GRAPH", forced);
	}

	public Response bnodeGraph(Bindings forced) {
		return query("___DASHBOARD_BNODE_GRAPH", forced);
	}

	public Response individuals(Bindings forced) {	
		return query("___DASHBOARD_INDIVIDUALS", forced);
	}

	public Response subClasses(Bindings forced) {
		return query("___DASHBOARD_SUB_CLASSES", forced);
	}

	public String getHost() {
		return sepaClient.getApplicationProfile().getHost();
	}

}
