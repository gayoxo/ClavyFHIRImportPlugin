package fdi.ucm.server.importparser.fhir.cens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;

public class CollectionFHIR_CENS_TRANSFORM1 {

	public static CompleteCollection Apply(CompleteCollection c) {
		CollectionFHIR_CENS_TRANSFORM1 main=new CollectionFHIR_CENS_TRANSFORM1();
		System.out.println("Transformacion que convierte elementos en Links cuando detecta el link en forma gramatica/identificador");
		return main.apply(c);
	}

	private CompleteCollection apply(CompleteCollection c_input) {
		
	HashSet<CompleteElementType> Identifiers=new HashSet<CompleteElementType>();
	
	for (CompleteGrammar gramarElements : c_input.getMetamodelGrammar()) 
		for (CompleteElementType completeElementType : gramarElements.getSons()) 
			if (completeElementType.getName().toLowerCase().equals("id"))
					Identifiers.add(completeElementType);
		
	
	HashMap<String,CompleteDocuments> Identifierspath=new HashMap<String,CompleteDocuments>();
	
	for (CompleteElementType idenElem : Identifiers) 
		for (CompleteDocuments document : c_input.getEstructuras()) 
			for (CompleteElement element : document.getDescription())
				if (element.getHastype()==idenElem&&element instanceof CompleteTextElement)
					Identifierspath.put(idenElem.getCollectionFather().getNombre().trim()
							+"/"+
							((CompleteTextElement)element).getValue().trim(), document);
			
	HashMap<CompleteElementType, CompleteLinkElementType> LinkValid=new HashMap<CompleteElementType, CompleteLinkElementType>();
	
	for (CompleteDocuments document : c_input.getEstructuras()) 
	{
		
		LinkedList<CompleteLinkElement> NuevosLinks = new LinkedList<CompleteLinkElement>();
		LinkedList<CompleteElement> NuevosBorrarLinks = new LinkedList<CompleteElement>();
		for (CompleteElement element : document.getDescription())
			if (element instanceof CompleteTextElement)
				if (Identifierspath.get(((CompleteTextElement)element).getValue().trim())!=null)
					{
					if (LinkValid.get(element.getHastype())==null)
					{
						CompleteLinkElementType copia=new CompleteLinkElementType(element.getHastype().getName(),
								element.getHastype().getFather(), element.getHastype().getCollectionFather());
						
						copia.setMultivalued(element.getHastype().isMultivalued());
						copia.setBrowseable(element.getHastype().isBrowseable());
						copia.setSelectable(element.getHastype().isSelectable());
						copia.setBeFilter(element.getHastype().isBeFilter());
						
						copia.setShows(element.getHastype().getShows());
						copia.setSons(element.getHastype().getSons());
						
						for (CompleteElementType hijonuevo : copia.getSons())
							hijonuevo.setFather(copia);
						
						ArrayList<CompleteElementType> Nuevalista=new ArrayList<CompleteElementType>();
						boolean Found=false;
						
						if (element.getHastype().getFather()==null) {
							for(CompleteElementType hijosA: element.getHastype().getCollectionFather().getSons())
								if (hijosA==element.getHastype())
									{
									Nuevalista.add(copia);
									Found=true;
									}
								else
									Nuevalista.add(hijosA);
							
							
							if (!Found)
								Nuevalista.add(copia);
							
							element.getHastype().getCollectionFather().setSons(Nuevalista);
						}
						else
						{
							
							for(CompleteElementType hijosA: element.getHastype().getFather().getSons())
								if (hijosA==element.getHastype())
									{
									Nuevalista.add(copia);
									Found=true;
									}
								else
									Nuevalista.add(hijosA);
							
							
							if (!Found)
								Nuevalista.add(copia);
							
							element.getHastype().getFather().setSons(Nuevalista);
						}
							
							
						LinkValid.put(element.getHastype(), copia);
						
					}
					CompleteLinkElementType ValidoLink = LinkValid.get(element.getHastype());
					NuevosLinks.add(new CompleteLinkElement(ValidoLink,
							Identifierspath.get(((CompleteTextElement)element).getValue().trim())));
					NuevosBorrarLinks.add(element);
					
					}
		
		document.getDescription().addAll(NuevosLinks);
		document.getDescription().removeAll(NuevosBorrarLinks);
					
	}
	
	for (CompleteLinkElementType completeElementType : LinkValid.values()) 
		if (completeElementType.getClassOfIterator()!=null)
			completeElementType.setClassOfIterator(LinkValid.get(completeElementType.getClassOfIterator()));
	
		
	
	
//JsonElement JSONELEM=null;
//		
//		try {
//			JSONELEM = new JsonParser().parse(new FileReader("cens/bundle1.json"));
//		} catch (JsonIOException e1) {
//			System.err.println("Error IO");
//			e1.printStackTrace();
//		} catch (JsonSyntaxException e1) {
//			System.err.println("Error Syntax");
//			e1.printStackTrace();
//		} catch (FileNotFoundException e1) {
//			System.err.println("Error NotFound");
//			e1.printStackTrace();
//		}
//		
//		if (JSONELEM==null)
//			{
//			System.err.println("Error found exiting");
//			System.exit(1);
//			}
//		
//		
//		HashMap<String, List<JsonObject>> resource_values=new HashMap<String, List<JsonObject>>(8);
//		
//		
//		JsonArray DocuemntosProcesar = JSONELEM.getAsJsonObject().get("entry").getAsJsonArray();
//		
//		for (JsonElement jsonElement : DocuemntosProcesar) {
//			JsonObject objetoindividual= jsonElement.getAsJsonObject().get("resource").getAsJsonObject();
//			
//			String resourceType = objetoindividual.get("resourceType").getAsJsonPrimitive().getAsString();
//			
//			List<JsonObject> listatemp_ = resource_values.get(resourceType);
//			if (listatemp_==null)
//				listatemp_=new LinkedList<JsonObject>();
//			listatemp_.add(objetoindividual);
//			
//			resource_values.put(resourceType, listatemp_);
//
//		}
//		
//		List<CollectionJSON> ColeccionesUnion=new LinkedList<CollectionJSON>();
//		HashMap<String, CollectionJSON> nombre_parser=new HashMap<String, CollectionJSON>();
//		
//		for (Entry<String, List<JsonObject>> resoty_valu : resource_values.entrySet()) {
//			
//			JsonArray JS=new JsonArray(resoty_valu.getValue().size());
//			for (int i = 0; i < resoty_valu.getValue().size(); i++) 
//				JS.add(resoty_valu.getValue().get(i));
//			
//			
//			
//			CollectionJSON PatientJSONParser=new CollectionJSON();
//			ArrayList<String> log = new ArrayList<String>();
//
//			Properties prop=new Properties();
//			prop.put("desc", "id");
//			
//			PatientJSONParser.procesaJSONArrayFolder(JS, log, resoty_valu.getKey(), prop);
//			nombre_parser.put(PatientJSONParser.getCollection().getName(), PatientJSONParser);
//			ColeccionesUnion.add(PatientJSONParser);
//		}
//		
//		CompleteCollection C=new CompleteCollection("bundle1", "bundle1");
//		
//
//		for (CollectionJSON collectionJSON : ColeccionesUnion) {
//			C.getMetamodelGrammar().addAll(collectionJSON.getCollection().getMetamodelGrammar());
//			C.getEstructuras().addAll(collectionJSON.getCollection().getEstructuras());
//			C.getSectionValues().addAll(collectionJSON.getCollection().getSectionValues());
//		}
		
		return c_input;
	}

}
