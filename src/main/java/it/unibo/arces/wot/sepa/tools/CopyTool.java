package it.unibo.arces.wot.sepa.tools;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermBNode;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class CopyTool {
	private static final Logger logger = LogManager.getLogger();
	
	public static void copyObservationHistory(String observation,String from,String to) throws SEPAPropertiesException, SEPASecurityException, SEPAProtocolException, SEPABindingsException, IOException {
		JSAP jsap = new JSAP("utility.jsap");
		ArrayList<Bindings> failedUpd = new ArrayList<Bindings>();
		
		GenericClient client = new GenericClient(jsap,null,null);
		
		// --- QUERY SOURCE
		Bindings fb = new Bindings();
		fb.addBinding("observation", new RDFTermURI(observation));
		fb.addBinding("from", new RDFTermLiteral(from,"xsd:dateTime"));
		fb.addBinding("to", new RDFTermLiteral(to,"xsd:dateTime"));
		
		Response ret = client.query("FROM_HISTORY", fb, 10000);
		// ---
		
		if (ret.isQueryResponse()) {
			QueryResponse res = (QueryResponse) ret;
			
			logger.info("Results: "+res.getBindingsResults().getBindings().size());
			int i=1;
			int err = 0;
			for (Bindings obs : res.getBindingsResults().getBindings()) {
				
				// --- UPDATE DESTINATION
				obs.addBinding("observation", new RDFTermURI(observation));
				
				// Virtuoso blank node representation
				if (obs.getValue("result").startsWith("nodeID://")) {
					String bNodeString = obs.getValue("result").replace("nodeID://", "_:");
					obs.setBindingValue("result", new RDFTermBNode(bNodeString));
				}
				
				Response upd = client.update("TO_HISTORY", obs, 500);
				// ---
				
				if (upd.isError()) {
					err++;
					logger.error("Update FAILED "+i+"/"+res.getBindingsResults().getBindings().size());
					failedUpd.add(obs);
				}
				else logger.info("Update OK "+i+"/"+res.getBindingsResults().getBindings().size());
				i++;
			}
			
			logger.info("Update FAILED "+err+"/"+res.getBindingsResults().getBindings().size());
			for (Bindings b : failedUpd) {
				logger.info(b);
			}
		}
		
		client.close();	
	}
	
	public static void copyForecast(String place) throws SEPAPropertiesException, SEPASecurityException, SEPAProtocolException, SEPABindingsException, IOException {
		JSAP jsap = new JSAP("copy.jsap");
		ArrayList<Bindings> failedUpd = new ArrayList<Bindings>();
		
		GenericClient client = new GenericClient(jsap,null,null);
		
		// --- QUERY SOURCE
		Bindings fb = new Bindings();
		fb.addBinding("place", new RDFTermURI(place));
		
		Response ret = client.query("FROM_FORECAST", fb, 10000);
		// ---
		
		if (ret.isQueryResponse()) {
			QueryResponse res = (QueryResponse) ret;
			
			logger.info("Results: "+res.getBindingsResults().getBindings().size());
			int i=1;
			int err = 0;
			for (Bindings obs : res.getBindingsResults().getBindings()) {
				
				// --- UPDATE DESTINATION
				obs.addBinding("place", new RDFTermURI(place));
				
				// Virtuoso blank node representation
				if (obs.getValue("result").startsWith("nodeID://")) {
					String bNodeString = obs.getValue("result").replace("nodeID://", "_:");
					obs.setBindingValue("result", new RDFTermBNode(bNodeString));
				}
				if (obs.getValue("forecast").startsWith("nodeID://")) {
					String bNodeString = obs.getValue("forecast").replace("nodeID://", "_:");
					obs.setBindingValue("forecast", new RDFTermBNode(bNodeString));
				}
				
				Response upd = client.update("TO_FORECAST", obs, 500);
				// ---
				
				if (upd.isError()) {
					err++;
					logger.error("Update FAILED "+i+"/"+res.getBindingsResults().getBindings().size());
					failedUpd.add(obs);
				}
				else logger.info("Update OK "+i+"/"+res.getBindingsResults().getBindings().size());
				i++;
			}
			
			logger.info("Update FAILED "+err+"/"+res.getBindingsResults().getBindings().size());
			for (Bindings b : failedUpd) {
				logger.info(b);
			}
		}
		
		client.close();	
	}
	
	public static void main(String[] args) throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException, SEPABindingsException, IOException {
//		String observation = "arces-monitor:Current_Weather_Bertacchini_Precipitation";
//		String from = "2019-01-01T00:00:00Z";
//		String to ="2019-12-31T23:59:59Z";
//		
//		copyObservationHistory(observation,from,to);
		
		String place = "swamp:Bertacchini";
		
		copyForecast(place);		
	}
}
