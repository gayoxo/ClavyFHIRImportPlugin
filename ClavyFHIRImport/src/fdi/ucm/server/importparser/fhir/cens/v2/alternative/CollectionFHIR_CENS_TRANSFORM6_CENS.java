package fdi.ucm.server.importparser.fhir.cens.v2.alternative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;

public class CollectionFHIR_CENS_TRANSFORM6_CENS {

	public static CompleteCollection Apply(CompleteCollection c) {
		CompleteGrammar SnoGram = null;
		for (CompleteGrammar colGram : c.getMetamodelGrammar()) {
			if (colGram.getNombre().toLowerCase().equals("snomed"))
				SnoGram=colGram;
		}
		if (SnoGram!=null)
		{
			HashSet<CompleteDocuments> CD=new HashSet<CompleteDocuments>();
			List<CompleteElementType> ListaCEt=new LinkedList<CompleteElementType>();
			
			ListaCEt.addAll(agregaTodos(SnoGram.getSons()));
			
			for (CompleteDocuments allDocuments : c.getEstructuras()) 
				for (CompleteElement cElemento : allDocuments.getDescription()) 
					if (ListaCEt.contains(cElemento.getHastype()))
						CD.add(allDocuments);
			
			List<CompleteDocuments> ListaResto=new LinkedList<CompleteDocuments>(c.getEstructuras());
			ListaResto.removeAll(CD);
			
//			for (CompleteDocuments completeDocuments : ListaResto) {
//				for (CompleteElement elemenT : completeDocuments.getDescription()) {
//					if (elemenT instanceof CompleteResourceElementType)
//				}
//			}
			
		
		}
		return c;
	}

	private static List<CompleteElementType> agregaTodos(List<CompleteElementType> sons) {
		List<CompleteElementType> ListaCEt=new LinkedList<CompleteElementType>();
		
		for (CompleteElementType completeElementType : sons) {
			ListaCEt.add(completeElementType);
			ListaCEt.addAll(agregaTodos(completeElementType.getSons()));
		}
		
		return ListaCEt;
	}

}
