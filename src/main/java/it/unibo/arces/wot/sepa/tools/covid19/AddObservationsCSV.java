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
data,stato,codice_regione,denominazione_regione,codice_provincia,denominazione_provincia,sigla_provincia,lat,long,totale_casi,note_it,note_en

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
data,stato,codice_regione,denominazione_regione,lat,long,ricoverati_con_sintomi,terapia_intensiva,totale_ospedalizzati,isolamento_domiciliare,totale_attualmente_positivi,nuovi_attualmente_positivi,dimessi_guariti,deceduti,totale_casi,tamponi,note_it,note_en

 * 
 * */
public class AddObservationsCSV extends Producer {
	private HashMap<String, String> properties = new HashMap<String, String>();

	public AddObservationsCSV(String hostFile, ClientSecurityManager sm) throws SEPAProtocolException,
			SEPASecurityException, SEPAPropertiesException, FileNotFoundException, IOException {
		super(new JSAP("observations.jsap"), "ADD_OBSERVATION", sm);

		if (hostFile != null)
			appProfile.read(hostFile, true);

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

	public AddObservationsCSV(String hostFile) throws SEPAProtocolException, SEPASecurityException,
			SEPAPropertiesException, FileNotFoundException, IOException {
		this(hostFile, null);
	}

	public AddObservationsCSV() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException,
			FileNotFoundException, IOException {
		this(null, null);
	}

	public void addRegionObservations(String graph, String fileCSV) throws SEPABindingsException, SEPASecurityException,
			SEPAProtocolException, SEPAPropertiesException, IOException {
		JsonArray array = loadCSVRegioni(fileCSV);

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
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Region/" + prov.getAsJsonObject()
					.get("denominazione_regione").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("timestamp",
					new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z")));

			for (String pro : list) {
				setUpdateBindingValue("property", new RDFTermURI(properties.get(pro)));
				setUpdateBindingValue("value",
						new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get(pro).getAsInt())));

				update();
			}
		}
	}

	public void addProvinceObservations(String graph, String fileCSV) throws SEPABindingsException,
			SEPASecurityException, SEPAProtocolException, SEPAPropertiesException, IOException {
		JsonArray array = loadCSVProvince(fileCSV);

		setUpdateBindingValue("unit", new RDFTermURI("unit:Number"));
		setUpdateBindingValue("graph", new RDFTermURI(graph));

		for (JsonElement prov : array) {
			setUpdateBindingValue("place", new RDFTermURI("http://covid19/Italy/Province/" + prov.getAsJsonObject()
					.get("denominazione_provincia").getAsString().replace(" ", "_").replace("'", "_")));
			setUpdateBindingValue("timestamp",
					new RDFTermLiteral(prov.getAsJsonObject().get("data").getAsString().replace(" ", "T").concat("Z")));

			setUpdateBindingValue("property", new RDFTermURI(properties.get("totale_casi")));
			setUpdateBindingValue("value",
					new RDFTermLiteral(String.valueOf(prov.getAsJsonObject().get("totale_casi").getAsInt())));

			update();
		}
	}

	public JsonArray loadCSVProvince(String csv) throws IOException {
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

	public JsonArray loadCSVRegioni(String csv) throws IOException {
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
}
