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
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

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
		    ID =find(c.getMetamodelGrammar(),unionid,null);
		    

			
			HashMap<CompleteDocuments, CompleteDocuments> listaCuadra=new HashMap<CompleteDocuments, CompleteDocuments>();


			//Aqui Creo las estrucuras base que pueden ser una o varias. Luego las tenia que crear igual con lo que adelanto.
			
			for (CompleteDocuments docucrea : c.getEstructuras()) {
				for (CompleteElement docuelem : docucrea.getDescription()) {
					if (docuelem.getHastype()==ID)
					{
						CompleteDocuments crear=new CompleteDocuments(nuevac, docucrea.getDescriptionText(), docucrea.getIcon());
						
						listaCuadra.put(docucrea, crear);
						nuevac.getEstructuras().add(crear);
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
			
			HashMap<CompleteGrammar,List<CompleteElementType>> calculadoraFinal=
					new HashMap<CompleteGrammar, List<CompleteElementType>>();
			
			HashMap<CompleteElementType,CompleteElementType> TablaCruceElemtos=
					new HashMap<CompleteElementType,CompleteElementType>();
			
			for (int i = 0; i < g_nuevac_hijos.size(); i++) {
				JSONObject Objetolista = (JSONObject) g_nuevac_hijos.get(i);
				procesaElementoDescendant(CGFinal,null,null,calculadora,Objetolista,
						c.getMetamodelGrammar(),calculadoraFinal,TablaCruceElemtos);
				
			}
			
			
			for (Entry<CompleteDocuments, CompleteDocuments> elemento : listaCuadra.entrySet()) {
				CompleteDocuments original = elemento.getKey();
				CompleteDocuments nuevo = elemento.getValue();
				
				for (CompleteElement cetetet : original.getDescription()) {
					if (cetetet instanceof CompleteTextElement)
						if (cetetet.getHastype() instanceof CompleteTextElementType &&
								TablaCruceElemtos.get(cetetet.getHastype())!=null)
						{
							CompleteTextElement nuevoelementoAgrega=new CompleteTextElement(
									(CompleteTextElementType) TablaCruceElemtos.get(cetetet.getHastype()),
									((CompleteTextElement) cetetet).getValue());
							nuevo.getDescription().add(nuevoelementoAgrega);
						}else {
							
						}
						else
							if (cetetet.getHastype() instanceof CompleteResourceElementType)
								if (TablaCruceElemtos.get(cetetet.getHastype())!=null)
								{
									CompleteElement nuevoelementoAgrega;
									
									if (cetetet instanceof CompleteResourceElementFile)
										nuevoelementoAgrega=new CompleteResourceElementFile((CompleteResourceElementType)TablaCruceElemtos.get(cetetet.getHastype()),
												((CompleteResourceElementFile) cetetet).getValue());
									else
										nuevoelementoAgrega=new CompleteResourceElementURL((CompleteResourceElementType)TablaCruceElemtos.get(cetetet.getHastype()),
												((CompleteResourceElementURL) cetetet).getValue());
									nuevo.getDescription().add(nuevoelementoAgrega);
								}
				
					
				}
				
				Set<CompleteDocuments> listaMete = listaCuadraDoble.get(original);
				
				for (CompleteDocuments cdocumentoi : listaMete) {
					for (CompleteElement cetetet : cdocumentoi.getDescription()) {
						if (cetetet instanceof CompleteTextElement)
							if (cetetet.getHastype() instanceof CompleteTextElementType &&
									TablaCruceElemtos.get(cetetet.getHastype())!=null)
							{
								CompleteTextElement nuevoelementoAgrega=new CompleteTextElement(
										(CompleteTextElementType) TablaCruceElemtos.get(cetetet.getHastype()),
										((CompleteTextElement) cetetet).getValue());
								nuevo.getDescription().add(nuevoelementoAgrega);
							}else {
								
							}
							else
								if (cetetet.getHastype() instanceof CompleteResourceElementType)
									if (TablaCruceElemtos.get(cetetet.getHastype())!=null)
									{
										CompleteElement nuevoelementoAgrega;
										
										if (cetetet instanceof CompleteResourceElementFile)
											nuevoelementoAgrega=new CompleteResourceElementFile((CompleteResourceElementType)TablaCruceElemtos.get(cetetet.getHastype()),
													((CompleteResourceElementFile) cetetet).getValue());
										else
											nuevoelementoAgrega=new CompleteResourceElementURL((CompleteResourceElementType)TablaCruceElemtos.get(cetetet.getHastype()),
													((CompleteResourceElementURL) cetetet).getValue());
										nuevo.getDescription().add(nuevoelementoAgrega);
									}
					
						
					}
				}
				
			}
			
			
			System.out.println("Print Estructura");
			System.out.println("->"+CGFinal.getNombre());
			
			printGrammar(CGFinal.getSons(),"->");
			
			System.out.println("Print Copias");
			for (Entry<CompleteElementType,CompleteElementType> elemenTableKeyval : TablaCruceElemtos.entrySet()) {
				System.out.println("--"+elemenTableKeyval.getKey().getName()+"=="+elemenTableKeyval.getValue().getName());
			}
			
			System.out.println("Print Documentos");
			for (Entry<CompleteDocuments, CompleteDocuments> elemento : listaCuadra.entrySet())
				{
				System.out.println("--S "+elemento.getValue().getDescriptionText());
				for (CompleteElement cetetet : elemento.getValue().getDescription()) {
					if (cetetet instanceof CompleteTextElement)
						System.out.println("------S "+cetetet.getHastype().getName() +" ===" +((CompleteTextElement) cetetet).getValue());
					if (cetetet instanceof CompleteResourceElementURL)
						System.out.println("------S "+cetetet.getHastype().getName() +" ===" +((CompleteResourceElementURL) cetetet).getValue());
					if (cetetet instanceof CompleteResourceElementFile)
						System.out.println("------S "+cetetet.getHastype().getName() +" ===" +((CompleteResourceElementFile) cetetet).getValue().getPath());
				}
				}
			
			
			System.out.println("Calculos de Gramatica");
			//calculos
			for (Entry<CompleteGrammar, Integer> eleme : calculadora.entrySet()) {
				System.out.println("->"+eleme.getKey().getNombre()+"=="+eleme.getValue());
			}
//			
//			
//			
//			HashMap<CompleteDocuments, Set<CompleteDocuments>> listaCuadraDobleRever=new HashMap<CompleteDocuments, Set<CompleteDocuments>>();
//			
//			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDoble.entrySet()) {
//				for (CompleteDocuments docenuevakey : entryreverse.getValue()) {
//					Set<CompleteDocuments> listatemp=listaCuadraDobleRever.get(docenuevakey);
//					if (listatemp==null)
//						listatemp=new HashSet<CompleteDocuments>();
//					listatemp.add(entryreverse.getKey());
//					listaCuadraDobleRever.put(docenuevakey, listatemp);
//				}
//			}
//			
//			
//			
//			System.out.println("Lista Normal sobre A");
//			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDoble.entrySet())  {
//				System.out.println("->"+entryreverse.getKey().getDescriptionText());
//				for (CompleteDocuments object : entryreverse.getValue()) {
//					System.out.println("------->"+object.getDescriptionText());
//				}
//			}
//			
//			
//			System.out.println("Lista Reverse sobre A");
//			for (Entry<CompleteDocuments, Set<CompleteDocuments>> entryreverse : listaCuadraDobleRever.entrySet())  {
//				System.out.println("->"+entryreverse.getKey().getDescriptionText());
//				for (CompleteDocuments object : entryreverse.getValue()) {
//					System.out.println("------->"+object.getDescriptionText());
//				}
//			}
			
			return nuevac;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		return c;
	}

	

	private static void printGrammar(List<CompleteElementType> sons, String string) {
		String string2 = "-"+string;
		for (CompleteElementType completeElementType : sons) {
			String T="N";

			if (completeElementType instanceof CompleteTextElementType)
				T="T";
			
			System.out.println(string2+completeElementType.getName()+"---"+T);
			printGrammar(completeElementType.getSons(), string2);
		}
		
	}



	



	private static CompleteGrammar findGrammar(List<CompleteGrammar> listaGrammar,
			String grammarnombrebuscar) {
		for (CompleteGrammar completeGrammar : listaGrammar) {
			if (completeGrammar.getNombre().equals(grammarnombrebuscar))
				return completeGrammar;
		}
		return null;
	}



	private static void procesaElementoDescendant(CompleteGrammar cGFinal, CompleteElementType elementoClavePadre, CompleteElementType padreReal,
			HashMap<CompleteGrammar, Integer> calculadora, JSONObject objetolista,
			List<CompleteGrammar> listaGrammar,
			HashMap<CompleteGrammar,List<CompleteElementType>> calculadoraFinal,
			HashMap<CompleteElementType, CompleteElementType> tablaCruceElemtos) {
//		System.out.println(objetolista.toJSONString());
		String nombre = (String) objetolista.get("name");
		

		JSONArray sons=null;
		
		if (objetolista.get("sons")!=null)
			sons = (JSONArray) objetolista.get("sons");
		
		
		Object multitype=null;
		
		if (objetolista.get("multitype")!=null)
			multitype = objetolista.get("multitype");

		if (multitype!=null)
			System.out.println("Error");
		
		CompleteElementType elementoClave = null;
		
		
		if (objetolista.get("eq")!=null&&objetolista.get("grammar")==null)
		{
			
			boolean _isResource=false;
			boolean _isText=false;
			

			if (objetolista.get("eq") instanceof JSONArray)
					{
					
				
					
					
					JSONArray listaEQ = (JSONArray)objetolista.get("eq");
					
					String equivalunico=(String)listaEQ.get(0);
					CompleteElementType CC=find(listaGrammar,equivalunico,padreReal);
					
					if (CC instanceof CompleteResourceElementType)
						_isResource=true;
					
					if (CC instanceof CompleteTextElementType)
						_isText=true;
					
					
					
					if (elementoClavePadre==null)
					{
						//Padre Gramatica
							if (_isResource)
								elementoClave=new CompleteResourceElementType(nombre, cGFinal);
							
							if (_isText)
								elementoClave=new CompleteTextElementType(nombre, cGFinal);

								
							if (elementoClave==null)
								elementoClave=new CompleteElementType(nombre, cGFinal);

			
						cGFinal.getSons().add(elementoClave);
						
					}
					else
					{
					
					//Padre Elemento Ahora esta abajo dentro del siguiente if, porque necesito saber si es TEXT o NO
			
						if (_isResource)
							elementoClave=new CompleteResourceElementType(nombre,elementoClavePadre, cGFinal);
						
						if (_isText)
							elementoClave=new CompleteTextElementType(nombre,elementoClavePadre, cGFinal);
			
							
						if (elementoClave==null)
							elementoClave=new CompleteElementType(nombre,elementoClavePadre, cGFinal);

			
			
					elementoClavePadre.getSons().add(elementoClave);
					}
					
					
					elementoClave.setMultivalued(true);
					elementoClave.setClassOfIterator(elementoClave);
					
					

					//Aqui esta el rollo a pensar
					
					if (CC!=null)
						if (testMultievaluado(CC))
							ProcesaCopiaMultievaluada(CC,listaGrammar,tablaCruceElemtos,elementoClave,
									elementoClavePadre,padreReal,cGFinal,sons,calculadora,calculadoraFinal,
									nombre,_isResource);
						else
							tablaCruceElemtos.put(CC, elementoClave);
					
					for (int k = 1; k < listaEQ.size(); k++) {
						
						
						
						String equivalunico2=(String)listaEQ.get(k);
						CompleteElementType CC2=find(listaGrammar,equivalunico2,padreReal);
						
						
						
						
						
						boolean _isResource2=false;
						
						if (CC2 instanceof CompleteResourceElementType)
							_isResource2=true;
						
						
						CompleteElementType elementoClave2;
						
						if (elementoClavePadre==null)
						{
						
							if (!_isResource)
								elementoClave2= new CompleteTextElementType(nombre, cGFinal);
							else
								elementoClave2= new CompleteResourceElementType(nombre, cGFinal);
							
							elementoClave2.setMultivalued(true);
							elementoClave2.setClassOfIterator(elementoClave);
							cGFinal.getSons().add(elementoClave2);
							
							if (sons!=null)
								for (int j = 0; j < sons.size(); j++) {
									JSONObject Objetolista = (JSONObject) sons.get(j);
									procesaElementoDescendant(cGFinal,elementoClave2,null,
											calculadora,Objetolista,listaGrammar,
											calculadoraFinal,tablaCruceElemtos);
								}
						
						}else
						{
							if (!_isResource)
								elementoClave2 = new CompleteTextElementType(nombre,elementoClavePadre, cGFinal);
							else
								elementoClave2 = new CompleteResourceElementType(nombre,elementoClavePadre, cGFinal);

							elementoClave2.setMultivalued(true);
							elementoClave2.setClassOfIterator(elementoClave);
							elementoClavePadre.getSons().add(elementoClave2);
							
							if (sons!=null)
								for (int j = 0; j < sons.size(); j++) {
									JSONObject Objetolista = (JSONObject) sons.get(j);
									procesaElementoDescendant(cGFinal,elementoClave2,null,
											calculadora,Objetolista,listaGrammar,
											calculadoraFinal,tablaCruceElemtos);
								}
						}
						
						
						
						if (CC2!=null)
							if (testMultievaluado(CC2))
								ProcesaCopiaMultievaluada(CC2,listaGrammar,tablaCruceElemtos,elementoClave2,
										elementoClavePadre,padreReal,cGFinal,sons,calculadora,calculadoraFinal,nombre,_isResource2);
							else
								tablaCruceElemtos.put(CC2, elementoClave2);
					}
						
					
				}else
				{
					
				

				CompleteElementType CC=find(listaGrammar,(String)objetolista.get("eq"),padreReal);
				
				if (CC instanceof CompleteResourceElementType)
					_isResource=true;
				
				
				if (elementoClavePadre==null)
				{
					//Padre Gramatica
						if (!_isResource)
							elementoClave=new CompleteTextElementType(nombre, cGFinal);
						else
							elementoClave=new CompleteResourceElementType(nombre, cGFinal);

		
					cGFinal.getSons().add(elementoClave);
					
				}
				else
				{
				
				//Padre Elemento Ahora esta abajo dentro del siguiente if, porque necesito saber si es TEXT o NO
		

					if (!_isResource)
						elementoClave=new CompleteTextElementType(nombre,elementoClavePadre, cGFinal);
					else
						elementoClave=new CompleteResourceElementType(nombre,elementoClavePadre, cGFinal);


		
		
				elementoClavePadre.getSons().add(elementoClave);
				}
				
				
				
				if (CC!=null)
					if (testMultievaluado(CC))
						ProcesaCopiaMultievaluada(CC,listaGrammar,tablaCruceElemtos,elementoClave,
								elementoClavePadre,padreReal,cGFinal,sons,calculadora,
								calculadoraFinal,nombre,_isResource);
					else
						tablaCruceElemtos.put(CC, elementoClave);

						
				}

			
		}else
			{
			
			if (elementoClavePadre==null)
			{
				//Padre Gramatica

					elementoClave=new CompleteElementType(nombre, cGFinal);
	
				cGFinal.getSons().add(elementoClave);
				
			}
			else
			{
			
			//Padre Elemento Ahora esta abajo dentro del siguiente if, porque necesito saber si es TEXT o NO

				elementoClave=new CompleteElementType(nombre,elementoClavePadre, cGFinal);
	
	
			elementoClavePadre.getSons().add(elementoClave);
			}
			
			
			}
		
		
		if (objetolista.get("grammar")==null)
		{
			
			//caso seguimos enla gramatica que tenemos de base
			if (sons!=null)
				for (int i = 0; i < sons.size(); i++) {
					JSONObject Objetolista = (JSONObject) sons.get(i);
					procesaElementoDescendant(cGFinal,elementoClave,null,
							calculadora,Objetolista,listaGrammar,calculadoraFinal,tablaCruceElemtos);
				}
			
		}
		else
		{
			//ataca a otra gramatica de la calculadora
			CompleteGrammar CG=findGrammar(listaGrammar,(String) objetolista.get("grammar"));
			if (CG!=null)
			{
				Integer numero=calculadora.get(CG);
				if (numero==null)
					numero=1;
				
				elementoClave.setMultivalued(true);
				elementoClave.setClassOfIterator(elementoClave);
				
				List<CompleteElementType> listaCopias=new LinkedList<CompleteElementType>();
				listaCopias.add(elementoClave);
				
				if (sons!=null)
					for (int i = 0; i < sons.size(); i++) {
						JSONObject Objetolista = (JSONObject) sons.get(i);
						procesaElementoDescendant(cGFinal,elementoClave,null,calculadora
								,Objetolista,listaGrammar,calculadoraFinal,tablaCruceElemtos);
					}

				
				for (int i = 0; i < numero-1; i++) {
					
					if (elementoClavePadre==null)
					{
						CompleteElementType elementoClave2 = new CompleteElementType(nombre, cGFinal);
						elementoClave2.setMultivalued(true);
						elementoClave2.setClassOfIterator(elementoClave);
						cGFinal.getSons().add(elementoClave2);
						listaCopias.add(elementoClave2);
						
						if (sons!=null)
							for (int j = 0; j < sons.size(); j++) {
								JSONObject Objetolista = (JSONObject) sons.get(j);
								procesaElementoDescendant(cGFinal,elementoClave2,null,
										calculadora,Objetolista,listaGrammar,
										calculadoraFinal,tablaCruceElemtos);
							}
						
					}else
					{
						CompleteElementType elementoClave2 = new CompleteElementType(nombre,elementoClavePadre, cGFinal);
						elementoClave2.setMultivalued(true);
						elementoClave2.setClassOfIterator(elementoClave);
						elementoClavePadre.getSons().add(elementoClave2);
						listaCopias.add(elementoClave2);
						
						if (sons!=null)
							for (int j = 0; j < sons.size(); j++) {
								JSONObject Objetolista = (JSONObject) sons.get(j);
								procesaElementoDescendant(cGFinal,elementoClave2,null,
										calculadora,Objetolista,listaGrammar,
										calculadoraFinal,tablaCruceElemtos);
							}
					}
					
					
					
				}
				
				
				calculadoraFinal.put(CG, listaCopias);
				
				

				
			}
		}
		
	}



	private static boolean testMultievaluado(CompleteElementType cC) {
		if (cC.isMultivalued())
			return true;
		else
			if (cC.getFather()!=null)
				return testMultievaluado(cC.getFather());
			else
				return false;
	}



	private static void ProcesaCopiaMultievaluada(CompleteElementType cC, List<CompleteGrammar> listaGrammar,
			HashMap<CompleteElementType, CompleteElementType> tablaCruceElemtos,
			CompleteElementType elementoClave,
			CompleteElementType elementoClavePadre,
			CompleteElementType elementoClavePadreReal,
			CompleteGrammar cGFinal,JSONArray sons,
			HashMap<CompleteGrammar, Integer> calculadora, HashMap<CompleteGrammar,
			List<CompleteElementType>> calculadoraFinal,
			String nombre, boolean _isResource) {
		
		if (cC.getClassOfIterator()!=null)
			cC=cC.getClassOfIterator();
		
		List<CompleteElementType> listaMulti=new LinkedList<CompleteElementType>();
		
		findList(listaGrammar,cC,listaMulti,elementoClavePadreReal);
		
		listaMulti.remove(cC);
		
		tablaCruceElemtos.put(cC, elementoClave);
		
		for (CompleteElementType completeElementType : listaMulti) {
			
			CompleteElementType elementoClave2;
			
			if (elementoClavePadre==null)
			{
			
				if (!_isResource)
					elementoClave2= new CompleteTextElementType(nombre, cGFinal);
				else
					elementoClave2= new CompleteResourceElementType(nombre, cGFinal);

				elementoClave2.setMultivalued(true);
				elementoClave2.setClassOfIterator(elementoClave);
				cGFinal.getSons().add(elementoClave2);
				
				if (sons!=null)
					for (int j = 0; j < sons.size(); j++) {
						JSONObject Objetolista = (JSONObject) sons.get(j);
						procesaElementoDescendant(cGFinal,elementoClave2,completeElementType,
								calculadora,Objetolista,listaGrammar,
								calculadoraFinal,tablaCruceElemtos);
					}
			
			}else
			{
				if (!_isResource)
					elementoClave2 = new CompleteTextElementType(nombre,elementoClavePadre, cGFinal);
				else
					elementoClave2 = new CompleteResourceElementType(nombre,elementoClavePadre, cGFinal);

				elementoClave2.setMultivalued(true);
				elementoClave2.setClassOfIterator(elementoClave);
				elementoClavePadre.getSons().add(elementoClave2);
				
				if (sons!=null)
					for (int j = 0; j < sons.size(); j++) {
						JSONObject Objetolista = (JSONObject) sons.get(j);
						procesaElementoDescendant(cGFinal,elementoClave2,completeElementType,
								calculadora,Objetolista,listaGrammar,
								calculadoraFinal,tablaCruceElemtos);
					}
			}
			
			tablaCruceElemtos.put(completeElementType, elementoClave2);
			
		}

	}



	private static void findList(List<CompleteGrammar> listaGrammar,
			CompleteElementType cC, List<CompleteElementType> listaMulti, CompleteElementType padreReal) {
		for (CompleteGrammar completeGrammar : listaGrammar) {
			findListEL(completeGrammar.getSons(),cC,listaMulti,padreReal);
		}
	}



	private static void findListEL(List<CompleteElementType> sons, CompleteElementType cC,
			List<CompleteElementType> listaMulti, CompleteElementType padreReal) {
		for (CompleteElementType completeElementType : sons) {
			if ((completeElementType.getClassOfIterator()==null&&completeElementType==cC)
					||completeElementType.getClassOfIterator()==cC)
				if (padreReal==null)
					listaMulti.add(completeElementType);
				else
					if (completeElementType.getFather()==padreReal)
						listaMulti.add(completeElementType);
			
			findListEL(completeElementType.getSons(), cC, listaMulti,padreReal);
		}
		
	}



	private static CompleteElementType find(List<CompleteGrammar> metamodelGrammar,
			String unionid, CompleteElementType father) {
		String[] splited = unionid.split("/");
		Queue<String> queue = new LinkedList<>(Arrays.asList(splited));
		String grammar = queue.poll();
		for (CompleteGrammar completeGrammar : metamodelGrammar) {
			if (grammar.equals(completeGrammar.getNombre()))
				if (father == null)
					return find(completeGrammar.getSons(),(Queue<String>) new LinkedList<String>(queue));
				else
					return find(completeGrammar.getSons(),
							(Queue<String>) new LinkedList<String>(queue),father);
					
		}
		return null;
	}



	private static CompleteElementType find(ArrayList<CompleteElementType> sons,
			Queue<String> queue, CompleteElementType father) {
		String elem=queue.poll();
		for (CompleteElementType celemEva : sons) {
			if (celemEva.getName().equals(elem)&&celemEva.getFather()==father)
				if (queue.isEmpty())
					{
					return celemEva;
					}
				else
					{ 
					CompleteElementType salida=find(celemEva.getSons(), new LinkedList<String>(queue));
							if (salida!=null)
								return salida;
					}
			
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





}
