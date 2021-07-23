package fdi.ucm.server.importparser.fhir.cens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;

public class CollectionFHIR_CENS_TRANSFORM2 {

	public static CompleteCollection Apply(CompleteCollection c) {
		CollectionFHIR_CENS_TRANSFORM2 main=new CollectionFHIR_CENS_TRANSFORM2();
		return main.apply(c);
	}

	private CompleteCollection apply(CompleteCollection c_input) {
		
		
		HashMap<CompleteElementType, CompleteResourceElementType> ResorvalidValid=
				new HashMap<CompleteElementType, CompleteResourceElementType>();
		
		
		for (CompleteGrammar gramatica : c_input.getMetamodelGrammar()) 
			if (gramatica.getNombre().trim().toLowerCase().equals("endpoint"))
			{

				for (CompleteElementType elementoEndpoint : gramatica.getSons()) 
					if (elementoEndpoint.getName().trim().toLowerCase().equals("address"))
					{
						CompleteResourceElementType copia=new CompleteResourceElementType(elementoEndpoint.getName(),
								elementoEndpoint.getFather(), elementoEndpoint.getCollectionFather());
						
						copia.setMultivalued(elementoEndpoint.isMultivalued());
						copia.setBrowseable(elementoEndpoint.isBrowseable());
						copia.setSelectable(elementoEndpoint.isSelectable());
						copia.setBeFilter(elementoEndpoint.isBeFilter());
						
						copia.setShows(elementoEndpoint.getShows());
						copia.setSons(elementoEndpoint.getSons());
						
						for (CompleteElementType hijonuevo : copia.getSons())
							hijonuevo.setFather(copia);
						
						ArrayList<CompleteElementType> Nuevalista=new ArrayList<CompleteElementType>();
						boolean Found=false;
						
						if (elementoEndpoint.getFather()==null) {
							for(CompleteElementType hijosA: elementoEndpoint.getCollectionFather().getSons())
								if (hijosA==elementoEndpoint)
									{
									Nuevalista.add(copia);
									Found=true;
									}
								else
									Nuevalista.add(hijosA);
							
							
							if (!Found)
								Nuevalista.add(copia);
							
							elementoEndpoint.getCollectionFather().setSons(Nuevalista);
						}
						else
						{
							
							for(CompleteElementType hijosA: elementoEndpoint.getFather().getSons())
								if (hijosA==elementoEndpoint)
									{
									Nuevalista.add(copia);
									Found=true;
									}
								else
									Nuevalista.add(hijosA);
							
							
							if (!Found)
								Nuevalista.add(copia);
							
							elementoEndpoint.getFather().setSons(Nuevalista);
						}
							
							
						ResorvalidValid.put(elementoEndpoint, copia);

					}
			}
		
		
		for (CompleteDocuments docuemnto : c_input.getEstructuras()) {
			
			List<CompleteElement>  nuevo=new LinkedList<CompleteElement>();
			List<CompleteElement>  viejo=new LinkedList<CompleteElement>();
			for (CompleteElement elemento : docuemnto.getDescription()) {
				if (ResorvalidValid.get(elemento.getHastype())!=null)
				{
					nuevo.add(new CompleteResourceElementURL(ResorvalidValid.get(elemento.getHastype()), 
							((CompleteTextElement)elemento).getValue()));
					viejo.add(elemento);
				}
			}
			docuemnto.getDescription().addAll(nuevo);
			docuemnto.getDescription().removeAll(viejo);
		
		}
		
		
		for (CompleteResourceElementType completeElementType : ResorvalidValid.values()) 
			if (completeElementType.getClassOfIterator()!=null)
				completeElementType.setClassOfIterator(ResorvalidValid.get(completeElementType.getClassOfIterator()));
		
		
		return c_input;
	}

}
