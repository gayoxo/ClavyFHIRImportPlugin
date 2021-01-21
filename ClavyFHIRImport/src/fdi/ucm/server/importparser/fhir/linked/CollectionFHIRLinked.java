package fdi.ucm.server.importparser.fhir.linked;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.json.simple.JSONArray;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonPrimitive;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionFHIRLinked {
	
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
			if (aplicar!=null)
			{
				
			}

			
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
	}


	@SuppressWarnings("unchecked")
	private static void CompactarImagen(CollectionJSON imagingStudyReportJSONParser) {
		
		CompleteElementType entryinstanceentry = imagingStudyReportJSONParser.getPathFinder().get("series/entry");

			CompleteElementType PadreSeries = entryinstanceentry.getFather();

			List<CompleteElementType> ListaEntry=new LinkedList<CompleteElementType>();
			for (int i = 0; i < PadreSeries.getSons().size(); i++) {
				CompleteElementType completeElementType=PadreSeries.getSons().get(i);
				
				if (completeElementType.getClassOfIterator()!=null&&completeElementType.getClassOfIterator()==entryinstanceentry)
					ListaEntry.add(completeElementType);

				}

			CompleteElementType entryinstance_enty = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance");
			
			List<CompleteElementType> ListaEntryinstance=new LinkedList<CompleteElementType>();
			for (CompleteElementType entry_act : ListaEntry) {
				for (CompleteElementType entry_act_sons : entry_act.getSons()) {
					if (entry_act_sons.getClassOfIterator()==entryinstance_enty)
						ListaEntryinstance.add(entry_act_sons);
				}
			}
			
			
			CompleteElementType entryinstance = imagingStudyReportJSONParser.getPathFinder().get("series/entry/instance/entry");
			
			
			HashSet<CompleteElementType> ListaQuitar = new HashSet<CompleteElementType>();
			
			Stack<CompleteElementType> porProcesar= new Stack<CompleteElementType>();
			
			porProcesar.push(entryinstance);
			
			CompleteElementType actualElem = null;
			
			while (!porProcesar.isEmpty())
			{
				actualElem=porProcesar.pop();
				ListaQuitar.add(actualElem);
				for (CompleteElementType completeElementType : actualElem.getSons()) 
					porProcesar.add(completeElementType);
				
			}
			
			
			
			
			HashMap<CompleteElementType, CompleteElementType> equivalencia=new HashMap<CompleteElementType, CompleteElementType>();
			
			for (CompleteElementType entry_act : ListaEntryinstance) {
				List<CompleteElementType> ListaEntryinstanceentry=new LinkedList<CompleteElementType>();
				
				for (CompleteElementType entry_act_sons : entry_act.getSons()) {
					if (entry_act_sons.getClassOfIterator()==entryinstance)
						ListaEntryinstanceentry.add(entry_act_sons);
				}
				
				if (ListaEntryinstanceentry.size()>0)
				{
				CompleteElementType Representante = ListaEntryinstanceentry.get(0);
				for (CompleteElementType representado : ListaEntryinstance) {
					equivalencia.put(representado, Representante);
					if (representado!=Representante)
						representado.getFather().getSons().remove(representado);
				}
				}
				
			}

			

		
		List<CompleteDocuments> documentos = imagingStudyReportJSONParser.getCollection().getEstructuras();
		for (CompleteDocuments documento_uni : documentos) {
			HashMap<CompleteElementType, List<String>> TextoFinal=new HashMap<CompleteElementType, List<String>>();
			List<CompleteElement> ElementosRepresentados=new LinkedList<CompleteElement>();
			List<CompleteElement> aQuitar=new LinkedList<CompleteElement>();
			for (CompleteElement docu_eleme : documento_uni.getDescription()) {
				CompleteElementType equivalente = equivalencia.get(docu_eleme.getHastype());
				if (equivalente!=null)
				{
					List<String> Listvalor = TextoFinal.get(equivalente);
					if (Listvalor==null)
						Listvalor=new LinkedList<String>();
					
					if (docu_eleme instanceof CompleteTextElement)
						Listvalor.add(((CompleteTextElement) docu_eleme).getValue());
					
					TextoFinal.put(equivalente, Listvalor);
					
					if (docu_eleme.getHastype()==equivalente)
						ElementosRepresentados.add(docu_eleme);
					
				}
				
				if (ListaQuitar.contains(docu_eleme.getHastype().getClassOfIterator())
						&&docu_eleme.getHastype()!=docu_eleme.getHastype().getClassOfIterator())
					aQuitar.add(docu_eleme);
			}
			
			
			documento_uni.getDescription().removeAll(aQuitar);
			for (CompleteElement elementofinal : ElementosRepresentados) {
				List<String> ListaElemS = TextoFinal.get(elementofinal.getHastype());
				JSONArray JA=new JSONArray();
				for (String stre : ListaElemS) 
					JA.add(new JsonPrimitive(stre));
				
				if (elementofinal instanceof CompleteTextElement)
					((CompleteTextElement) elementofinal).setValue(JA.toJSONString());
			}
			
			
		}
		
	}


	private static void ConvierteEnLink(CompleteTextElementType pasarALink, List<CompleteDocuments> estructuras,
			HashMap<String,HashMap<String, CompleteDocuments>> coleccionesUnion) {
		CompleteLinkElementType NuevoElemento= new CompleteLinkElementType(pasarALink.getName(),pasarALink.getFather(),pasarALink.getCollectionFather());
		
		if (pasarALink.getFather()==null)
		{
			CompleteGrammar CG=NuevoElemento.getCollectionFather();
			int yo=-1;
			for (int i = 0; i < CG.getSons().size(); i++) 
				if (CG.getSons().get(i)==pasarALink)
					yo=i;
			
			
			if (yo>=0)
			{
				CG.getSons().remove(yo);
				CG.getSons().add(yo, NuevoElemento);
			}
			
		}
		else
		{
			int yo=-1;
			for (int i = 0; i < pasarALink.getFather().getSons().size(); i++) 
				if (pasarALink.getFather().getSons().get(i)==pasarALink)
					yo=i;
			
			
			if (yo>=0)
			{
				pasarALink.getFather().getSons().remove(yo);
				pasarALink.getFather().getSons().add(yo, NuevoElemento);
			}
		}
		
		
		
		
		for (CompleteDocuments completeDocuments : estructuras) { 
			
			List<CompleteElement> quitar=new LinkedList<CompleteElement>();
			List<CompleteElement> agregar=new LinkedList<CompleteElement>();
			
			for (CompleteElement elementoDoc : completeDocuments.getDescription())
				if (elementoDoc.getHastype()==pasarALink && elementoDoc instanceof CompleteTextElement)
					{
					
					String valueP=((CompleteTextElement)elementoDoc).getValue();
					
					String[] ListaSP = valueP.split("/");
					
					if (ListaSP.length==2)
					{
						String GramOrigen = ListaSP[0].trim();
						String IDDestino = ListaSP[1].trim();
						
						HashMap<String, CompleteDocuments> aquiGram = coleccionesUnion.get(GramOrigen);
						
						if (aquiGram != null)
						{
							CompleteDocuments DocumeDes = aquiGram.get(IDDestino);
							if (DocumeDes!=null)
								{
								CompleteLinkElement LL=new CompleteLinkElement(NuevoElemento, DocumeDes);
								quitar.add(elementoDoc);
								agregar.add(LL);
								}
						}
						
						

					}
					
					}
			
			completeDocuments.getDescription().removeAll(quitar);
			completeDocuments.getDescription().addAll(agregar);
		}
		
		
		
	}



}
