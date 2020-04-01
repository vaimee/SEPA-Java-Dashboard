package it.unibo.arces.wot.sepa.tools.covid19.producers;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.JsonArray;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.tools.DropGraphs;
import it.unibo.arces.wot.sepa.tools.covid19.AddObservations;

public class ProtezioneCivile {

	private static String observationGraph = "http://covid19/observation";
	private static String historyGraph = "http://covid19/observation/history";
	private static String hostJsap = "host-mml.jsap";

	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException,
			SEPAPropertiesException, IOException, SEPABindingsException {
		
		// Drop observation graph
		DropGraphs agentDropGraphs = new DropGraphs(hostJsap);
		agentDropGraphs.drop(observationGraph);
		agentDropGraphs.close();
		
		AddObservations agentObservations = new AddObservations(hostJsap);
		
		JsonArray set = AddObservations.loadJsonArray("dpc-covid19-ita-andamento-nazionale-latest.json");
		
		agentObservations.addNationalObservations(observationGraph,set);
		agentObservations.addNationalObservations(historyGraph,set);
		
		set = AddObservations.loadJsonArray("dpc-covid19-ita-regioni-latest.json");
		
		agentObservations.addRegionObservations(observationGraph,set);
		agentObservations.addRegionObservations(historyGraph,set);
		
		set = AddObservations.loadJsonArray("dpc-covid19-ita-province-latest.json");
		
		agentObservations.addProvinceObservations(observationGraph,set);
		agentObservations.addProvinceObservations(historyGraph,set);
		
		agentObservations.close();
	}
	
//	public static void main(String[] args) throws FileNotFoundException, SEPAProtocolException, SEPASecurityException,
//	SEPAPropertiesException, IOException, SEPABindingsException {
//		updateNationalHistory();
//	}
	
	public static void updateNationalHistory() throws FileNotFoundException, SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException, SEPABindingsException {
		// Produce history observations
		String[] set = {
				"dpc-covid19-ita-andamento-nazionale-20200330.csv",
				"dpc-covid19-ita-andamento-nazionale-20200329.csv",
				"dpc-covid19-ita-andamento-nazionale-20200328.csv",
				"dpc-covid19-ita-andamento-nazionale-20200327.csv",
				"dpc-covid19-ita-andamento-nazionale-20200326.csv",
				"dpc-covid19-ita-andamento-nazionale-20200325.csv",
				"dpc-covid19-ita-andamento-nazionale-20200324.csv",
				"dpc-covid19-ita-andamento-nazionale-20200323.csv",
				"dpc-covid19-ita-andamento-nazionale-20200322.csv",
				"dpc-covid19-ita-andamento-nazionale-20200321.csv",
				"dpc-covid19-ita-andamento-nazionale-20200320.csv",
				"dpc-covid19-ita-andamento-nazionale-20200319.csv",
				"dpc-covid19-ita-andamento-nazionale-20200318.csv",
				"dpc-covid19-ita-andamento-nazionale-20200317.csv",
				"dpc-covid19-ita-andamento-nazionale-20200316.csv",
				"dpc-covid19-ita-andamento-nazionale-20200315.csv",
				"dpc-covid19-ita-andamento-nazionale-20200314.csv",
				"dpc-covid19-ita-andamento-nazionale-20200313.csv",
				"dpc-covid19-ita-andamento-nazionale-20200312.csv",
				"dpc-covid19-ita-andamento-nazionale-20200311.csv",
				"dpc-covid19-ita-andamento-nazionale-20200310.csv",
				"dpc-covid19-ita-andamento-nazionale-20200309.csv",
				"dpc-covid19-ita-andamento-nazionale-20200308.csv",
				"dpc-covid19-ita-andamento-nazionale-20200307.csv",
				"dpc-covid19-ita-andamento-nazionale-20200306.csv",
				"dpc-covid19-ita-andamento-nazionale-20200305.csv",
				"dpc-covid19-ita-andamento-nazionale-20200304.csv",
				"dpc-covid19-ita-andamento-nazionale-20200303.csv",
				"dpc-covid19-ita-andamento-nazionale-20200302.csv",
				"dpc-covid19-ita-andamento-nazionale-20200301.csv",
				
				"dpc-covid19-ita-andamento-nazionale-20200229.csv",
				"dpc-covid19-ita-andamento-nazionale-20200228.csv",
				"dpc-covid19-ita-andamento-nazionale-20200227.csv",
				"dpc-covid19-ita-andamento-nazionale-20200226.csv",
				"dpc-covid19-ita-andamento-nazionale-20200225.csv",
				"dpc-covid19-ita-andamento-nazionale-20200224.csv"
				};
		
		AddObservations agentObservations = new AddObservations(hostJsap);
		
		for (String file : set) {
			JsonArray array = AddObservations.loadCSVNazionale(file);
			agentObservations.addNationalObservations(historyGraph, array);
		}
				
		agentObservations.close();	
	}

}
