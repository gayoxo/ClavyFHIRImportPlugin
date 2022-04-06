package fdi.ucm.server.importparser.fhir.cens.v2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

public class CollectionFHIR_CENS_TRANSFORM4_SEPARADO {

	public static CompleteCollection Apply(CompleteCollection c, String EsquemaBase) {
		CollectionFHIR_CENS_TRANSFORM4_SEPARADO main=new CollectionFHIR_CENS_TRANSFORM4_SEPARADO();
		System.out.println("Unificacion de esquemas");
		return main.apply(c,EsquemaBase);
	}

	private CompleteCollection apply(CompleteCollection c_input, String esquemaBase) {
		
		List<CompleteDocuments> AProcesar=getDocumentosAfectados(c_input,esquemaBase);
		
		HashMap<CompleteGrammar, List<CompleteElementType>> ListaAgregaTotal=new HashMap<CompleteGrammar, List<CompleteElementType>>();
		
		for (CompleteDocuments completeDocuments : AProcesar)
			{
			HashSet<CompleteDocuments> ListaFinal = new HashSet<CompleteDocuments>();
			procesaDocument(completeDocuments,c_input,ListaFinal);
			
			procesaTodosContraMi(completeDocuments,c_input,ListaFinal);
			
			ListaFinal.remove(completeDocuments);
			
			HashMap<CompleteGrammar, List<CompleteDocuments>> ListaAgrega=new HashMap<CompleteGrammar, List<CompleteDocuments>>();
			procesaListaAgrega(ListaAgrega,ListaFinal);
			
			for (Entry<CompleteGrammar, List<CompleteDocuments>> entryAgregaFinal : ListaAgrega.entrySet()) {
				CompleteGrammar gramaticaAgrega = entryAgregaFinal.getKey();
				List<CompleteDocuments> documentos= entryAgregaFinal.getValue();
				
				boolean ismultivalued = documentos.size()>1;
				
				CompleteGrammar Padre=null;
				
				for (CompleteElement elemento : completeDocuments.getDescription())
					
					if (getGrammar(elemento.getHastype()).getNombre().equals(esquemaBase))
						{
						Padre=getGrammar(elemento.getHastype());
						break;
						}
				

				
				for (CompleteDocuments documentosAgrega : documentos)
					AgregaADoc(completeDocuments,documentosAgrega,gramaticaAgrega,Padre,ismultivalued,ListaAgregaTotal);
				

				
				
			}
			
			}
		
		
		
		for (Entry<CompleteGrammar, List<CompleteElementType>> entry_gram_doc : ListaAgregaTotal.entrySet()) {
			CompleteGrammar aquiAgrego = entry_gram_doc.getValue().get(0).getCollectionFather();
			for (CompleteElementType agregate : entry_gram_doc.getValue()) 
				aquiAgrego.getSons().add(agregate);
			
		}
		
		
		
		List<CompleteGrammar> GramaticasAQuitar=new LinkedList<CompleteGrammar>();
		
		for (CompleteGrammar completeGrammar : c_input.getMetamodelGrammar()) 
			if (!completeGrammar.getNombre().equals(esquemaBase))
				GramaticasAQuitar.add(completeGrammar);
		
//		c_input.getMetamodelGrammar().removeAll(GramaticasAQuitar);
//		
//		c_input.setEstructuras(AProcesar);
		
		return c_input;
	}

private void procesaTodosContraMi(CompleteDocuments yodocument, CompleteCollection c_input,
			HashSet<CompleteDocuments> listaFinal) {
		for (CompleteDocuments documentoBase : c_input.getEstructuras()) {
			for (CompleteElement completeelementdoc : documentoBase.getDescription()) {
				if (completeelementdoc instanceof CompleteLinkElement)
					if (((CompleteLinkElement) completeelementdoc).getValue()==yodocument)
						listaFinal.add(documentoBase);
			}
		}
		
	}

private void AgregaADoc(CompleteDocuments completeDocuments, CompleteDocuments documentosAgrega,
			CompleteGrammar gramaticaAgrega, CompleteGrammar padre, boolean ismultivalued, 
			HashMap<CompleteGrammar, List<CompleteElementType>> listaAgregaTotal) {
	
	CompleteElementType RootA=new CompleteElementType(gramaticaAgrega.getNombre(), null , padre);
	if (ismultivalued)
		RootA.setMultivalued(ismultivalued);
	
	if (listaAgregaTotal.isEmpty()||listaAgregaTotal.get(gramaticaAgrega)==null)
		{
		RootA.setClassOfIterator(RootA);
		List<CompleteElementType> nuevo=new LinkedList<CompleteElementType>();
		nuevo.add(RootA);
		listaAgregaTotal.put(gramaticaAgrega,nuevo);
		}
	else
		{
		RootA.setClassOfIterator(listaAgregaTotal.get(gramaticaAgrega).get(0));
		List<CompleteElementType> nuevo=listaAgregaTotal.get(gramaticaAgrega);
		nuevo.add(RootA);
		listaAgregaTotal.put(gramaticaAgrega,nuevo);
		}

	
	generaHaciaAbajo(gramaticaAgrega.getSons(),RootA,documentosAgrega.getDescription(),completeDocuments);


	}

private void procesaListaAgrega(HashMap<CompleteGrammar, List<CompleteDocuments>> listaAgrega,
			HashSet<CompleteDocuments> listaFinal) {
		for (CompleteDocuments completeDocuments : listaFinal) {
			HashSet<CompleteGrammar> aplicaDoc=new HashSet<CompleteGrammar>();
			for (CompleteElement elementogeta : completeDocuments.getDescription()) 
				aplicaDoc.add(getGrammar(elementogeta.getHastype()));
		
			for (CompleteGrammar gramaticaA : aplicaDoc) {
				List<CompleteDocuments> listaAgregadoc=listaAgrega.get(gramaticaA);
				if (listaAgregadoc==null)
					listaAgregadoc=new LinkedList<CompleteDocuments>();
				listaAgregadoc.add(completeDocuments);
				listaAgrega.put(gramaticaA, listaAgregadoc);
			}
			
		}
		
	}

private void procesaDocument(CompleteDocuments completeDocuments, CompleteCollection c_input,HashSet<CompleteDocuments> procesadoRama) {
		List<CompleteLinkElement> LinksCierre=new LinkedList<CompleteLinkElement>();
		procesadoRama.add(completeDocuments);
		for (CompleteElement completelement : completeDocuments.getDescription())
			if (completelement instanceof CompleteLinkElement)
				if (!procesadoRama.contains(((CompleteLinkElement) completelement).getValue()))
					LinksCierre.add(((CompleteLinkElement) completelement));

		
		for (CompleteLinkElement completeLinkElement : LinksCierre) 
			procesaDocument(completeLinkElement.getValue(), c_input, procesadoRama);
	
		
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
