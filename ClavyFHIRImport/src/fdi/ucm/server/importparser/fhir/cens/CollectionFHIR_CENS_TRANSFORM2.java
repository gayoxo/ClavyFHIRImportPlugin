package fdi.ucm.server.importparser.fhir.cens;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
		System.out.println("Transformacion que convierte elementos en otras naturalezas");
		return main.apply(c);
	}

	private CompleteCollection apply(CompleteCollection c_input) {
		
		HashSet<String> TablaResources=loadtable();
		
		HashMap<CompleteElementType, CompleteResourceElementType> ResorvalidValid=
				new HashMap<CompleteElementType, CompleteResourceElementType>();
		
		
		for (CompleteGrammar gramatica : c_input.getMetamodelGrammar()) 
		{
			String Base=gramatica.getNombre().trim();
			processHijos(gramatica.getSons(),TablaResources,
					ResorvalidValid,Base,c_input.getEstructuras());
			
			
			
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

	private void processHijos(List<CompleteElementType> sons, HashSet<String> tablaResources,
			HashMap<CompleteElementType, CompleteResourceElementType> resorvalidValid,
			String baseAcumulada, List<CompleteDocuments> listDocuments) {
		for (CompleteElementType elementoEndpoint : sons) {
			String Base=baseAcumulada+"/"+elementoEndpoint.getName().trim();
			if (tablaResources.contains(Base))
			{
				System.out.println("Convirtiendo a Recurso->"+Base);



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
								
								
							resorvalidValid.put(elementoEndpoint, copia);


				
				
			} else 
			{
				boolean found = false;
				int i = 0;
				while (!found&&i<listDocuments.size())
				{
					for (CompleteElement completeDocumentsElemnt : listDocuments.get(i).getDescription()) 
						if (completeDocumentsElemnt.getHastype()==elementoEndpoint)
							found=true;
					
					i++;
				}
				
				if (!found)
				{
					
					System.out.println("Convirtiendo a Basico->"+Base);



					CompleteElementType copia=new CompleteElementType(elementoEndpoint.getName(),
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
					
					
				}
				
			}
			
			
			processHijos(elementoEndpoint.getSons(), tablaResources, resorvalidValid, Base,listDocuments);
			
		}
		
	}

	private HashSet<String> loadtable() {
		List<String> Salida=new LinkedList<>();
		try {
			File file = new File("cens/resources.txt");
			Salida=Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Archivo no encontrado o con errores");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Archivo no encontrado o con errores");
		} 

		return new HashSet<>(Salida);
	}

}
