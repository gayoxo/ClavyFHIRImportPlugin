package fdi.ucm.server.importparser.fhir.cens.v2.alternative;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

public class CollectionFHIR_CENS_CLEAN_SNOWMED {

	public static CompleteCollection Apply(CompleteCollection c) {
		CollectionFHIR_CENS_CLEAN_SNOWMED main=new CollectionFHIR_CENS_CLEAN_SNOWMED();
		System.out.println("Transformacion que Recupera de SNOWMED solo los terminos Sinonimos");
		return main.apply(c);
	}

	
	public CompleteCollection apply(CompleteCollection c) {
		CompleteGrammar CCSnowmed=null;
		for (CompleteGrammar cetG : c.getMetamodelGrammar()) 
			if (cetG.getNombre().toLowerCase().equals("snomed"))
				CCSnowmed=cetG;
		
		CompleteElementType DD=null;
		for (CompleteElementType listaTodos : CCSnowmed.getSons()) 
			if (listaTodos.getName().toLowerCase().equals("descriptions"))
				DD=listaTodos;
				
		List<CompleteElementType> Terminos= new LinkedList<CompleteElementType>();	
		Terminos.addAll(BuscaRecursivo(DD.getSons()));
		
		List<CompleteElementType> All=new LinkedList<CompleteElementType>();
		All.addAll(recuperaTodos(DD.getSons()));
		
		All.removeAll(Terminos);
		
		for (CompleteDocuments completemed : c.getEstructuras()) {
			List<CompleteElement> ElementosBorrar=new LinkedList<CompleteElement>();
			for (CompleteElement ceter : completemed.getDescription()) 
				if (All.contains(ceter.getHastype()))
					ElementosBorrar.add(ceter);
			
			
			completemed.getDescription().removeAll(ElementosBorrar);
		}
		
		CCSnowmed.getSons().clear();
		CCSnowmed.getSons().addAll(Terminos);
		
		
		
		return c;
	}

	private static Collection<? extends CompleteElementType> recuperaTodos(List<CompleteElementType> lista) {
		List<CompleteElementType> Terminos= new LinkedList<CompleteElementType>();
		for (CompleteElementType completeElementType : lista) {
			Terminos.add(completeElementType);
			Terminos.addAll(BuscaRecursivo(completeElementType.getSons()));
		}
		return Terminos;
	}

	private static List<CompleteElementType> BuscaRecursivo(List<CompleteElementType> lista) {
		List<CompleteElementType> Terminos= new LinkedList<CompleteElementType>();
		for (CompleteElementType completeElementType : lista) {
			if (completeElementType instanceof CompleteTextElementType 
					&& completeElementType.getName().toLowerCase().equals("term"))
				Terminos.add(completeElementType);
			Terminos.addAll(BuscaRecursivo(completeElementType.getSons()));
		}
		return Terminos;
	}

}
