package it.unibo.arces.wot.sepa.tools.covid19;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

/*

Region
------
rdf:type gn:Feature
gn:featureClass 'ADM1'
gn:contryCode 'ITA'

{
							
	"codice_regione": 5,					<http://covid19/Italy/Region5>
	"denominazione_regione": "Veneto",		gn:name 'Veneto'
	
	"lat": 45.43490485,						gn:lat 45.43490485
	"long": 12.33845213,					gn:long 12.33845213
}

 * 
 * */
public class AddRegions extends Producer {
	
	public AddRegions(String hostFile,ClientSecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		super(new JSAP("places.jsap"), "ADD_REGION", sm);
		
		if (hostFile != null) appProfile.read(hostFile, true);
	}
	
	public AddRegions(String hostFile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile,null);
	}
	
	public AddRegions() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(null,null);
	}
	
	public void addAll() throws SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		JsonArray provicesArray = appProfile.getExtendedData().get("regions").getAsJsonArray();
		
		for (JsonElement prov : provicesArray) {
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Region/" +prov.getAsJsonObject().get("denominazione_regione").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("name", new RDFTermLiteral(prov.getAsJsonObject().get("denominazione_regione").getAsString()));
			setUpdateBindingValue("code", new RDFTermLiteral("ITA"));
			setUpdateBindingValue("lat", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("lat").getAsFloat()),"xsd:decimal"));
			setUpdateBindingValue("lon", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("long").getAsFloat()),"xsd:decimal"));
			
			update();
		}
	}

	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException, SEPABindingsException {
		AddRegions agent = new AddRegions("host-mml.jsap");
		
		agent.addAll();
		
		agent.close();
	}
}

