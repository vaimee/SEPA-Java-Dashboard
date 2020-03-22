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
Province
--------
rdf:type gn:Feature
gn:featureClass 'ADM2'
gn:contryCode 'ITA'

{				
	"codice_regione": 13,					gn:parentFeature <http://covid19/Italy/Region13>
	
	"codice_provincia": 69,					<http://covid19/Province/Italy69>
	
	"denominazione_provincia": "Chieti",
	"sigla_provincia": "CH",				gn:name "Chieti (CH)"
	
	"lat": 42.35103167,						gn:lat 42.35103167
	"long": 14.16754574,					gn:long 14.16754574
}
 * 
 * */
public class AddProvinces extends Producer {
		
	public AddProvinces(String hostFile,ClientSecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		super(new JSAP("places.jsap"), "ADD_PROVINCE", sm);
		
		if (hostFile != null) appProfile.read(hostFile, true);
	}
	
	public AddProvinces(String hostFile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile,null);
	}
	
	public AddProvinces() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(null,null);
	}
	
	public void addAll() throws SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		JsonArray provicesArray = appProfile.getExtendedData().get("provinces").getAsJsonArray();
		
		for (JsonElement prov : provicesArray) {
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Province/" + prov.getAsJsonObject().get("denominazione_provincia").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("parent", new RDFTermURI("http://covid19/Italy/Region/" +prov.getAsJsonObject().get("denominazione_regione").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("name", new RDFTermLiteral(prov.getAsJsonObject().get("denominazione_provincia").getAsString() + " ("+prov.getAsJsonObject().get("sigla_provincia").getAsString()+")"));
			setUpdateBindingValue("code", new RDFTermLiteral("ITA"));
			setUpdateBindingValue("lat", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("lat").getAsFloat()),"xsd:decimal"));
			setUpdateBindingValue("lon", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("long").getAsFloat()),"xsd:decimal"));
			
			update();
		}
	}

	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException, SEPABindingsException {
		AddProvinces agent = new AddProvinces("host-mml.jsap");
		
		agent.addAll();
		
		agent.close();
	}
}
