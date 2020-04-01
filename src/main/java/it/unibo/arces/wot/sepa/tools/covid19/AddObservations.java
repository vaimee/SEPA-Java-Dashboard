package it.unibo.arces.wot.sepa.tools.covid19;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
		properties.put("dimessi_guariti", "covid19:Recovered");
		properties.put("deceduti", "covid19:Death");
		properties.put("totale_casi", "covid19:TotalCases");
		properties.put("tamponi", "covid19:TestPerformed");
		
		properties.put("totale_positivi", "covid19:TotalPositiveCases");
		properties.put("nuovi_positivi", "covid19:DailyPositiveCases");
		properties.put("variazione_totale_positivi", "covid19:DeltaTotalPositiveCases");
	}
	
	public AddObservations(String hostFile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile,null);
	}
	
	public AddObservations() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		this(null,null);
	}
	
	public void addNationalObservations(String graph,JsonArray array) throws FileNotFoundException, SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		/*
		 *     {
        "data": "2020-03-31T17:00:00",
        "stato": "ITA",
        "ricoverati_con_sintomi": 28192,
        "terapia_intensiva": 4023,
        "totale_ospedalizzati": 32215,
        "isolamento_domiciliare": 45420,
        "totale_positivi": 77635,
        "variazione_totale_positivi": 2107,
        "nuovi_positivi": 4053,
        "dimessi_guariti": 15729,
        "deceduti": 12428,
        "totale_casi": 105792,
        "tamponi": 506968,
        "note_it": "",
        "note_en": ""
		 * 
		 * */
		
//		JsonArray array = loadJsonArray("dpc-covid19-ita-andamento-nazionale-latest.json");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("ricoverati_con_sintomi");
		list.add("terapia_intensiva");
		list.add("totale_ospedalizzati");
		list.add("isolamento_domiciliare");
		list.add("totale_positivi");
		list.add("nuovi_positivi");
		list.add("dimessi_guariti");
		list.add("deceduti");
		list.add("totale_casi");
		list.add("tamponi");
		list.add("variazione_totale_positivi");
		
		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		
		for (JsonElement country : array) {
			String placeURIString = new String("http://covid19/context/country/" +country.getAsJsonObject().get("stato").getAsString().replace(" ", "_").replace("'", "_"));
			
			RDFTermURI place = new RDFTermURI(placeURIString);
			RDFTermLiteral timestamp = new RDFTermLiteral(country.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z"));
			
			setUpdateBindingValue("place",place );
			setUpdateBindingValue("timestamp", timestamp);
			
			for (String pro : list) {
				RDFTermURI property = new RDFTermURI(properties.get(pro));
				RDFTermLiteral value = new RDFTermLiteral(String.valueOf(country.getAsJsonObject().get(pro).getAsInt()));
				
				setUpdateBindingValue("property", property);			
				setUpdateBindingValue("value", value);	
			
				logger.info("Country: "+place+ " Property: "+property+" Value: " +value+ " Timestamp: "+timestamp); 
				
				update();
			}	
		}
	}

	public void addRegionObservations(String graph,JsonArray array) throws FileNotFoundException, SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		/*		 
		 * "totale_attualmente_positivi" ==> "totale_positivi"
		 * "nuovi_attualmente_positivi" ==> "nuovi_positivi" 
		 * 
		 * NUOVA PROPRIETA'
		 * "variazione_totale_positivi" ???
		 * 
		 * 
		 * "totale_positivi":1191,
		 * "variazione_totale_positivi":22,
		 * "nuovi_positivi":56,
		 * 
		 * */
		ArrayList<String> list = new ArrayList<String>();
		list.add("ricoverati_con_sintomi");
		list.add("terapia_intensiva");
		list.add("totale_ospedalizzati");
		list.add("isolamento_domiciliare");
		list.add("totale_positivi");
		list.add("nuovi_positivi");
		list.add("dimessi_guariti");
		list.add("deceduti");
		list.add("totale_casi");
		list.add("tamponi");
		list.add("variazione_totale_positivi");
		
		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		
		for (JsonElement prov : array) {
			String placeURIString = new String("http://covid19/Italy/Region/" +prov.getAsJsonObject().get("denominazione_regione").getAsString().replace(" ", "_").replace("'", "_"));
			
			// FIX
			if (placeURIString.endsWith("Emilia-Romagna")) placeURIString = placeURIString.replace("-", "_");
			
			RDFTermURI place = new RDFTermURI(placeURIString);
			RDFTermLiteral timestamp = new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z"));
			
			setUpdateBindingValue("place",place );
			setUpdateBindingValue("timestamp", timestamp);
			
			for (String pro : list) {
				RDFTermURI property = new RDFTermURI(properties.get(pro));
				RDFTermLiteral value = new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get(pro).getAsInt()));
				
				setUpdateBindingValue("property", property);			
				setUpdateBindingValue("value", value);	
			
				logger.info("Region: "+place+ " Property: "+property+" Value: " +value+ " Timestamp: "+timestamp); 
				
				update();
			}	
		}
	}
	
	public void addProvinceObservations(String graph,JsonArray array) throws FileNotFoundException, SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {	
		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		
		for (JsonElement prov : array) {
			RDFTermURI place = new RDFTermURI("http://covid19/Italy/Province/" + prov.getAsJsonObject().get("denominazione_provincia").getAsString().replace(" ", "_").replace("'", "_"));
			RDFTermLiteral timestamp = new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z"));
			RDFTermURI property = new RDFTermURI(properties.get("totale_casi"));
			RDFTermLiteral value =  new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("totale_casi").getAsInt()));
			
			setUpdateBindingValue("place", place);			
			setUpdateBindingValue("timestamp", timestamp);
			
			setUpdateBindingValue("property", property );
			setUpdateBindingValue("value", value);
			
			logger.info("Province: "+place+ " Property: "+property+" Value: " +value+ " Timestamp: "+timestamp);
			
			update();
		}
	}
	
	public static JsonArray loadJsonArray(String jsapFile) throws FileNotFoundException {
		FileReader in = new FileReader(jsapFile);
		
		return new JsonParser().parse(in).getAsJsonArray();
	}
	
	public static JsonArray loadCSVProvince(String csv) throws IOException {
		FileReader in = new FileReader(csv);

		String line;

		BufferedReader br = new BufferedReader(in);

		// Skip first line
		br.readLine();

		JsonArray ret = new JsonArray();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(",");

			JsonElement elem = new JsonObject();
			try {
				elem.getAsJsonObject().add("denominazione_provincia", new JsonPrimitive(fields[5]));
				elem.getAsJsonObject().add("data", new JsonPrimitive(fields[0]));
				elem.getAsJsonObject().add("totale_casi", new JsonPrimitive(fields[9]));
				ret.add(elem);
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.error(e.getMessage());
				logger.error("Wrong line: "+line);
			}
			
		}

		br.close();

		return ret;
	}

	public static JsonArray loadCSVRegioni(String csv) throws IOException {
		FileReader in = new FileReader(csv);

		String line;

		BufferedReader br = new BufferedReader(in);

		// Skip first line
		br.readLine();

		// data,stato,codice_regione,denominazione_regione,lat,long,
		// 6
		// ricoverati_con_sintomi,terapia_intensiva,totale_ospedalizzati,isolamento_domiciliare,totale_attualmente_positivi,nuovi_attualmente_positivi,dimessi_guariti,deceduti,totale_casi,tamponi
		// note_it,note_en
		JsonArray ret = new JsonArray();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(",");

			JsonElement elem = new JsonObject();
			elem.getAsJsonObject().add("denominazione_regione", new JsonPrimitive(fields[3]));
			elem.getAsJsonObject().add("data", new JsonPrimitive(fields[0]));

			elem.getAsJsonObject().add("ricoverati_con_sintomi", new JsonPrimitive(fields[6]));
			elem.getAsJsonObject().add("terapia_intensiva", new JsonPrimitive(fields[7]));
			elem.getAsJsonObject().add("totale_ospedalizzati", new JsonPrimitive(fields[8]));
			elem.getAsJsonObject().add("isolamento_domiciliare", new JsonPrimitive(fields[9]));
			elem.getAsJsonObject().add("totale_attualmente_positivi", new JsonPrimitive(fields[10]));
			elem.getAsJsonObject().add("nuovi_attualmente_positivi", new JsonPrimitive(fields[11]));
			elem.getAsJsonObject().add("dimessi_guariti", new JsonPrimitive(fields[12]));
			elem.getAsJsonObject().add("deceduti", new JsonPrimitive(fields[13]));
			elem.getAsJsonObject().add("totale_casi", new JsonPrimitive(fields[14]));
			elem.getAsJsonObject().add("tamponi", new JsonPrimitive(fields[15]));

			ret.add(elem);
		}

		br.close();

		return ret;
	}
	
	public static JsonArray loadCSVNazionale(String csv) throws IOException {
		FileReader in = new FileReader(csv);

		String line;

		BufferedReader br = new BufferedReader(in);

		// Skip first line
		br.readLine();

		//data,stato,ricoverati_con_sintomi,terapia_intensiva,totale_ospedalizzati,isolamento_domiciliare,totale_positivi,variazione_totale_positivi,nuovi_positivi,dimessi_guariti,deceduti,totale_casi,tamponi,note_it,note_en
		
		JsonArray ret = new JsonArray();
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(",");

			JsonElement elem = new JsonObject();
			elem.getAsJsonObject().add("data", new JsonPrimitive(fields[0]));
			elem.getAsJsonObject().add("stato", new JsonPrimitive(fields[1]));
			
			elem.getAsJsonObject().add("ricoverati_con_sintomi", new JsonPrimitive(fields[2]));
			elem.getAsJsonObject().add("terapia_intensiva", new JsonPrimitive(fields[3]));
			elem.getAsJsonObject().add("totale_ospedalizzati", new JsonPrimitive(fields[4]));
			elem.getAsJsonObject().add("isolamento_domiciliare", new JsonPrimitive(fields[5]));
			elem.getAsJsonObject().add("totale_positivi", new JsonPrimitive(fields[6]));
			elem.getAsJsonObject().add("variazione_totale_positivi", new JsonPrimitive(fields[7]));
			
			elem.getAsJsonObject().add("nuovi_positivi", new JsonPrimitive(fields[8]));
			elem.getAsJsonObject().add("dimessi_guariti", new JsonPrimitive(fields[9]));
			elem.getAsJsonObject().add("deceduti", new JsonPrimitive(fields[10]));
			elem.getAsJsonObject().add("totale_casi", new JsonPrimitive(fields[11]));
			elem.getAsJsonObject().add("tamponi", new JsonPrimitive(fields[12]));

			ret.add(elem);
		}

		br.close();

		return ret;
	}
}
