/**
 * 
 */
package fdi.ucm.server.importparser.fhir.cens.v2.alternative.v3;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.crypto.spec.GCMParameterSpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;

/**
 * @author jgayoso
 *
 */
public class CollectionFHIR_PROCESALIMPIA {

	public static CompleteCollection Apply(CompleteCollection c) {
		JSONParser jsonParser = new JSONParser();
		
		try {
			FileReader reader = new FileReader(new File("src/fdi/ucm/server/importparser/fhir/cens/v2/alternative/v3/FasePersonal-1.0.json"));
			
		
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			
			
			CompleteCollection nuevac=new CompleteCollection(c.getName(), c.getDescription());
			
			String g_nuevac = (String) obj.get("grammarname");
			String g_nuevac_des = (String) obj.get("description");
			String unionid = (String) obj.get("unionid");
			
		    CompleteElementType ID=null;
		    ID =find(c.getMetamodelGrammar(),unionid);
		    
			
			CompleteGrammar CGFinal=new CompleteGrammar(g_nuevac,g_nuevac_des,nuevac);
			
			nuevac.getMetamodelGrammar().add(CGFinal);
				
			JSONArray g_nuevac_hijos = (JSONArray) obj.get("structure");
			
			HashMap<CompleteDocuments, CompleteDocuments> listaCuadra=new HashMap<CompleteDocuments, CompleteDocuments>();
			HashMap<CompleteDocuments, Set<CompleteDocuments>> listaCuadraDoble=new HashMap<CompleteDocuments, Set<CompleteDocuments>>();
			
			for (CompleteDocuments docucrea : c.getEstructuras()) {
				for (CompleteElement docuelem : docucrea.getDescription()) {
					if (docuelem.getHastype()==ID)
					{
						CompleteDocuments crear=new CompleteDocuments(nuevac, docucrea.getDescriptionText(), docucrea.getIcon());
						
						listaCuadra.put(docucrea, crear);
					}
				}
			}
			
			
			CompleteGrammar GramaticainicialCierre = ID.getCollectionFather();
			
			
			
			for (CompleteDocuments actualDoc : listaCuadra.keySet()) {
				
			
			
			Set<CompleteGrammar> Pendientes=new HashSet<CompleteGrammar>(c.getMetamodelGrammar());
			
			Pendientes.remove(GramaticainicialCierre);
			
			HashSet<CompleteDocuments> ListaIDsParse=new HashSet<CompleteDocuments>();
			
			ListaIDsParse.add(actualDoc);
			
			boolean continua = true;
			
			while (continua)
			{
				continua=false;
			List<CompleteLinkElement> nuevosLinksRev=new LinkedList<CompleteLinkElement>();
			List<CompleteLinkElement> nuevosLinksFor=new LinkedList<CompleteLinkElement>();

			
			
			
			
			for (CompleteDocuments docuExplore : c.getEstructuras()) {
				for (CompleteElement elexplore : docuExplore.getDescription()) {
					if (elexplore instanceof CompleteLinkElement)
					{
						if (Pendientes.contains(elexplore.getHastype().getCollectionFather())&&
								ListaIDsParse.contains(((CompleteLinkElement)elexplore).getValue()))
										{
										nuevosLinksRev.add((CompleteLinkElement) elexplore);
										//minireparacion por si acaso
										if (elexplore.getDocumentsFather()==null)
											elexplore.setDocumentsFather(docuExplore);
										}
						
						if (ListaIDsParse.contains(docuExplore)&&
								!elexplore.getHastype().getCollectionFather().getNombre().toLowerCase().equals("snomed")&&
								Pendientes.contains(elexplore.getHastype().getCollectionFather()))
							nuevosLinksFor.add((CompleteLinkElement) elexplore);
						
					}
				}
			}
			
			
			
			
			
			
			for (CompleteLinkElement linkvalidos : nuevosLinksFor) {
				
				if (linkvalidos.getValue()!=null)
					{
					ListaIDsParse.add(linkvalidos.getValue());
					if (!linkvalidos.getHastype().getCollectionFather().getNombre().toLowerCase().equals("snomed"))
						if (Pendientes.remove(linkvalidos.getHastype().getCollectionFather()))
							continua = true;
					}
			}
			
			for (CompleteLinkElement linkvalidos : nuevosLinksRev) {
				
				if (linkvalidos.getDocumentsFather()!=null)
					{
					ListaIDsParse.add(linkvalidos.getDocumentsFather());
					if (!linkvalidos.getHastype().getCollectionFather().getNombre().toLowerCase().equals("snomed"))
						Pendientes.remove(linkvalidos.getHastype().getCollectionFather());
						continua = true;
					}
			}
			

			
			}
			
			ListaIDsParse.remove(actualDoc);
			
			listaCuadraDoble.put(actualDoc, new HashSet<CompleteDocuments>(ListaIDsParse));
			
			}
			
			
			HashMap<CompleteDocuments, Set<CompleteDocuments>> listaCuadraDobleRever=new HashMap<CompleteDocuments, Set<CompleteDocuments>>();
			
			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDoble.entrySet()) {
				for (CompleteDocuments docenuevakey : entryreverse.getValue()) {
					Set<CompleteDocuments> listatemp=listaCuadraDobleRever.get(docenuevakey);
					if (listatemp==null)
						listatemp=new HashSet<CompleteDocuments>();
					listatemp.add(entryreverse.getKey());
					listaCuadraDobleRever.put(docenuevakey, listatemp);
				}
			}
			
			
			
			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDobleRever.entrySet())  {
				System.out.println("->"+entryreverse.getKey().getDescriptionText());
				for (CompleteDocuments object : entryreverse.getValue()) {
					System.out.println("------->"+object.getDescriptionText());
				}
			}
			
			return nuevac;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		return c;
	}

	private static CompleteElementType find(List<CompleteGrammar> metamodelGrammar, String unionid) {
		String[] splited = unionid.split("/");
		Queue<String> queue = new LinkedList<>(Arrays.asList(splited));
		String grammar = queue.poll();
		for (CompleteGrammar completeGrammar : metamodelGrammar) {
			if (grammar.equals(completeGrammar.getNombre()))
				return find(completeGrammar.getSons(),(Queue<String>) new LinkedList<String>(queue));
		}
		return null;
	}



	private static CompleteElementType find(List<CompleteElementType> sons, Queue<String> queue) {
		String elem=queue.poll();
		for (CompleteElementType celemEva : sons) {
			if (celemEva.getName().equals(elem))
				if (queue.isEmpty())
					return celemEva;
				else
					{ 
					CompleteElementType salida=find(celemEva.getSons(), new LinkedList<String>(queue));
							if (salida!=null)
								return salida;
					}
			
		}
		return null;
	}

//	private static void procesaHijos(JSONArray g_nuevac_hijos, CompleteGrammar cGFinal, CompleteCollection c,
//			HashMap<Long, CompleteDocuments> listaCuadra, HashMap<Long, List<Long>> haciaDelanteReverse) {
//		for (int i = 0; i < g_nuevac_hijos.size(); i++) {
//			JSONObject array_element = (JSONObject) g_nuevac_hijos.get(i);
//			
//			
//			String name = (String) array_element.get("name");
//			String equi=null;
//			if (array_element.get("eq")!=null)
//				equi = (String) array_element.get("eq");
//			String multitype = null;
//			if (array_element.get("multitype")!=null)
//				multitype = (String) array_element.get("multitype");
//			
//			if (equi==null)
//				cGFinal.getSons().add(new CompleteElementType(name,cGFinal));
//			else
//			{
//				CompleteElementType equiElem=null;
//				equiElem =find(c.getMetamodelGrammar(),equi);
//				if (equiElem!=null)
//				{
//				HashMap<Long, List<String>> document_values=colectValues(c,equiElem,listaCuadra,haciaDelanteReverse);
//				}
//			}
//			
//			
//			
//			
//		}
//		
//	}

//	private static HashMap<Long, List<String>> colectValues(CompleteCollection c, CompleteElementType equiElem,
//			HashMap<Long, CompleteDocuments> listaCuadra, HashMap<Long, List<Long>> haciaDelanteReverse) {
//		HashMap<Long, List<String>> Salida=new HashMap<Long, List<String>>();
//		
//		for (CompleteDocuments cd : c.getEstructuras()) {
//			for (CompleteElement daleElemento : cd.getDescription()) {
//				
//				if (daleElemento.getHastype()==equiElem)
//				{
//					if (listaCuadra.containsKey(cd.getClavilenoid()))
//						{
//						Long id_docAdd=cd.getClavilenoid();
//						List<String> minima=Salida.get(id_docAdd);
//						if (minima==null)
//							minima=new LinkedList<String>();
//						if (daleElemento instanceof CompleteTextElement)
//							minima.add(((CompleteTextElement) daleElemento).getValue());
//						
//						Salida.put(id_docAdd, minima);
//						
//						}
//					else
//						{
//						
//						List<Long> Enlazados=haciaDelanteReverse.get(cd.getClavilenoid());
//
//						for (iterable_type iterable_element : iterable) {
//							
//						}
//						
//						}
//					
//				}
//				
//			}
//		}
//		
//		return Salida;
//	}



}
