package fdi.ucm.server.importparser.fhir.cens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;

public class CollectionFHIR_CENS_TRANSFORM4 {

	public static CompleteCollection Apply(CompleteCollection c, String EsquemaBase) {
		CollectionFHIR_CENS_TRANSFORM4 main=new CollectionFHIR_CENS_TRANSFORM4();
		System.out.println("Unificacion de esquemas");
		return main.apply(c,EsquemaBase);
	}

	private CompleteCollection apply(CompleteCollection c_input, String esquemaBase) {
		 
		
		boolean continuar=true;
		
		while (continuar) {
		
		for (CompleteGrammar gramaticas : c_input.getMetamodelGrammar()) {
			if (gramaticas.getNombre().toLowerCase().equals(esquemaBase))
				continuar=procesaelement(gramaticas,c_input.getEstructuras());
		}

		}
		
		return c_input;
		
//		//TODO AQUI ME HE QUEDADO
//		
//		HashSet<String> TablaResources=loadtable();
//		HashMap<String, CompleteDocuments> DocumentosSnowmed=new HashMap<>();
//		
//		HashMap<CompleteElementType, CompleteLinkElementType> ResorvalidValid=
//				new HashMap<CompleteElementType, CompleteLinkElementType>();
//		
//		
//		for (CompleteGrammar gramatica : c_input.getMetamodelGrammar()) 
//		{
//			String Base=gramatica.getNombre().trim();
//			processHijos(gramatica.getSons(),TablaResources,
//					ResorvalidValid,Base,c_input.getEstructuras());
//			
//			
//			
//		}
//		
//		
//		for (CompleteDocuments docuemnto : c_input.getEstructuras()) {
//			
//			List<CompleteElement>  nuevo=new LinkedList<CompleteElement>();
////			List<CompleteElement>  viejo=new LinkedList<CompleteElement>();
//			for (CompleteElement elemento : docuemnto.getDescription()) {
//				if (ResorvalidValid.get(elemento.getHastype())!=null)
//				{
//					CompleteLinkElementType ElementoPropio = ResorvalidValid.get(elemento.getHastype());
//					
//					if (isSnowmed(ElementoPropio, docuemnto.getDescription())) 
//					{
//						CompleteDocuments DocumenLinky=foundSnowLink(((CompleteTextElement)elemento).getValue(),
//								DocumentosSnowmed,c_input); 
//						
//						if (DocumenLinky!=null)
//							nuevo.add(new CompleteLinkElement(ElementoPropio, 
//									DocumenLinky));
//								
//					}
////						nuevo.add(new CompleteResourceElementURL(ElementoPropio, 
////								((CompleteTextElement)elemento).getValue()));
////					viejo.add(elemento);
//				}
//			}
//			docuemnto.getDescription().addAll(nuevo);
////			docuemnto.getDescription().removeAll(viejo);
//		
//		}
//		
//		for (CompleteDocuments completeDocuments : new HashSet<>(DocumentosSnowmed.values())) 
//			c_input.getEstructuras().add(completeDocuments);
//		
//		
//		for (CompleteLinkElementType completeElementType : ResorvalidValid.values()) 
//			if (completeElementType.getClassOfIterator()!=null)
//				completeElementType.setClassOfIterator(ResorvalidValid.get(completeElementType.getClassOfIterator()));
//		
//		
//		CompleteCollection SNOMED=LoadCompleteLoad(DocumentosSnowmed.keySet());
//		
////		Aplicamos Transformada 2 de nuevo para limpiar la gramtica de SNOWMED
////		SNOMED=CollectionFHIR_CENS_TRANSFORM1.Apply(SNOMED);
//		SNOMED=CollectionFHIR_CENS_TRANSFORM2.Apply(SNOMED);
//		
//		c_input.getMetamodelGrammar().addAll(SNOMED.getMetamodelGrammar());
//		
//
//		for (CompleteDocuments completeDocuments : SNOMED.getEstructuras()) {
//			CompleteDocuments real=DocumentosSnowmed.get(completeDocuments.getDescriptionText().trim());
//			if (real!=null)
//				real.getDescription().addAll(completeDocuments.getDescription());
//			
//		}
//		
//		
//		return c_input;
	}

//	private boolean procesa__c_input(CompleteCollection c_input) {
//		boolean salida=false;
//		for (CompleteGrammar gramatica : c_input.getMetamodelGrammar()) {
//			salida=procesaelement(gramatica,c_input.getEstructuras())||salida;
//
//		}
//		return salida;
//	}

	
	//SEGURO QUE ME DAN PROBLEMAS LOS MULTIEVALUADOS
	private boolean procesaelement(CompleteGrammar gramatica, List<CompleteDocuments> Documentos) {
		boolean salida=false;
		List<CompleteElementType> listaprocesa=new LinkedList<CompleteElementType>(gramatica.getSons());
		for (CompleteElementType element : listaprocesa) {
			
			if (element instanceof CompleteLinkElementType)
			{
				
				List<CompleteElementType> nuevoElement=procesaLink((CompleteLinkElementType)element,Documentos);
				
				int posicion = gramatica.getSons().lastIndexOf(element);
				
				Collections.reverse(nuevoElement);
				
				for (CompleteElementType completeElementType : nuevoElement) {
					gramatica.getSons().add(posicion, completeElementType);
				}
				
				gramatica.getSons().remove(element);

				salida=true;
			}else
				salida=procesaelement(element,Documentos)||salida;
		}
		
		return salida;
	}

	

	private List<CompleteElementType> procesaLink(CompleteLinkElementType element, List<CompleteDocuments> documentos) {
		List<CompleteElementType> nuevos=new ArrayList<CompleteElementType>();
		
		//TODO AQUI HAY QUE METER MANO CUIDADO LOS BUCLES, no se procesa si es la misma gramatica (asi se evita)
		
		return nuevos;
	}

	//SEGURO QUE ME DAN PROBLEMAS LOS MULTIEVALUADOS
	private boolean procesaelement(CompleteElementType elementoentrada, List<CompleteDocuments> Documentos) {
		boolean salida=false;
		List<CompleteElementType> listaprocesa=new LinkedList<CompleteElementType>(elementoentrada.getSons());
		for (CompleteElementType element : listaprocesa) {
			
			if (element instanceof CompleteLinkElementType)
			{
				
				List<CompleteElementType> nuevoElement=procesaLink((CompleteLinkElementType)element,Documentos);
				
				int posicion = elementoentrada.getSons().lastIndexOf(element);
				
				Collections.reverse(nuevoElement);
				
				for (CompleteElementType completeElementType : nuevoElement) {
					elementoentrada.getSons().add(posicion, completeElementType);
				}

				elementoentrada.getSons().remove(element);

				salida=true;
			}else
				salida=procesaelement(element,Documentos)||salida;
		}
		
		return salida;
	}

	

}
