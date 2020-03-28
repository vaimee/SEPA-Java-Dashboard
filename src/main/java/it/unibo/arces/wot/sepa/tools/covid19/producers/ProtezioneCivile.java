package it.unibo.arces.wot.sepa.tools.covid19.producers;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.tools.DropGraphs;
import it.unibo.arces.wot.sepa.tools.covid19.AddObservations;

public class ProtezioneCivile {

	private static String observationGraph = "http://test/covid19/observation";
	private static String historyGraph = "http://test/covid19/observation/history";
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
		
		// Produce history observations
		agentObservations.addRegionObservations(historyGraph);
		agentObservations.addProvinceObservations(historyGraph);
		
		agentObservations.close();
	}

}
