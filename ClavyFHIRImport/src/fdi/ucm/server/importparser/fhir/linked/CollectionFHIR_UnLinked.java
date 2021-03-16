package fdi.ucm.server.importparser.fhir.linked;


import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionFHIR_UnLinked {
	
	public boolean debugfile=false;

	
	public static void main(String[] args) {
		
		HashMap<String, CollectionJSON> nombre_parser=new HashMap<String, CollectionJSON>();
		List<CollectionJSON> ColeccionesUnion=new LinkedList<CollectionJSON>();
		
		CollectionJSON PatientJSONParser=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		PatientJSONParser.procesaJSONFolder("files/ex1/Patient", log);
		PatientJSONParser.getCollection().setName("Patient");
		nombre_parser.put(PatientJSONParser.getCollection().getName(), PatientJSONParser);
		ColeccionesUnion.add(PatientJSONParser);
		
		CollectionJSON DiagnosticReportJSONParser=new CollectionJSON();
		DiagnosticReportJSONParser.procesaJSONFolder("files/ex1/DiagnosticReport", log);
		DiagnosticReportJSONParser.getCollection().setName("DiagnosticReport");
		nombre_parser.put(DiagnosticReportJSONParser.getCollection().getName(), DiagnosticReportJSONParser);
		ColeccionesUnion.add(DiagnosticReportJSONParser);

		
		CollectionJSON ConditionReportJSONParser=new CollectionJSON();
		ConditionReportJSONParser.procesaJSONFolder("files/ex1/Condition", log);
		ConditionReportJSONParser.getCollection().setName("Condition");
		nombre_parser.put(ConditionReportJSONParser.getCollection().getName(), ConditionReportJSONParser);
		ColeccionesUnion.add(ConditionReportJSONParser);
		
		
		CollectionJSON ImagingStudyReportJSONParser=new CollectionJSON();
		ImagingStudyReportJSONParser.procesaJSONFolder("files/ex1/ImagingStudy", log);
		ImagingStudyReportJSONParser.getCollection().setName("ImagingStudy");
		nombre_parser.put(ImagingStudyReportJSONParser.getCollection().getName(), ImagingStudyReportJSONParser);
		ColeccionesUnion.add(ImagingStudyReportJSONParser);

		CompactarImagen(ImagingStudyReportJSONParser);
	

		CompleteCollection C=new CompleteCollection("ex1", "ex1");
		

		for (CollectionJSON collectionJSON : ColeccionesUnion) {
			C.getMetamodelGrammar().addAll(collectionJSON.getCollection().getMetamodelGrammar());
			C.getEstructuras().addAll(collectionJSON.getCollection().getEstructuras());
			C.getSectionValues().addAll(collectionJSON.getCollection().getSectionValues());
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



	@SuppressWarnings("unchecked")
	protected static void CompactarImagen(CollectionJSON imagingStudyReportJSONParser) {
		
		CompleteElementType entryinstanceentry = imagingStudyReportJSONParser.getPathFinder().get("series/entry");

			CompleteElementType PadreSeries = entryinstanceentry.getFather();

			List<CompleteElementType> ListaEntrySeries=new LinkedList<CompleteElementType>();
			for (int i = 0; i < PadreSeries.getSons().size(); i++) {
				CompleteElementType completeElementType=PadreSeries.getSons().get(i);
				
				if (completeElementType.getClassOfIterator()!=null&&completeElementType.getClassOfIterator()==entryinstanceentry)
					ListaEntrySeries.add(completeElementType);

				}

			CompleteElementType entryinstance_enty = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance");
			
			List<CompleteElementType> ListaEntryinstance=new LinkedList<CompleteElementType>();
			HashMap<CompleteElementType,CompleteElementType> instancia_serie=new HashMap<CompleteElementType, CompleteElementType>();
			
			for (CompleteElementType act_serie_pro : ListaEntrySeries) {
				for (CompleteElementType entry_act_sons : act_serie_pro.getSons()) {
					if (entry_act_sons.getClassOfIterator()==entryinstance_enty)
						{
						ListaEntryinstance.add(entry_act_sons);
						instancia_serie.put(entry_act_sons, act_serie_pro);
						}
				}
			}
			
			CompleteElementType entryinstance_enty_entry = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance/entry");
		
			Stack<CompleteElementType> porProcesar= new Stack<CompleteElementType>();
			HashSet<CompleteElementType> ListaQuitar = new HashSet<CompleteElementType>();
			HashMap<CompleteElementType,CompleteElementType> entry_serie =new HashMap<CompleteElementType, CompleteElementType>();
			
			for (CompleteElementType instancia : ListaEntryinstance) {
				porProcesar.add(instancia);
				instancia.getFather().getSons().remove(instancia);
				for (CompleteElementType completeElementType : instancia.getSons()) {
					if (completeElementType.getClassOfIterator()==entryinstance_enty_entry)
						entry_serie.put(completeElementType, instancia_serie.get(instancia));
				}
			}

			CompleteElementType actualElem = null;
	
			while (!porProcesar.isEmpty())
			{
				actualElem=porProcesar.pop();
				ListaQuitar.add(actualElem);
				for (CompleteElementType completeElementType : actualElem.getSons()) 
					porProcesar.add(completeElementType);
				
			}
			
			//AQUI ES EL PUNTO CLAVE

			
			CompleteElementType entryinstance_uid = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance/entry/uid");
			CompleteElementType entryinstance_number = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance/entry/number");
			CompleteElementType entryinstance_sopClass = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance/entry/sopClass");

			HashMap<CompleteElementType,CompleteTextElementType> serie_new_entry =new HashMap<CompleteElementType, CompleteTextElementType>();
			
			for (CompleteElementType completeElementType : ListaEntrySeries) {
				CompleteTextElementType nuevo=new CompleteTextElementType("compacted_instance", completeElementType, completeElementType.getCollectionFather());
				completeElementType.getSons().add(nuevo);
				serie_new_entry.put(completeElementType, nuevo);
			}

		
		List<CompleteDocuments> documentos = imagingStudyReportJSONParser.getCollection().getEstructuras();
		
		for (CompleteDocuments documento_uni : documentos) {
			
			HashMap<CompleteElementType, JSONObject> entry_JSONObject = new HashMap<CompleteElementType, JSONObject>();
			HashMap<CompleteElementType, JSONArray> serie_JSONArray = new HashMap<CompleteElementType, JSONArray>();

			
			List<CompleteElement> aQuitar=new LinkedList<CompleteElement>();
			for (CompleteElement docu_eleme : documento_uni.getDescription()) {

				if (docu_eleme.getHastype().getClassOfIterator()==entryinstance_uid&&
						docu_eleme instanceof CompleteTextElement)
				{
					CompleteElementType entryFather = docu_eleme.getHastype().getFather();
					CompleteElementType entryserie = entry_serie.get(entryFather);
					JSONObject JSONObject_entry = entry_JSONObject.get(entryFather);
					JSONArray JSONArray_serie = serie_JSONArray.get(entryserie);
					
					if (JSONObject_entry==null)
						{
						JSONObject_entry= new JSONObject();
						
						if (JSONArray_serie == null)
							{
							JSONArray_serie = new JSONArray();
							serie_JSONArray.put(entryserie, JSONArray_serie);
							}
						
						JSONArray_serie.add(JSONObject_entry);
						
						entry_JSONObject.put(entryFather, JSONObject_entry);
						}
					
					try {
						JSONObject_entry.put("uid",((CompleteTextElement)docu_eleme).getValue() );
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					
				}
				
				
				if (docu_eleme.getHastype().getClassOfIterator()==entryinstance_number&&
						docu_eleme instanceof CompleteTextElement)
				{
					CompleteElementType entryFather = docu_eleme.getHastype().getFather();
					CompleteElementType entryserie = entry_serie.get(entryFather);
					JSONObject JSONObject_entry = entry_JSONObject.get(entryFather);
					JSONArray JSONArray_serie = serie_JSONArray.get(entryserie);
					
					if (JSONObject_entry==null)
						{
						JSONObject_entry= new JSONObject();
						
						if (JSONArray_serie == null)
							{
							JSONArray_serie = new JSONArray();
							serie_JSONArray.put(entryserie, JSONArray_serie);
							}
						
						JSONArray_serie.add(JSONObject_entry);
						
						entry_JSONObject.put(entryFather, JSONObject_entry);
						}
					
					try {
						JSONObject_entry.put("number",((CompleteTextElement)docu_eleme).getValue() );
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					
				}
				
				if (docu_eleme.getHastype().getClassOfIterator()==entryinstance_sopClass&&
						docu_eleme instanceof CompleteTextElement)
				{
					CompleteElementType entryFather = docu_eleme.getHastype().getFather();
					CompleteElementType entryserie = entry_serie.get(entryFather);
					JSONObject JSONObject_entry = entry_JSONObject.get(entryFather);
					JSONArray JSONArray_serie = serie_JSONArray.get(entryserie);
					
					if (JSONObject_entry==null)
						{
						JSONObject_entry= new JSONObject();
						
						if (JSONArray_serie == null)
							{
							JSONArray_serie = new JSONArray();
							serie_JSONArray.put(entryserie, JSONArray_serie);
							}
						
						JSONArray_serie.add(JSONObject_entry);
						
						entry_JSONObject.put(entryFather, JSONObject_entry);
						}
					
					try {
						JSONObject_entry.put("sopClass",((CompleteTextElement)docu_eleme).getValue() );
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					
				}
				
				
				
				
				
				if (ListaQuitar.contains(docu_eleme.getHastype()))
					aQuitar.add(docu_eleme);
			}
			
			for (Entry<CompleteElementType, JSONArray> serie : serie_JSONArray.entrySet()) {
				CompleteTextElementType tipovalido = serie_new_entry.get(serie.getKey());
				
				
				CompleteTextElement nuevo=new CompleteTextElement(tipovalido, serie.getValue().toJSONString());
				documento_uni.getDescription().add(nuevo);
			}
			
			
			documento_uni.getDescription().removeAll(aQuitar);
		
			
			
		}
		
	}




}
