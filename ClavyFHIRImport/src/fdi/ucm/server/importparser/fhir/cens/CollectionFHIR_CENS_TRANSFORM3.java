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

public class CollectionFHIR_CENS_TRANSFORM3 {

	public static CompleteCollection Apply(CompleteCollection c) {
		CollectionFHIR_CENS_TRANSFORM3 main=new CollectionFHIR_CENS_TRANSFORM3();
		System.out.println("Snomed procesamiento y recoleccion");
		return main.apply(c);
	}

	private CompleteCollection apply(CompleteCollection c_input) {
		 
		HashSet<String> TablaResources=loadtable();
		HashMap<String, CompleteDocuments> DocumentosSnowmed=new HashMap<>();
		
		HashMap<CompleteElementType, CompleteLinkElementType> ResorvalidValid=
				new HashMap<CompleteElementType, CompleteLinkElementType>();
		
		
		for (CompleteGrammar gramatica : c_input.getMetamodelGrammar()) 
		{
			String Base=gramatica.getNombre().trim();
			processHijos(gramatica.getSons(),TablaResources,
					ResorvalidValid,Base,c_input.getEstructuras());
			
			
			
		}
		
		
		for (CompleteDocuments docuemnto : c_input.getEstructuras()) {
			
			List<CompleteElement>  nuevo=new LinkedList<CompleteElement>();
//			List<CompleteElement>  viejo=new LinkedList<CompleteElement>();
			for (CompleteElement elemento : docuemnto.getDescription()) {
				if (ResorvalidValid.get(elemento.getHastype())!=null)
				{
					CompleteLinkElementType ElementoPropio = ResorvalidValid.get(elemento.getHastype());
					
					if (isSnowmed(ElementoPropio, docuemnto.getDescription())) 
					{
						CompleteDocuments DocumenLinky=foundSnowLink(((CompleteTextElement)elemento).getValue(),
								DocumentosSnowmed,c_input); 
						
						if (DocumenLinky!=null)
							nuevo.add(new CompleteLinkElement(ElementoPropio, 
									DocumenLinky));
								
					}
//						nuevo.add(new CompleteResourceElementURL(ElementoPropio, 
//								((CompleteTextElement)elemento).getValue()));
//					viejo.add(elemento);
				}
			}
			docuemnto.getDescription().addAll(nuevo);
//			docuemnto.getDescription().removeAll(viejo);
		
		}
		
		for (CompleteDocuments completeDocuments : new HashSet<>(DocumentosSnowmed.values())) 
			c_input.getEstructuras().add(completeDocuments);
		
		
		for (CompleteLinkElementType completeElementType : ResorvalidValid.values()) 
			if (completeElementType.getClassOfIterator()!=null)
				completeElementType.setClassOfIterator(ResorvalidValid.get(completeElementType.getClassOfIterator()));
		
		
		CompleteCollection SNOMED=LoadCompleteLoad(DocumentosSnowmed.keySet());
		
//		Aplicamos Transformada 2 de nuevo para limpiar la gramtica de SNOWMED
//		SNOMED=CollectionFHIR_CENS_TRANSFORM1.Apply(SNOMED);
		SNOMED=CollectionFHIR_CENS_TRANSFORM2.Apply(SNOMED);
		
		c_input.getMetamodelGrammar().addAll(SNOMED.getMetamodelGrammar());
		

		for (CompleteDocuments completeDocuments : SNOMED.getEstructuras()) {
			CompleteDocuments real=DocumentosSnowmed.get(completeDocuments.getDescriptionText().trim());
			if (real!=null)
				real.getDescription().addAll(completeDocuments.getDescription());
			
		}
		
		
		return c_input;
	}

	private CompleteCollection LoadCompleteLoad(Set<String> keySet) {
		JsonArray JA=new JsonArray();
		
		for (String idconcept : keySet) {
			
			
			boolean Loaded=false;
			
			if ((new File("/tmp/snomed/"+idconcept+".json").exists()))
			{
				StringBuffer SB= new StringBuffer();
				try {
					File myObj = new File("/tmp/snomed/"+idconcept+".json");
				      Scanner myReader = new Scanner(myObj);
				      System.out.println("//////////INICIO CARGA"+idconcept);
				      while (myReader.hasNextLine()) {
				        String data = myReader.nextLine();
				        SB.append(data);
//				        System.out.println(data);
				      }
				      myReader.close();
				      System.out.println("//////////FIN "+idconcept);
				      
				      JsonElement JSONELEM = new JsonParser().parse(SB.toString());
						JA.add(JSONELEM);
				      
				      Loaded=true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				

				
				
			}
			
			
			if (!Loaded)
			{
			
			try {
				
				
				
				
				SSLContext ctx = SSLContext.getInstance("TLS");
	            ctx.init(null, new TrustManager[] { new InvalidCertificateTrustManager() }, null);  
	            SSLContext.setDefault(ctx);
				
				URL url = new URL("https://browser.ihtsdotools.org/snowstorm/snomed-ct/browser/MAIN/2019-07-31/concepts/"+idconcept);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				HttpURLConnection.setFollowRedirects(true);
				con.setInstanceFollowRedirects(true);
				con.setRequestMethod("GET");
				con.setRequestProperty("Content-Type", "application/json; utf-8");
				

				con.setConnectTimeout(5000);
				con.setReadTimeout(5000);
				
				int status = con.getResponseCode();
				
				if (status==HttpURLConnection.HTTP_OK)
				{
				BufferedReader in = new BufferedReader(
						  new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer content = new StringBuffer();
						while ((inputLine = in.readLine()) != null) {
						    content.append(inputLine);
						}
						in.close();
						
						
//						System.out.println(content);
						
						JsonElement JSONELEM = new JsonParser().parse(content.toString());
						
						
						JA.add(JSONELEM);
						
						
						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						String jsonOutput = gson.toJson(JSONELEM);
						
						
						
					String filename = "/tmp/snomed/"+idconcept+".json";
					
					
					 FileWriter myWriter = new FileWriter(filename);
				      myWriter.write(jsonOutput);
				      myWriter.close();
				      
				      System.out.println("//////////File->"+filename);
				      
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			}
			
		}
		
		
		CollectionJSON SNOWMEDCOl=new CollectionJSON();
		
		Properties prop=new Properties();
		prop.put("desc", "conceptId");
		
		ArrayList<String> Logete = new ArrayList<String>();
		
		SNOWMEDCOl.procesaJSONArrayFolder(JA, Logete , "SNOMED", prop);
		
		for (String logete : Logete) {
			System.out.println(logete);
		}
		
		return SNOWMEDCOl.getCollection();
	}

	private CompleteDocuments foundSnowLink(String value, HashMap<String, CompleteDocuments> documentosSnowmed,
			CompleteCollection c_input) {
		CompleteDocuments documentoFound = documentosSnowmed.get(value);
		if (documentoFound!=null)
			return documentoFound;
		
		documentoFound = new CompleteDocuments(c_input, value, "");
		documentosSnowmed.put(value, documentoFound);
		
		return documentoFound;
	}

	private boolean isSnowmed(CompleteLinkElementType elementoPropio, List<CompleteElement> description) {
		CompleteElementType CarpetaPadre = elementoPropio.getFather();
		CompleteElementType systemType=null;
		for (CompleteElementType completeElement : CarpetaPadre.getSons()){ 
			if (completeElement.getName().toLowerCase().equals("system")){
					systemType=completeElement;
			}
		}
		if (systemType==null)
			return false;
		
		for (CompleteElement completeElement : description) {
			if (completeElement.getHastype()==systemType && 
					completeElement instanceof CompleteTextElement &&
						((CompleteTextElement)completeElement).getValue().toLowerCase().contains("snomed"))
							return true;
		}
		
		
		return false;
	}

	private void processHijos(List<CompleteElementType> sons, HashSet<String> tablaResources,
			HashMap<CompleteElementType, CompleteLinkElementType> resorvalidValid,
			String baseAcumulada, List<CompleteDocuments> listDocuments) {
		for (CompleteElementType elementoEndpoint : sons) {
			String Base=baseAcumulada+"/"+elementoEndpoint.getName().trim();
			if (tablaResources.contains(Base))
			{
				System.out.println("Agregando Recurso->"+Base);



							CompleteLinkElementType copia=new CompleteLinkElementType(elementoEndpoint.getName(),
									elementoEndpoint.getFather(), elementoEndpoint.getCollectionFather());
							
							copia.setMultivalued(elementoEndpoint.isMultivalued());
							copia.setBrowseable(elementoEndpoint.isBrowseable());
							copia.setSelectable(elementoEndpoint.isSelectable());
							copia.setBeFilter(elementoEndpoint.isBeFilter());
							
							copia.setShows(elementoEndpoint.getShows());
							copia.setSons(elementoEndpoint.getSons());
							
							for (CompleteElementType hijonuevo : copia.getSons())
								hijonuevo.setFather(copia);
							
							ArrayList<CompleteElementType> Nuevalista;

							if (elementoEndpoint.getFather()==null) {
								Nuevalista=new ArrayList<CompleteElementType>(elementoEndpoint.getCollectionFather().getSons()); 
								Nuevalista.add(copia);
								
								elementoEndpoint.getCollectionFather().setSons(Nuevalista);
							}
							else
							{
								Nuevalista=new ArrayList<CompleteElementType>(elementoEndpoint.getFather().getSons()); 
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
					
					System.out.println("Agregando Basico->"+Base);



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
			File file = new File("cens/snomed.txt");
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
