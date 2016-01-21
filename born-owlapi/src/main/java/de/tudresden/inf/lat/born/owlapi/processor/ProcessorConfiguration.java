package de.tudresden.inf.lat.born.owlapi.processor;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This interface models the configuration of a processor.
 * 
 * @author Julian Mendez
 *
 */
public interface ProcessorConfiguration {

	/**
	 * Returns the completion rules.
	 * 
	 * @return the completion rules
	 */
	String getCompletionRules();

	/**
	 * Sets the completion rules.
	 * 
	 * @param completionRules
	 *            completion rules
	 */
	void setCompletionRules(String completionRules);

	/**
	 * Returns the ontology.
	 * 
	 * @return the ontology
	 */
	OWLOntology getOntology();

	/**
	 * Sets the ontology.
	 * 
	 * @param ontologyInputStream
	 *            the ontology
	 */
	void setOntology(OWLOntology ontologyInputStream);

	/**
	 * Returns the Bayesian network.
	 * 
	 * @return the Bayesian network
	 */
	String getBayesianNetwork();

	/**
	 * Sets the Bayesian network.
	 * 
	 * @param bayesianNetwork
	 *            Bayesian network
	 */
	void setBayesianNetwork(String bayesianNetwork);

	/**
	 * Returns the query.
	 * 
	 * @return the query
	 */
	String getQuery();

	/**
	 * Sets the query.
	 * 
	 * @param query
	 *            query
	 */
	void setQuery(String query);

	/**
	 * Returns the output file name.
	 * 
	 * @return the output file name
	 */
	String getOutputFileName();

	/**
	 * Sets the output file name.
	 * 
	 * @param outputFileName
	 *            output file name
	 */
	void setOutputFileName(String outputFileName);

	/**
	 * Returns the query processor.
	 * 
	 * @return the query processor
	 */
	QueryProcessor getQueryProcessor();

	/**
	 * Sets the query processor.
	 * 
	 * @param queryProcessor
	 *            the query processor
	 */
	void setQueryProcessor(QueryProcessor queryProcessor);

	/**
	 * Tells whether the log is showing.
	 * 
	 * @return <code>true</code> if and only if the log is showing
	 */
	boolean isShowingLog();

	/**
	 * Sets whether the log is showing.
	 * 
	 * @param showingLog
	 *            <code>true</code> if and only if the log is showing
	 */
	void setShowingLog(boolean showingLog);

}
