package fdi.ucm.server.importparser.fhir.linked;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


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
		
		CompleteCollection C=new CompleteCollection("ex1", "ex1");
		

		
		C.getMetamodelGrammar().addAll(patientCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(patientCollection.getEstructuras());
		C.getSectionValues().addAll(patientCollection.getSectionValues());
		
		C.getMetamodelGrammar().addAll(DReportCollection.getMetamodelGrammar());
		C.getEstructuras().addAll(DReportCollection.getEstructuras());
		C.getSectionValues().addAll(DReportCollection.getSectionValues());
		
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
