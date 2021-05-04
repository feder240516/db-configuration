package main;

import static org.jooq.impl.DSL.*;
import org.jooq.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.jooq.DatePart;
import org.jooq.Query;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import ai.libs.hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;
import services.CSVService;

public class MainHASCO {
	
	public static Query generateQuerySelectSalaries() {
		return select(field("employees.emp_no"), 
						field("employees.first_name"), 
						field("employees.last_name"), 
						field("salaries.salary"))
				.from("employees")
				.join("salaries")
				.on(field("employees.emp_no").eq(field("salaries.emp_no")))
				.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
	}

	public static void main(String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		int[] ports = new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909};
		PortManager.getInstance().setupAvailablePorts(ports);
		
		File newFile = new File("src/main/java/configuration/dbTestProblem.json");
		System.out.println(newFile.getCanonicalPath());
		
		Query selectSalaries = generateQuerySelectSalaries();
		
		TestDescription td1 = new TestDescription("Only select salaries", 2);
	    td1.addQuery(1, selectSalaries);
		
		Benchmarker b = new Benchmarker(td1, 2);
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<Double>(newFile, "IDatabase", (ci) -> {
			try {
				return b.benchmark(ci);
			} catch (ExecutionException | UnavailablePortsException | IOException | SQLException e) {
				e.printStackTrace();
				return Double.POSITIVE_INFINITY;
			}
		} )  ;
		HASCOViaFD<Double> hasco = HASCOBuilder.get()
					.withProblem(problem)
					.withBlindSearch()
					.withTimeout(new Timeout(1, TimeUnit.MINUTES))
					.withCPUs(2)
					.getAlgorithm();
		
		
		
		
		//hasco.registerSolutionEventListener(e -> System.out.println(" ------> Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance()));
		hasco.registerListener(new Object() {

			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(new ComponentSerialization().serialize(solutionEvent.getSolutionCandidate().getComponentInstance()));
				
			}
		});
		//AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(hasco);
		//window.withMainPlugin(new GraphViewPlugin());
		//window.withPlugin(new SolutionPerformanceTimelinePlugin(new HASCOSolutionCandidateRepresenter()));
		//window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new HASCOModelStatisticsPlugin());
		try {
			hasco.call();
		} catch (Exception e) {
			System.out.println("Finished");
			System.out.println(String.format("Best candidate for 30 minutes execution: %s", hasco.getBestSeenSolution().toString()));
			CSVService.getInstance().dumpWithVars("_HASCO1");
		}
		
		//System.out.println(String.format("Puntaje: %f", hasco.nextSolutionCandidate().getScore()));
		/*hasco.getReport().getSolutionCandidates().forEach((candidate) -> {
			//System.out.println(candidate.);
		}); */
		
	}

}
