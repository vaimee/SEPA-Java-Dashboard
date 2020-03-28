package it.unibo.arces.wot.sepa.tools.covid19;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
			
{
	"stato": "ITA",							gn:contryCode 'ITA'
	
	"codice_regione": 13,					gn:parentFeature <http://covid19/Region/Italy13>
	"denominazione_regione": "Abruzzo",
	
	"codice_provincia": 69,					<http://covid19/Province/Italy69>
	
	"denominazione_provincia": "Chieti",
	"sigla_provincia": "CH",				gn:name "Chieti (CH)"
	
	"lat": 42.35103167,						gn:lat 42.35103167
	"long": 14.16754574,					gn:long 14.16754574
	
	"data": "2020-03-20 17:00:00",			sosa:resultTime '2020-03-20T17:00:00Z'
	"totale_casi": 80						covid19:TotalCases 80
}

Region
------
rdf:type gn:Feature
gn:featureClass 'ADM1'

{
	
	"stato": "ITA",							gn:contryCode 'ITA'
	"codice_regione": 5,					<http://covid19/Region/Italy5>
	"denominazione_regione": "Veneto",		gn:name 'Veneto'
	
	"lat": 45.43490485,						gn:lat 45.43490485
	"long": 12.33845213,					gn:long 12.33845213
	
	"data": "2020-03-20 17:00:00",			sosa:resultTime '2020-03-20T17:00:00Z'
	"ricoverati_con_sintomi": 843,			covid19:HospitalisedWithSymptoms 843
	"terapia_intensiva": 236,				covid19:IntensiveCare 236
	"totale_ospedalizzati": 1079,			covid19:TotalHospitalised 1079
	"isolamento_domiciliare": 2598,			covid19:HomeConfinement 2598	
	"totale_attualmente_positivi": 3677,	covid19:TotalPositiveCases 3677
	
	"nuovi_attualmente_positivi": 508,		covid19:DailyPositiveCases 508
	"dimessi_guariti": 223,					covid19:Recovered 223
	"deceduti": 131,						covid19:Death 131
	
	"totale_casi": 4031,					covid19:TotalCases 4031
	"tamponi": 49288						covid19:TestPerformed 49288
}

codiv19 ==> http://covid19#

ObservableProperties URI
	covid19:HospitalisedWithSymptoms
	covid19:IntensiveCare 
	covid19:TotalHospitalised 
	covid19:HomeConfinement 	
	covid19:TotalPositiveCases 	
	covid19:DailyPositiveCases
	covid19:Recovered
	covid19:Death 
	covid19:TotalCases
	covid19:TestPerformed

 * 
 * */
public class AddObservations extends Producer {
	private HashMap<String,String> properties = new HashMap<String, String>();
	
	public AddObservations(String hostFile,ClientSecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		super(new JSAP("observations.jsap"), "ADD_OBSERVATION", sm);
		
		if (hostFile != null) appProfile.read(hostFile, true);
		
		properties.put("ricoverati_con_sintomi", "covid19:HospitalisedWithSymptoms");
		properties.put("terapia_intensiva", "covid19:IntensiveCare");
		properties.put("totale_ospedalizzati", "covid19:TotalHospitalised");
		properties.put("isolamento_domiciliare", "covid19:HomeConfinement");
		properties.put("totale_attualmente_positivi", "covid19:TotalPositiveCases");
		properties.put("nuovi_attualmente_positivi", "covid19:DailyPositiveCases");
		properties.put("dimessi_guariti", "covid19:Recovered");
		properties.put("deceduti", "covid19:Death");
		properties.put("totale_casi", "covid19:TotalCases");
		properties.put("tamponi", "covid19:TestPerformed");
	}
	
	public AddObservations(String hostFile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile,null);
	}
	
	public AddObservations() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(null,null);
	}

	public void addRegionObservations(String graph) throws FileNotFoundException, SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		JsonArray array = loadJsonArray("dpc-covid19-ita-regioni-latest.json");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("ricoverati_con_sintomi");
		list.add("terapia_intensiva");
		list.add("totale_ospedalizzati");
		list.add("isolamento_domiciliare");
		list.add("totale_attualmente_positivi");
		list.add("nuovi_attualmente_positivi");
		list.add("dimessi_guariti");
		list.add("deceduti");
		list.add("totale_casi");
		list.add("tamponi");
		
		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		
		for (JsonElement prov : array) {
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Region/" +prov.getAsJsonObject().get("denominazione_regione").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("timestamp", new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z")));
			
			for (String pro : list) {
				setUpdateBindingValue("property", new RDFTermURI(properties.get(pro)));			
				setUpdateBindingValue("value", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get(pro).getAsInt())));	
			
				update();
			}	
		}
	}
	
	public void addProvinceObservations(String graph) throws FileNotFoundException, SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		JsonArray array = loadJsonArray("dpc-covid19-ita-province-latest.json");
		
		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		
		for (JsonElement prov : array) {
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Province/" + prov.getAsJsonObject().get("denominazione_provincia").getAsString().replace(" ", "_").replace("'", "_")));			
			setUpdateBindingValue("timestamp", new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z")));
			
			setUpdateBindingValue("property", new RDFTermURI(properties.get("totale_casi")));
			setUpdateBindingValue("value", new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("totale_casi").getAsInt())));
			
			update();
		}
	}
	
	public JsonArray loadJsonArray(String jsapFile) throws FileNotFoundException {
		FileReader in = new FileReader(jsapFile);
		
		return new JsonParser().parse(in).getAsJsonArray();
	}
}
