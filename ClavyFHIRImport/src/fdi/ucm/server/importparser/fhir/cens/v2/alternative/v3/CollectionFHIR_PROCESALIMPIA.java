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
import java.util.Stack;

import javax.crypto.spec.GCMParameterSpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteFile;
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
			
			
			//Aqui cargo los datos basicos de la coleccion Resultado
			
			String g_nuevac = (String) obj.get("grammarname");
			String g_nuevac_des = (String) obj.get("description");
			String unionid = (String) obj.get("unionid");
			
		    CompleteElementType ID=null;
		    ID =find(c.getMetamodelGrammar(),unionid);
		    

			
			HashMap<CompleteDocuments, CompleteDocuments> listaCuadra=new HashMap<CompleteDocuments, CompleteDocuments>();


			//Aqui Creo las estrucuras base que pueden ser una o varias. Luego las tenia que crear igual con lo que adelanto.
			
			for (CompleteDocuments docucrea : c.getEstructuras()) {
				for (CompleteElement docuelem : docucrea.getDescription()) {
					if (docuelem.getHastype()==ID)
					{
						CompleteDocuments crear=new CompleteDocuments(nuevac, docucrea.getDescriptionText(), docucrea.getIcon());
						
						listaCuadra.put(docucrea, crear);
					}
				}
			}
			
			
			//Aqui proceso los archivos, si los hay
			nuevac.getSectionValues().addAll(c.getSectionValues());
			
			
			
			//En minuscula
			FileReader readerFor = new FileReader(new File("src/fdi/ucm/server/importparser/fhir/cens/v2/alternative/v3/FasePersonal-1.0-FOR.json"));
			
			
			JSONArray objFor = (JSONArray) jsonParser.parse(readerFor);
			
			Set<String> listaFor=new HashSet<String>();
			
			listaFor.addAll(Arrays.asList(Arrays.asList(objFor.toArray()).toArray(new String[objFor.toArray().length])));
			
			
			//En minuscula
			FileReader readerTo = new FileReader(new File("src/fdi/ucm/server/importparser/fhir/cens/v2/alternative/v3/FasePersonal-1.0-To.json"));
			
		
			JSONArray objTo = (JSONArray) jsonParser.parse(readerTo);
			
			Set<String> listaTo=new HashSet<String>();
			
			listaTo.addAll(Arrays.asList(Arrays.asList(objTo.toArray()).toArray(new String[objTo.toArray().length])));
			
			
			//Ahora hay que recorrer para cadauno de manera descendiente.
			
			HashMap<CompleteDocuments, Set<CompleteDocuments>> listaCuadraDoble= new HashMap<CompleteDocuments, Set<CompleteDocuments>>();

			
	
			for (CompleteDocuments actualDoc : listaCuadra.keySet()) {
				
			
			//Para procesar todos los documentos tenemos la de pendientes y procesados.
				Queue<CompleteDocuments> Pendientes=new LinkedList<CompleteDocuments>();
			
			Pendientes.add(actualDoc);
			
			HashSet<CompleteDocuments> ListaIDsParse=new HashSet<CompleteDocuments>();
			
			ListaIDsParse.add(actualDoc);
			
			
			
			while (!Pendientes.isEmpty())
			{
				
				
				CompleteDocuments DocumentoActual = Pendientes.poll();
				
				for (CompleteElement elementoARevisar : DocumentoActual.getDescription()) {
					if ( elementoARevisar instanceof CompleteLinkElement &&
							listaFor.contains(elementoARevisar.getHastype().getCollectionFather().getNombre().toLowerCase()))
					{
						
						//Si no esta Lo añado al proceso.
						CompleteDocuments DocumentoActualLinked=((CompleteLinkElement)elementoARevisar).getValue();
						
						if (!ListaIDsParse.contains(DocumentoActualLinked))
						{	
						ListaIDsParse.add(DocumentoActualLinked);
						Pendientes.add(DocumentoActualLinked);
						}
						
					}
				}

				
				for (CompleteDocuments docuExplore : c.getEstructuras()) {
					for (CompleteElement elexplore : docuExplore.getDescription()) {
						if ( elexplore instanceof CompleteLinkElement &&
								listaTo.contains(elexplore.getHastype().getCollectionFather().getNombre().toLowerCase()))
						{
							
							//Si no esta Lo añado al proceso.
							CompleteDocuments DocumentoActualLinked=((CompleteLinkElement)elexplore).getValue();
							
							if (ListaIDsParse.contains(DocumentoActualLinked)
									&&
									!ListaIDsParse.contains(docuExplore))
							{	
							ListaIDsParse.add(docuExplore);
							Pendientes.add(docuExplore);
							}
							
						}
					}
				}
				
			
			

			
			}
			
			ListaIDsParse.remove(actualDoc);
			
			listaCuadraDoble.put(actualDoc, new HashSet<CompleteDocuments>(ListaIDsParse));
			
			}
			
			
			
			
			//genero la nueva gramatica.
			
			CompleteGrammar CGFinal=new CompleteGrammar(g_nuevac,g_nuevac_des,nuevac);
			
			nuevac.getMetamodelGrammar().add(CGFinal);
				
			JSONArray g_nuevac_hijos = (JSONArray) obj.get("structure");
			
			
			HashMap<CompleteGrammar, Integer> calculadora=new HashMap<CompleteGrammar, Integer>();
			
			
			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDoble.entrySet())  {
				
				Set<CompleteDocuments> asociados=entryreverse.getValue();
				for (CompleteGrammar gramaticaaContar : c.getMetamodelGrammar()) {
					int i=0;
					for (CompleteDocuments docu : asociados) {
						for (CompleteElement elemento : docu.getDescription()) {
							if ((!(elemento instanceof CompleteLinkElement))&&
									elemento.getHastype().getCollectionFather()==gramaticaaContar)
								{
								i++;
								break;
								}
						}
					}
					
					if (calculadora.get(gramaticaaContar)==null||calculadora.get(gramaticaaContar)<i)
						calculadora.put(gramaticaaContar, new Integer(i));
					
				}
				
				
			}
			
			System.out.println("Calculos de Gramatica");
			//calculos
			for (Entry<CompleteGrammar, Integer> eleme : calculadora.entrySet()) {
				System.out.println("->"+eleme.getKey().getNombre()+"=="+eleme.getValue());
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
			
			
			
			System.out.println("Lista Normal sobre A");
			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDoble.entrySet())  {
				System.out.println("->"+entryreverse.getKey().getDescriptionText());
				for (CompleteDocuments object : entryreverse.getValue()) {
					System.out.println("------->"+object.getDescriptionText());
				}
			}
			
			
			System.out.println("Lista Reverse sobre A");
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
