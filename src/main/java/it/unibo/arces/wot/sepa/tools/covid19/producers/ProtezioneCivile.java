package it.unibo.arces.wot.sepa.tools.covid19.producers;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.tools.DropGraphs;
import it.unibo.arces.wot.sepa.tools.covid19.AddObservations;
import it.unibo.arces.wot.sepa.tools.covid19.AddObservationsCSV;

public class ProtezioneCivile {

	private static String observationGraph = "http://covid19/observation";
	private static String historyGraph = "http://covid19/observation/history";
	private static String hostJsap = "host-mml.jsap";

	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException,
			SEPAPropertiesException, IOException, SEPABindingsException {
		DropGraphs agentDropGraphs = new DropGraphs(hostJsap);
		AddObservations agentObservations = new AddObservations(hostJsap);

		// Drop observation graph
		agentDropGraphs.drop(observationGraph);
		agentDropGraphs.close();

		// Produce observations
		agentObservations.addRegionObservations(observationGraph);
		agentObservations.addProvinceObservations(observationGraph);
		
		agentObservations.close();
	}
	
	protected void produceFromCSV() throws FileNotFoundException, SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException, SEPABindingsException {
		// Produce history observations
		String[] arrRegioni = {"dpc-covid19-ita-regioni-20200327.csv",
				"dpc-covid19-ita-regioni-20200326.csv",
				"dpc-covid19-ita-regioni-20200325.csv",
				"dpc-covid19-ita-regioni-20200324.csv",
				"dpc-covid19-ita-regioni-20200323.csv",
				"dpc-covid19-ita-regioni-20200322.csv",
				"dpc-covid19-ita-regioni-20200321.csv",
				"dpc-covid19-ita-regioni-20200320.csv",
				"dpc-covid19-ita-regioni-20200319.csv",
				"dpc-covid19-ita-regioni-20200318.csv",
				"dpc-covid19-ita-regioni-20200317.csv",
				"dpc-covid19-ita-regioni-20200316.csv",
				"dpc-covid19-ita-regioni-20200315.csv",
				"dpc-covid19-ita-regioni-20200314.csv",
				"dpc-covid19-ita-regioni-20200313.csv",
				"dpc-covid19-ita-regioni-20200312.csv",
				"dpc-covid19-ita-regioni-20200311.csv",
				"dpc-covid19-ita-regioni-20200310.csv",
				"dpc-covid19-ita-regioni-20200309.csv",
				"dpc-covid19-ita-regioni-20200308.csv",
				"dpc-covid19-ita-regioni-20200307.csv",
				"dpc-covid19-ita-regioni-20200306.csv",
				"dpc-covid19-ita-regioni-20200305.csv",
				"dpc-covid19-ita-regioni-20200304.csv",
				"dpc-covid19-ita-regioni-20200303.csv",
				"dpc-covid19-ita-regioni-20200302.csv",
				"dpc-covid19-ita-regioni-20200301.csv",
				
				"dpc-covid19-ita-regioni-20200229.csv",
				"dpc-covid19-ita-regioni-20200228.csv",
				"dpc-covid19-ita-regioni-20200227.csv",
				"dpc-covid19-ita-regioni-20200226.csv",
				"dpc-covid19-ita-regioni-20200225.csv",
				"dpc-covid19-ita-regioni-20200224.csv"
				};
		
		String[] arrProvince = {
				"dpc-covid19-ita-province-20200327.csv",
				"dpc-covid19-ita-province-20200326.csv",
				"dpc-covid19-ita-province-20200325.csv",
				"dpc-covid19-ita-province-20200324.csv",
				"dpc-covid19-ita-province-20200323.csv",
				"dpc-covid19-ita-province-20200322.csv",
				"dpc-covid19-ita-province-20200321.csv",
				"dpc-covid19-ita-province-20200320.csv",
				"dpc-covid19-ita-province-20200319.csv",
				"dpc-covid19-ita-province-20200318.csv",
				"dpc-covid19-ita-province-20200317.csv",
				"dpc-covid19-ita-province-20200316.csv",
				"dpc-covid19-ita-province-20200315.csv",
				"dpc-covid19-ita-province-20200314.csv",
				"dpc-covid19-ita-province-20200313.csv",
				"dpc-covid19-ita-province-20200312.csv",
				"dpc-covid19-ita-province-20200311.csv",
				"dpc-covid19-ita-province-20200310.csv",
				"dpc-covid19-ita-province-20200309.csv",
				"dpc-covid19-ita-province-20200308.csv",
				"dpc-covid19-ita-province-20200307.csv",
				"dpc-covid19-ita-province-20200306.csv",
				"dpc-covid19-ita-province-20200305.csv",
				"dpc-covid19-ita-province-20200304.csv",
				"dpc-covid19-ita-province-20200303.csv",
				"dpc-covid19-ita-province-20200302.csv",
				"dpc-covid19-ita-province-20200301.csv",
				
				"dpc-covid19-ita-province-20200229.csv",
				"dpc-covid19-ita-province-20200228.csv",
				"dpc-covid19-ita-province-20200227.csv",
				"dpc-covid19-ita-province-20200226.csv",
				"dpc-covid19-ita-province-20200225.csv",
				"dpc-covid19-ita-province-20200224.csv"
				};
		
		AddObservationsCSV agentObservations = new AddObservationsCSV(hostJsap);
		
		for (String province : arrProvince) agentObservations.addProvinceObservations(historyGraph, province);
		for (String region : arrRegioni) agentObservations.addRegionObservations(historyGraph, region);
		
		agentObservations.close();	
	}

}
