package es.us.isa;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;

import es.us.isa.ppinot.evaluation.Aggregator;
import es.us.isa.ppinot.evaluation.evaluators.LogMeasureEvaluator;
import es.us.isa.ppinot.evaluation.Measure;
import es.us.isa.ppinot.evaluation.evaluators.MeasureEvaluator;
import es.us.isa.ppinot.evaluation.TemporalMeasureScope;
import es.us.isa.ppinot.evaluation.logs.LogProvider;
import es.us.isa.ppinot.evaluation.logs.MXMLLog;
import es.us.isa.ppinot.model.MeasureDefinition;
import es.us.isa.ppinot.model.Schedule;
import es.us.isa.ppinot.model.TimeUnit;
import es.us.isa.ppinot.model.aggregated.AggregatedMeasure;
import es.us.isa.ppinot.model.base.CountMeasure;
import es.us.isa.ppinot.model.base.TimeMeasure;
import es.us.isa.ppinot.model.condition.TimeInstantCondition;
import es.us.isa.ppinot.model.condition.TimeMeasureType;
import es.us.isa.ppinot.model.derived.DerivedSingleInstanceMeasure;
import es.us.isa.ppinot.model.scope.Period;
import es.us.isa.ppinot.model.scope.SimpleTimeFilter;
import es.us.isa.ppinot.model.state.GenericState;

public class App {
	
	private static final Schedule WORKINGHOURS = new Schedule(DateTimeConstants.MONDAY, DateTimeConstants.FRIDAY, new LocalTime(8,0), new LocalTime(20,0)); 

	public static void main(String[] args) throws Exception {
		App app = new App();
		List<Measure> measures = app.compute(app.buildResponseTime());
		app.printMeasures(measures);
		measures = app.compute(app.buildPresenceTime());
		app.printMeasures(measures);
		measures = app.compute(app.buildDocumentationTime());
		app.printMeasures(measures);
		measures = app.compute(app.buildPlanVsTotal());
		app.printMeasures(measures);
		measures = app.compute(app.buildMaxCorrections());
		app.printMeasures(measures);
		measures = app.compute(app.avgDuration());
		app.printMeasures(measures);
		measures = app.compute(app.buildExtra());
		app.printMeasures(measures);
	}
	
	private List<Measure> compute(MeasureDefinition measure) throws Exception {
		LogProvider mxmlLog = new MXMLLog(new FileInputStream(new File("logs/simulation_logs.mxml")), null);
		MeasureEvaluator evaluator = new LogMeasureEvaluator(mxmlLog);		

		return evaluator.eval(measure, new SimpleTimeFilter(Period.MONTHLY, 1, false));
	}

	private TimeMeasure buildResponseTime() {
		TimeMeasure responseTime = new TimeMeasure();
		responseTime.setFrom(new TimeInstantCondition("EVENT 2 START MESSAGE", GenericState.START));
		responseTime.setTo(new TimeInstantCondition("Plan FI", GenericState.END));
		responseTime.setConsiderOnly(WORKINGHOURS);
	    responseTime.setUnitOfMeasure(TimeUnit.HOURS);
		return responseTime;
	}

	private TimeMeasure buildPresenceTime() {
		TimeMeasure presenceTime = new TimeMeasure();
		presenceTime.setFrom(new TimeInstantCondition("Plan FI", GenericState.END));
		presenceTime.setTo(new TimeInstantCondition("Go to venue", GenericState.END));
		presenceTime.setConsiderOnly(WORKINGHOURS);
	    presenceTime.setUnitOfMeasure(TimeUnit.HOURS);
		return presenceTime;
	}

	private TimeMeasure buildResolutionTime() {
		TimeMeasure resolutionTime = new TimeMeasure();
		resolutionTime.setFrom(new TimeInstantCondition("Perform FI", GenericState.START));
		resolutionTime.setTo(new TimeInstantCondition("Perform FI", GenericState.END));
		resolutionTime.setConsiderOnly(WORKINGHOURS);
	    resolutionTime.setUnitOfMeasure(TimeUnit.HOURS);
		return resolutionTime;
	}

	private TimeMeasure buildDocumentationTime() {
		TimeMeasure documentationTime = new TimeMeasure();
		documentationTime.setFrom(new TimeInstantCondition("Create and submit FI documentation", GenericState.START));
		documentationTime.setTo(new TimeInstantCondition("Create and submit FI documentation", GenericState.END));
		documentationTime.setTimeMeasureType(TimeMeasureType.CYCLIC);
		documentationTime.setSingleInstanceAggFunction(Aggregator.SUM);
		documentationTime.setConsiderOnly(WORKINGHOURS);
	    documentationTime.setUnitOfMeasure(TimeUnit.HOURS);
	    return documentationTime;
	}

	private MeasureDefinition buildPlanVsTotal() throws Exception {
		
		TimeMeasure responseTime = buildResponseTime();		
		TimeMeasure presenceTime = buildPresenceTime();		
		TimeMeasure resolutionTime = buildResolutionTime();		
        
        DerivedSingleInstanceMeasure planVsTotal = new DerivedSingleInstanceMeasure();
        
        planVsTotal.setFunction("tresp / (tresp + tpres + tres)");
        planVsTotal.addUsedMeasure("tresp", responseTime);
        planVsTotal.addUsedMeasure("tpres", presenceTime);
        planVsTotal.addUsedMeasure("tres", resolutionTime);
        
        return planVsTotal;
	}
	
	private MeasureDefinition buildMaxCorrections() {
		CountMeasure numCorrections = new CountMeasure();
		numCorrections.setWhen(new TimeInstantCondition("Correction required", GenericState.START));
		
		AggregatedMeasure maxCorrections = new AggregatedMeasure();
		maxCorrections.setBaseMeasure(numCorrections);
		maxCorrections.setAggregationFunction(Aggregator.MAX);
		
		return maxCorrections;
	}
	
	private MeasureDefinition avgDuration() {
		TimeMeasure duration = new TimeMeasure();
		duration.setFrom(new TimeInstantCondition("EVENT 2 START MESSAGE", GenericState.START));
		duration.setTo(new TimeInstantCondition("FI closed", GenericState.END));
		duration.setConsiderOnly(WORKINGHOURS);
		duration.setUnitOfMeasure(TimeUnit.HOURS);
		
		AggregatedMeasure avgDuration = new AggregatedMeasure();
		avgDuration.setBaseMeasure(duration);
		avgDuration.setAggregationFunction(Aggregator.AVG);
		
		return avgDuration;	
	}
	

	private MeasureDefinition buildExtra() {
		TimeMeasure responseTime = buildResponseTime();
		TimeMeasure presenceTime = buildPresenceTime();
		TimeMeasure resolutionTime = buildResolutionTime();
		TimeMeasure documentationTime = buildDocumentationTime();
		
		DerivedSingleInstanceMeasure accomplishedIntervention = new DerivedSingleInstanceMeasure();
        accomplishedIntervention.setFunction("(tresp < 0.5 && tpres < 4 && tres < 2 && (tdoc < 4 || Double.isNaN(tdoc)))");
		accomplishedIntervention.addUsedMeasure("tresp", responseTime);
		accomplishedIntervention.addUsedMeasure("tpres", presenceTime);
		accomplishedIntervention.addUsedMeasure("tres", resolutionTime);
		accomplishedIntervention.addUsedMeasure("tdoc", documentationTime);
		
		AggregatedMeasure percAccomplished = new AggregatedMeasure();
		percAccomplished.setBaseMeasure(accomplishedIntervention);
		percAccomplished.setAggregationFunction(Aggregator.AVG);
				
		return percAccomplished;	
	}
	
	private void printMeasures(List<Measure> measures) {
		for (Measure m: measures) {
        	System.out.println("Value: " + m.getValue());
        	System.out.println("Number of instances: " + m.getInstances().size());
        	System.out.println("Instances: " + m.getInstances());
        	if (m.getMeasureScope() instanceof TemporalMeasureScope) {
        		TemporalMeasureScope tempScope = (TemporalMeasureScope) m.getMeasureScope();
        		System.out.println("Start: " + tempScope.getStart().toString());
        		System.out.println("End: " + tempScope.getEnd().toString());
        	}
        	System.out.println("--");
		}
	}

}
