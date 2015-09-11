package de.tudresden.inf.lat.born.owlapi.splitter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.tudresden.inf.lat.born.core.term.SubApp;
//for OWL API 3.5.1

//for OWL API 4.0.2
//import org.semanticweb.owlapi.owlxml.renderer.OWLXMLRenderer;

/**
 * An object of this class splits a probabilistic OWL ontology in two parts: an
 * OWL ontology with variables, and a Bayesian network.
 * 
 * @see SplitterCore
 * 
 * @author Julian Mendez
 *
 */
public class SplitterSubApp implements SubApp {

	public static final String HELP = "Parameters: <input ontology> <output ontology> <Bayesian network>";

	/**
	 * Constructs a new splitter.
	 */
	public SplitterSubApp() {
	}

	@Override
	public String getHelp() {
		return HELP;
	}

	@Override
	public boolean isValid(String args[]) {
		return (args.length == 3);
	}

	@Override
	public String run(String args[]) {
		if (isValid(args)) {
			try {
				SplitterConfiguration conf = new SplitterConfiguration();

				InputStream in = new FileInputStream(args[0]);
				conf.setInputOntology(in);

				OutputStream outOnt = new FileOutputStream(args[1]);
				conf.setOutputOntology(outOnt);

				OutputStream outNet = new FileOutputStream(args[2]);
				conf.setBayesianNetwork(outNet);

				SplitterCore core = new SplitterCore();
				core.run(conf);

				in.close();
				outNet.close();
				outOnt.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return "Done.";
		} else {
			return getHelp();
		}
	}

}