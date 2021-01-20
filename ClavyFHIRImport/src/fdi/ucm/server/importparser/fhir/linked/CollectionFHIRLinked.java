package fdi.ucm.server.importparser.fhir.linked;


import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;


public class CollectionFHIRLinked {
	
	public boolean debugfile=false;

	
	public static void main(String[] args) {
		CollectionJSON PatientJSONParser=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		PatientJSONParser.debugfile=true;
		PatientJSONParser.procesaJSONFolder("files/ex1/Patient", log);
		CompleteCollection patientCollection = PatientJSONParser.getCollection();
		
		CollectionJSON DiagnosticReportJSONParser=new CollectionJSON();
		DiagnosticReportJSONParser.debugfile=true;
		DiagnosticReportJSONParser.procesaJSONFolder("files/ex1/DiagnosticReport", log);
		CompleteCollection DReportCollection = DiagnosticReportJSONParser.getCollection();
		
		CollectionJSON ConditionReportJSONParser=new CollectionJSON();
		ConditionReportJSONParser.debugfile=true;
		ConditionReportJSONParser.procesaJSONFolder("files/ex1/Condition", log);
		CompleteCollection CReportCollection = ConditionReportJSONParser.getCollection();
		
		CollectionJSON ImagingStudyReportJSONParser=new CollectionJSON();
		ImagingStudyReportJSONParser.debugfile=true;
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
