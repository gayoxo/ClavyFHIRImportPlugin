package fdi.ucm.server.importparser.fhir.linked;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionFHIRAgregated extends CollectionFHIRLinked{
	
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
	
		CollectionJSON ConditionSNOWMEDReportJSONParser=new CollectionJSON();
		ConditionSNOWMEDReportJSONParser.procesaJSONFolder("files/ex1/Snomed", log);
		ConditionSNOWMEDReportJSONParser.getCollection().setName("Snomed");
		nombre_parser.put(ConditionSNOWMEDReportJSONParser.getCollection().getName(), ConditionSNOWMEDReportJSONParser);
		ColeccionesUnion.add(ConditionSNOWMEDReportJSONParser);
		
		LimpiaSnomed(ConditionSNOWMEDReportJSONParser);
		
		CompleteCollection C=new CompleteCollection("ex1", "ex1");
		

		for (CollectionJSON collectionJSON : ColeccionesUnion) {
			C.getMetamodelGrammar().addAll(collectionJSON.getCollection().getMetamodelGrammar());
			C.getEstructuras().addAll(collectionJSON.getCollection().getEstructuras());
			C.getSectionValues().addAll(collectionJSON.getCollection().getSectionValues());
		}
		
	
		
		
		
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
			String GrammarAPp=Browse.getGrammar();
			CollectionJSON aplicar = nombre_parser.get(GrammarAPp);
			if (aplicar!=null &&
					(aplicar.getPathFinder().get(Browse.getPath())!=null))
				aplicar.getPathFinder().get(Browse.getPath()).setBrowseable(true);

			
		}
		
		
		
		HashMap<String,HashMap<String, CompleteDocuments>> listaElemDoc=new HashMap<String, HashMap<String,CompleteDocuments>>();
		
		for (CollectionJSON collectionJSON : ColeccionesUnion) {
			CompleteElementType valorclave = collectionJSON.getPathFinder().get("id");
			if (valorclave!=null&&valorclave instanceof CompleteTextElementType)
			{
				HashMap<String, CompleteDocuments> listaElemDocClave = new HashMap<String, CompleteDocuments>();
				
				for (CompleteDocuments document : collectionJSON.getCollection().getEstructuras()) {
					for (CompleteElement elem : document.getDescription()) {
						if (elem.getHastype()==valorclave && elem instanceof CompleteTextElement)
							{
							listaElemDocClave.put(((CompleteTextElement)elem).getValue(), document);
							break;
							}
					}
				}
				
				listaElemDoc.put(collectionJSON.getCollection().getName(), listaElemDocClave);
			}
		}
		
		
		
		for (ConfigFHIRYAMLlink linkedede : order.getLink()) {
			String GrammarAPp=linkedede.getGrammar();
			CollectionJSON aplicar = nombre_parser.get(GrammarAPp);
			if (aplicar!=null)
			{
				String PathSelect=linkedede.getPathOrigen();
				CompleteElementType PasarALink = aplicar.getPathFinder().get(PathSelect);
				if (PasarALink instanceof CompleteTextElementType)
					ConvierteEnLink((CompleteTextElementType)PasarALink,aplicar.getCollection().getEstructuras(),listaElemDoc
						);
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
		 
		 
		 
		 //TODO AHORA HARIA LA OTRA TRANSFORMACION.
	}



}
