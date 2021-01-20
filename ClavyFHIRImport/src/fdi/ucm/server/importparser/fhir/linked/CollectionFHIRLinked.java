package fdi.ucm.server.importparser.fhir.linked;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;


public class CollectionFHIRLinked {
	
	public boolean debugfile=false;

	
	public static void main(String[] args) {
		CollectionJSON PatientJSONParser=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		PatientJSONParser.procesaJSONFolder("files/ex1/Patient", log);
		CompleteCollection patientCollection = PatientJSONParser.getCollection();
		
		CollectionJSON DiagnosticReportJSONParser=new CollectionJSON();
		DiagnosticReportJSONParser.procesaJSONFolder("files/ex1/DiagnosticReport", log);
		CompleteCollection DReportCollection = DiagnosticReportJSONParser.getCollection();
		
		CollectionJSON ConditionReportJSONParser=new CollectionJSON();
		ConditionReportJSONParser.procesaJSONFolder("files/ex1/Condition", log);
		CompleteCollection CReportCollection = ConditionReportJSONParser.getCollection();
		
		CollectionJSON ImagingStudyReportJSONParser=new CollectionJSON();
		ImagingStudyReportJSONParser.procesaJSONFolder("files/ex1/ImagingStudy", log);
		CompleteCollection IReportCollection = ImagingStudyReportJSONParser.getCollection();
		
		CompleteCollection C=new CompleteCollection("ex1", "ex1");
		

		
		C.getMetamodelGrammar().addAll(patientCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(patientCollection.getEstructuras());
		C.getSectionValues().addAll(patientCollection.getSectionValues());
		
		C.getMetamodelGrammar().addAll(DReportCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(DReportCollection.getEstructuras());
		C.getSectionValues().addAll(DReportCollection.getSectionValues());
		
		C.getMetamodelGrammar().addAll(CReportCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(CReportCollection.getEstructuras());
		C.getSectionValues().addAll(CReportCollection.getSectionValues());
		
		C.getMetamodelGrammar().addAll(IReportCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(IReportCollection.getEstructuras());
		C.getSectionValues().addAll(IReportCollection.getSectionValues());
		
		
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		
		ConfigFHIRYAML order=new ConfigFHIRYAML();
		try {
			order = mapper.readValue(new File("src/fdi/ucm/server/importparser/fhir/linked/config.yml"), ConfigFHIRYAML.class);
		} catch (JsonParseException e1) {
			log.add("Error YAML parse");
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			log.add("Error YAML mapping");
			e1.printStackTrace();
		} catch (IOException e1) {
			log.add("Error YAML IO");
			e1.printStackTrace();
		}
		
		for (ConfigFHIRYAMLBrowse Browse : order.getBrowseable()) {
			String GrammarAPp=Browse.getGrammar().toLowerCase();
			switch (GrammarAPp) {
			case "patient":
				
				break;
				
			case "diagnosticreport":
				
				break;

			default:
				break;
			}
			
		}
		
		for (ConfigFHIRYAMLlink Browse : order.getLink()) {
			String GrammarAPp=Browse.getGrammar().toLowerCase();
			switch (GrammarAPp) {
			case "patient":
				
				break;
				
			case "diagnosticreport":
//				DiagnosticReportJSONParser.getPath()
				break;

			default:
				break;
			}
			
		}
		
		
		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));

				oos.writeObject(C);

				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}



}
