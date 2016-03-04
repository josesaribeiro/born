package de.tudresden.inf.lat.born.tool.cycleremover;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.functional.renderer.OWLFunctionalSyntaxRenderer;
import org.semanticweb.owlapi.io.OWLRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tudresden.inf.lat.born.owlapi.processor.ProcessorConfigurationImpl;

/**
 * This is an executable class to remove cycles from an ontology.
 * 
 * @author Julian Mendez
 *
 */
public class CycleRemover {

	public static final String HELP = "Parameters: \n" + //
			"  <ontology file> <output file>: removes the cycles in a given ontology \n";
	public static final String NEW_ONTOLOGY_SUFFIX = "-acyclic";

	public CycleRemover() {
	}

	Set<OWLAxiom> removeCycles(OWLDataFactory dataFactory, Set<OWLAxiom> axioms) {
		Set<OWLAxiom> newAxioms = new HashSet<>();
		CycleDetector axiomFilter = new CycleDetector(dataFactory);
		axioms.forEach(axiom -> {
			boolean axiomWasAccepted = axiom.accept(axiomFilter);
			if (axiomWasAccepted) {
				newAxioms.add(axiom);
			}
		});
		return newAxioms;
	}

	OWLOntology removeCycles(OWLOntology owlOntology) throws OWLOntologyCreationException {
		Objects.requireNonNull(owlOntology);
		Set<OWLAxiom> newAxioms = removeCycles(owlOntology.getOWLOntologyManager().getOWLDataFactory(),
				owlOntology.getAxioms());
		IRI oldOntologyIri = owlOntology.getOntologyID().getOntologyIRI().get();
		IRI newOntologyIri = IRI.create(oldOntologyIri.toURI().toASCIIString() + NEW_ONTOLOGY_SUFFIX);
		return owlOntology.getOWLOntologyManager().createOntology(newAxioms, newOntologyIri);
	}

	void removeCycle(String ontologyFileName, String outputFileName) throws OWLException, IOException {
		Objects.requireNonNull(ontologyFileName);
		Objects.requireNonNull(outputFileName);
		OWLOntology owlOntology = ProcessorConfigurationImpl.readOntology(new FileInputStream(ontologyFileName));
		storeOntology(removeCycles(owlOntology), outputFileName);
	}

	void storeOntology(OWLOntology owlOntology, String fileName) throws OWLException, IOException {
		Objects.requireNonNull(owlOntology);
		Objects.requireNonNull(fileName);
		OWLRenderer renderer = new OWLFunctionalSyntaxRenderer();
		FileOutputStream output = new FileOutputStream(fileName);
		renderer.render(owlOntology, output);
		output.flush();
		output.close();
	}

	public static void main(String[] args) throws IOException, OWLException {
		Objects.requireNonNull(args);
		if (args.length == 2) {
			String ontologyFileName = args[0];
			String outputFileName = args[1];
			CycleRemover instance = new CycleRemover();
			instance.removeCycle(ontologyFileName, outputFileName);

		} else {
			System.out.println(HELP);

		}
	}

}
