package it.unibo.arces.wot.sepa.tools;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

public class DropGraphs extends Producer {
		
	public DropGraphs(String hostFile,ClientSecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		super(new JSAP("utility.jsap"), "DROP_GRAPH", sm);
		
		if (hostFile != null) appProfile.read(hostFile, true);
	}
	
	public DropGraphs(String hostFile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile,null);
	}
	
	public DropGraphs() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(null,null);
	}
	
	public void drop(String uri) throws SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		setUpdateBindingValue("graph", new RDFTermURI(uri));
		update();
	}

	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException, SEPABindingsException {
		DropGraphs agentDropGraphs = new DropGraphs("host-mml.jsap");
		
		for (int i=5000 ;i < 6000; i++) agentDropGraphs.drop("http://wot.arces.unibo.it/thing/THING"+i);
		
		agentDropGraphs.close();
	}
}
