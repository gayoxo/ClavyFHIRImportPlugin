package fdi.ucm.server.importparser.fhir.cens.v2;

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
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Set;
import java.util.Stack;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import fdi.ucm.server.importparser.json.CollectionJSON;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

public class CollectionFHIR_CENS_TRANSFORM4_COMPLETE {

	public static CompleteCollection Apply(CompleteCollection c, String EsquemaBase) {
		CollectionFHIR_CENS_TRANSFORM4_COMPLETE main=new CollectionFHIR_CENS_TRANSFORM4_COMPLETE();
		System.out.println("Unificacion de esquemas");
		return main.apply(c,EsquemaBase);
	}

	private CompleteCollection apply(CompleteCollection c_input, String esquemaBase) {
		
		List<CompleteDocuments> AProcesar=getDocumentosAfectados(c_input,esquemaBase);
		
		for (CompleteDocuments completeDocuments : AProcesar)
			procesaDocument(completeDocuments,c_input,new HashSet<CompleteDocuments>());
		
		
		List<CompleteGrammar> GramaticasAQuitar=new LinkedList<CompleteGrammar>();
		
		for (CompleteGrammar completeGrammar : c_input.getMetamodelGrammar()) 
			if (!completeGrammar.getNombre().equals(esquemaBase))
				GramaticasAQuitar.add(completeGrammar);
		
//		c_input.getMetamodelGrammar().removeAll(GramaticasAQuitar);
//		
//		c_input.setEstructuras(AProcesar);
		
		return c_input;
	}

private void procesaDocument(CompleteDocuments completeDocuments, CompleteCollection c_input,HashSet<CompleteDocuments> procesadoRama) {
		List<CompleteLinkElement> LinksCierre=new LinkedList<CompleteLinkElement>();
		procesadoRama.add(completeDocuments);
		for (CompleteElement completelement : completeDocuments.getDescription())
			if (completelement instanceof CompleteLinkElement)
				if (!procesadoRama.contains(((CompleteLinkElement) completelement).getValue()))
					LinksCierre.add(((CompleteLinkElement) completelement));

		
		for (CompleteLinkElement completeLinkElement : LinksCierre) 
			procesaDocument(completeLinkElement.getValue(), c_input, new HashSet<CompleteDocuments>(procesadoRama));
		
		for (CompleteLinkElement completeLinkElement : LinksCierre) 
		{
			CompleteElementType PadreHashType = completeLinkElement.getHastype().getFather();
			List<CompleteGrammar> CGSustitucion=getGrammarByDocument(completeLinkElement.getValue());
			
//			HashMap<CompleteElementType, CompleteElementType> CuadreGram2Elem=new HashMap<CompleteElementType, CompleteElementType>();
			
			for (CompleteGrammar CG : CGSustitucion) {
				CompleteElementType RootA=new CompleteElementType(CG.getNombre(), PadreHashType , getGrammar(PadreHashType));
				if (PadreHashType==null)
					getGrammar(PadreHashType).getSons().add(RootA);
				else
					PadreHashType.getSons().add(RootA);
				
				
				generaHaciaAbajo(CG.getSons(),RootA,completeLinkElement.getValue().getDescription(),completeDocuments);
				
				
			}
			
		}
		
		
	}

private void generaHaciaAbajo(List<CompleteElementType> sons, CompleteElementType padre,
		List<CompleteElement> description,CompleteDocuments completeDocuments) {
	for (CompleteElementType acopiar : sons) {
		CompleteElementType copia=generaCopiaNewElementType(acopiar,padre,getGrammar(padre));

		padre.getSons().add(copia);
		
		copia.setMultivalued(acopiar.isMultivalued());
		copia.setBrowseable(acopiar.isBrowseable());
		copia.setSelectable(acopiar.isSelectable());
		copia.setBeFilter(acopiar.isBeFilter());
		
		for (CompleteElement completeElement : description) 
			if (completeElement.getHastype()==acopiar)
			{
				CompleteElement completeElementCopia=generaCopiaNewElementValue(completeElement,copia);
				completeDocuments.getDescription().add(completeElementCopia);
			}
		
		generaHaciaAbajo(acopiar.getSons(), copia, description, completeDocuments);
	}
	
}

private CompleteElement generaCopiaNewElementValue(CompleteElement completeElement, CompleteElementType copia) {
	if (copia instanceof CompleteTextElementType)
		return new CompleteTextElement((CompleteTextElementType)copia,
				((CompleteTextElement) completeElement).getValue());
	if (copia instanceof CompleteResourceElementType)
		if (completeElement instanceof CompleteResourceElementFile)
			return new CompleteResourceElementFile((CompleteResourceElementType)copia,
					((CompleteResourceElementFile) completeElement).getValue());
		else if (completeElement instanceof CompleteResourceElementURL)
			return new CompleteResourceElementURL((CompleteResourceElementType)copia,
				((CompleteResourceElementURL) completeElement).getValue());
	if (copia instanceof CompleteLinkElementType)
		return new CompleteLinkElement((CompleteLinkElementType)copia,
				((CompleteLinkElement) completeElement).getValue());
	return new CompleteElement((CompleteElementType)copia);
}

private CompleteElementType generaCopiaNewElementType(CompleteElementType procesa, CompleteElementType father, CompleteGrammar GramaticaPadre ) {
	if (procesa instanceof CompleteTextElementType)
		return new CompleteTextElementType(procesa.getName(),
				father, GramaticaPadre);
	if (procesa instanceof CompleteResourceElementType)
		return new CompleteResourceElementType(procesa.getName(),
				father, GramaticaPadre);
	if (procesa instanceof CompleteLinkElementType)
		return new CompleteLinkElementType(procesa.getName(),
				father, GramaticaPadre);
	return new CompleteElementType(procesa.getName(),
			father, GramaticaPadre);
}

private List<CompleteGrammar> getGrammarByDocument(CompleteDocuments value) {
	Set<CompleteGrammar> ListaSalida=new HashSet<CompleteGrammar>();
	
	for (CompleteElement elemento : value.getDescription()) 
		ListaSalida.add(getGrammar(elemento.getHastype()));
	
	
	return new LinkedList<CompleteGrammar>(ListaSalida);
}

private List<CompleteDocuments> getDocumentosAfectados(CompleteCollection c_input,
		String esquemaBase) {
	
		List<CompleteDocuments> Salida=new LinkedList<CompleteDocuments>();
		for (CompleteDocuments completeDocuments : c_input.getEstructuras()) {
			
			if (includeDoc(completeDocuments.getDescription(),esquemaBase)){
				Salida.add(completeDocuments);
			}
		}
		return Salida;
	}

private boolean includeDoc(List<CompleteElement> description, String esquemaBase) {
	for (CompleteElement elemento : description) {
		
		if (getGrammar(elemento.getHastype()).getNombre().equals(esquemaBase))
			return true;
	}
	return false;
}

private CompleteGrammar getGrammar(CompleteElementType hastype) {
	CompleteElementType act=hastype;
	while (true)
	{
		if (act.getCollectionFather()!=null)
			return act.getCollectionFather();
		else
			act=act.getFather();
	}
	

}


	

}
