package fdi.ucm.server.importparser.fhir.cens.v2.alternative;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;

public class CollectionFHIR_CENS_TRANSFORM5_BORRASOBRANTE {

	public static CompleteCollection Apply(CompleteCollection c, String string) {
		
		List<CompleteGrammar> listaGram=new LinkedList<CompleteGrammar>();
		
		for (CompleteGrammar completeGrammar : c.getMetamodelGrammar()) 
			if (!completeGrammar.getNombre().toLowerCase().equals(string.toLowerCase())&&
					!completeGrammar.getNombre().toLowerCase().equals("snomed"))
				listaGram.add(completeGrammar);
		
		HashSet<CompleteElementType> Todos=new HashSet<CompleteElementType>();
		
		for (CompleteGrammar conGram : listaGram) {
			procesaLista(conGram.getSons(),Todos);
		}
		
		for (CompleteDocuments documenttos : c.getEstructuras()) {
			List<CompleteElement> ListaBorrar=new LinkedList<CompleteElement>();
			for (CompleteElement cElemetEle : documenttos.getDescription()) {
				if (Todos.contains(cElemetEle.getHastype()))
					ListaBorrar.add(cElemetEle);
			}
			
			documenttos.getDescription().removeAll(ListaBorrar);
			
		}
		
		List<CompleteDocuments> aBorrar=new LinkedList<CompleteDocuments>();
		for (CompleteDocuments documentosAver : c.getEstructuras()) {
			if (documentosAver.getDescription().isEmpty())
				aBorrar.add(documentosAver);
		}
		
		c.getEstructuras().removeAll(aBorrar);
		c.getMetamodelGrammar().removeAll(listaGram);
			
		return c;
	}

	private static void procesaLista(List<CompleteElementType> sons, HashSet<CompleteElementType> todos) {
		for (CompleteElementType cElemenmtType : sons) {
			todos.add(cElemenmtType);
			procesaLista(cElemenmtType.getSons(), todos);
		}
		
	}

}
