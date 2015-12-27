package de.tudresden.inf.lat.born.owlapi.multiprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tudresden.inf.lat.born.owlapi.processor.ProcessorConfigurationImpl;
import de.tudresden.inf.lat.born.owlapi.processor.ProcessorCore;

/**
 * An object of this class processed several ontologies with their respective
 * Bayesian networks, using pseudorandomly generated queries.
 * 
 * @author Julian Mendez
 *
 */
public class MultiProcessorCore {

	public static final char TAB_CHAR = '\t';
	public static final char NEW_LINE_CHAR = '\n';
	public static final char SLASH_CHAR = '/';
	public static final String TEMP_FILE_SUFFIX = ".tmp";
	public static final String CSV_EXTENSION = ".csv";
	public static final String OWL_EXTENSION = ".owl";
	public static final String PL_EXTENSION = ".pl";

	/**
	 * Constructs a new multi processor core.
	 */
	public MultiProcessorCore() {
	}

	List<OWLClass> getClasses(OWLOntology ontology) {
		Objects.requireNonNull(ontology);
		List<OWLClass> listOfClasses = new ArrayList<>();
		Set<OWLClass> treeSet = new TreeSet<>();
		treeSet.addAll(ontology.getClassesInSignature());
		listOfClasses.addAll(treeSet);
		return listOfClasses;
	}

	SubsumptionQuery getNextQuery(List<OWLClass> listOfClasses, PseudorandomNumberGenerator random) {
		Objects.requireNonNull(listOfClasses);
		Objects.requireNonNull(random);
		int n = listOfClasses.size();
		int a = random.nextInt(n);
		int b = random.nextInt(n);
		return new SubsumptionQuery(listOfClasses.get(a), listOfClasses.get(b));
	}

	List<SubsumptionQuery> getQueries(OWLOntology ontology, int numberOfQueries, PseudorandomNumberGenerator random) {
		Objects.requireNonNull(ontology);
		Objects.requireNonNull(random);
		List<OWLClass> listOfClasses = getClasses(ontology);
		List<SubsumptionQuery> listOfQueries = new ArrayList<>();
		IntStream.range(0, numberOfQueries).forEach(x -> {
			listOfQueries.add(getNextQuery(listOfClasses, random));
		});
		return listOfQueries;
	}

	public List<String> run(MultiProcessorConfigurationImpl conf, long start) {
		Objects.requireNonNull(conf);
		List<String> ret = new ArrayList<>();
		PseudorandomNumberGenerator random = new PseudorandomNumberGenerator(conf.getSeed());
		ProcessorCore core = new ProcessorCore();

		conf.getOntologyList().forEach(ontPair -> {

			ProcessorConfigurationImpl configuration = new ProcessorConfigurationImpl();
			configuration.setOntology(ontPair.getOntology());
			configuration.setBayesianNetwork(ontPair.getBayesianNetwork());
			configuration.setOutputFileName(
					conf.getOutputDirectory() + SLASH_CHAR + ontPair.getOntologyName() + TEMP_FILE_SUFFIX);
			configuration.setQueryProcessor(conf.getQueryProcessor());
			configuration.setShowingLog(conf.isShowingLog());
			List<SubsumptionQuery> queries = getQueries(ontPair.getOntology(), conf.getNumberOfQueries(), random);
			StringBuffer sbuf = new StringBuffer();

			queries.forEach(query -> {
				configuration.setQuery(query.asProblogString());
				String result = core.run(configuration, start);
				sbuf.append(query.getSubClass().getIRI());
				sbuf.append(TAB_CHAR);
				sbuf.append(query.getSuperClass().getIRI());
				sbuf.append(TAB_CHAR);
				sbuf.append(result.trim());
				sbuf.append(NEW_LINE_CHAR);
			});
			ret.add(sbuf.toString());

		});
		return ret;
	}

	public static List<OntologyAndNetwork> getOntologyAndNetworkList(String ontologyDirectory,
			String bayesianNetworkDirectory) {
		Objects.requireNonNull(ontologyDirectory);
		Objects.requireNonNull(bayesianNetworkDirectory);
		try {
			List<OntologyAndNetwork> ret = new ArrayList<>();
			if (!ontologyDirectory.isEmpty() && !bayesianNetworkDirectory.isEmpty()) {
				File file = new File(ontologyDirectory);
				File[] files = file.listFiles();
				Arrays.sort(files);

				for (int index = 0; index < files.length; index++) {

					String fileName = files[index].getName();
					if (fileName.endsWith(OWL_EXTENSION)) {
						String ontologyName = fileName.substring(0, fileName.length() - OWL_EXTENSION.length());

						File ontologyFile = new File(ontologyDirectory + SLASH_CHAR + ontologyName + OWL_EXTENSION);
						File bayesianNetworkFile = new File(
								bayesianNetworkDirectory + SLASH_CHAR + ontologyName + PL_EXTENSION);

						OWLOntology owlOntology = ProcessorConfigurationImpl
								.readOntology(new FileInputStream(ontologyFile));

						String bayesianNetwork = "";
						if (bayesianNetworkFile.exists()) {
							bayesianNetwork = ProcessorConfigurationImpl.read(new FileReader(bayesianNetworkFile));
						}

						ret.add(new OntologyAndNetwork(ontologyName, owlOntology, bayesianNetwork));
					}
				}
			}
			return ret;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	public void storeResults(MultiProcessorConfigurationImpl conf, List<String> list) throws IOException {
		Objects.requireNonNull(conf);
		Objects.requireNonNull(list);
		Iterator<String> resultIt = list.iterator();
		List<OntologyAndNetwork> ontologyList = conf.getOntologyList();
		String outputDirectory = conf.getOutputDirectory();
		for (OntologyAndNetwork ontNet : ontologyList) {
			String result = resultIt.next();
			String fileName = outputDirectory + SLASH_CHAR + ontNet.getOntologyName() + CSV_EXTENSION;
			FileWriter fileWriter = new FileWriter(fileName);
			fileWriter.write(result);
			fileWriter.flush();
			fileWriter.close();
		}
	}

}
