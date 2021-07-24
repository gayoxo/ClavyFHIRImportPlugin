package fdi.ucm.server.importparser.fhir.cens;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;

public class CollectionFHIR_CENS_EXTRACT {

	public static CompleteCollection Apply() {
		CollectionFHIR_CENS_EXTRACT main=new CollectionFHIR_CENS_EXTRACT();
		System.out.println("Extractor de la informacion");
		return main.apply();
	}

	private CompleteCollection apply() {
JsonElement JSONELEM=null;
		
		try {
			JSONELEM = new JsonParser().parse(new FileReader("cens/bundle1.json"));
		} catch (JsonIOException e1) {
			System.err.println("Error IO");
			e1.printStackTrace();
		} catch (JsonSyntaxException e1) {
			System.err.println("Error Syntax");
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			System.err.println("Error NotFound");
			e1.printStackTrace();
		}
		
		if (JSONELEM==null)
			{
			System.err.println("Error found exiting");
			System.exit(1);
			}
		
		
		HashMap<String, List<JsonObject>> resource_values=new HashMap<String, List<JsonObject>>(8);
		
		
		JsonArray DocuemntosProcesar = JSONELEM.getAsJsonObject().get("entry").getAsJsonArray();
		
		for (JsonElement jsonElement : DocuemntosProcesar) {
			JsonObject objetoindividual= jsonElement.getAsJsonObject().get("resource").getAsJsonObject();
			
			String resourceType = objetoindividual.get("resourceType").getAsJsonPrimitive().getAsString();
			
			List<JsonObject> listatemp_ = resource_values.get(resourceType);
			if (listatemp_==null)
				listatemp_=new LinkedList<JsonObject>();
			listatemp_.add(objetoindividual);
			
			resource_values.put(resourceType, listatemp_);

		}
		
		List<CollectionJSON> ColeccionesUnion=new LinkedList<CollectionJSON>();
		HashMap<String, CollectionJSON> nombre_parser=new HashMap<String, CollectionJSON>();
		
		for (Entry<String, List<JsonObject>> resoty_valu : resource_values.entrySet()) {
			
			JsonArray JS=new JsonArray(resoty_valu.getValue().size());
			for (int i = 0; i < resoty_valu.getValue().size(); i++) 
				JS.add(resoty_valu.getValue().get(i));
			
			
			
			CollectionJSON PatientJSONParser=new CollectionJSON();
			ArrayList<String> log = new ArrayList<String>();

			Properties prop=new Properties();
			prop.put("desc", "id");
			
			PatientJSONParser.procesaJSONArrayFolder(JS, log, resoty_valu.getKey(), prop);
			nombre_parser.put(PatientJSONParser.getCollection().getName(), PatientJSONParser);
			ColeccionesUnion.add(PatientJSONParser);
		}
		
		CompleteCollection C=new CompleteCollection("bundle1", "bundle1");
		

		for (CollectionJSON collectionJSON : ColeccionesUnion) {
			C.getMetamodelGrammar().addAll(collectionJSON.getCollection().getMetamodelGrammar());
			C.getEstructuras().addAll(collectionJSON.getCollection().getEstructuras());
			C.getSectionValues().addAll(collectionJSON.getCollection().getSectionValues());
		}
		
		return C;
	}

}
