package fdi.ucm.server.importparser.fhir.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionFHIR {
	
	private static final String TAB_ESPACE_CONST = "--";
	private CompleteCollection Collection;
	public boolean debugfile=false;
	private ArrayList<String> log=new ArrayList<String>();
	private HashMap<String, TypeObject> TypetiposObjeto;
	private int limit;
	private boolean fin;
		
	
	class Entry<K, V>{
		
		K Key;
		V Value;
		
		public Entry(K key,V value) {
			Key=key;
			Value=value;
		}
		
		public K getKey() {
			return Key;
		}
		public void setKey(K key) {
			Key = key;
		}
		public V getValue() {
			return Value;
		}
		public void setValue(V value) {
			Value = value;
		}
		
		
		
	}
	

	public void procesaFHIR(String URLBase, ArrayList<String> log, int limit) {
				
		this.log=log;
		this.limit=limit;
		TypetiposObjeto= new HashMap<String, TypeObject>();
		
//		http://hapi.fhir.org/baseR4/Encounter?_pretty=true
		
		Collection=new CompleteCollection("Coleccion imported by URLBase", URLBase);
	
		cargoCasosClinicos(URLBase,limit);
		processGramar();
		
	}

	private void processGramar() {
		for (java.util.Map.Entry<String, TypeObject> typo_wrap : TypetiposObjeto.entrySet()) {
			Collection.getEstructuras().addAll(typo_wrap.getValue().getListaDocumentos().values());
			if (!typo_wrap.getValue().getPath_elem().isEmpty())
			{
				Collection.getMetamodelGrammar().add(typo_wrap.getValue().getTgramar());
				typo_wrap.getValue().getTgramar().setColeccion(Collection);
			}
		}
		
	}

	private void cargoCasosClinicos(String URLBase, int limit) {
		
		
		fin =false;
		
		if (debugfile&& (new File("files/casosclinicos.json").exists()))
		{
			System.out.println("//////////Leido archivo de pacientes");
			
			String actual= "files/casosclinicos.json";
			int indice=1;
			while ((new File(actual).exists()) && !fin)
			{
				StringBuffer SB= new StringBuffer();
				try {
					File myObj = new File(actual);
				      Scanner myReader = new Scanner(myObj);
				      System.out.println("//////////INICIO "+actual);
				      while (myReader.hasNextLine()) {
				        String data = myReader.nextLine();
				        SB.append(data);
//				        System.out.println(data);
				      }
				      myReader.close();
				      System.out.println("//////////FIN "+actual);
				} catch (Exception e) {
					log.add(e.getMessage());
				}
				
				actual= "files/casosclinicos.json."+indice+".json";
				indice++;
				
				JsonElement JSONELEM = new JsonParser().parse(SB.toString());
				
					
				processCasosClinicos(JSONELEM.getAsJsonObject());
				
			}
		}else
		{
			if (debugfile)
				System.out.println("//////////generado archivo de pacientes");
			
			
			List<Entry<String,String>> parameters=new LinkedList<Entry<String,String>>();

			parameters.add(new Entry<String,String>("_include", "DiagnosticReport:patient"));
			parameters.add(new Entry<String,String>("_include", "DiagnosticReport:result"));
			parameters.add(new Entry<String,String>("_format", "json"));
			parameters.add(new Entry<String,String>("_pretty", "true"));
			if (limit>0)
				parameters.add(new Entry<String,String>("_count", Integer.toString(limit)));
			

			StringBuffer querryBuffer= new StringBuffer();
			
			try {
			querryBuffer.append(URLBase);
			querryBuffer.append("/DiagnosticReport?");
			
				querryBuffer.append(ParameterStringBuilder.getParamsString(parameters));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			//http://hapi.fhir.org/baseR4/Encounter?_include=Encounter:patient&_pretty=true
			//http://hapi.fhir.org/baseR4/Condition?_include=Condition:encounter&_include=Condition:patient&_pretty=true
			//http://hapi.fhir.org/baseR4/DiagnosticReport?_include=DiagnosticReport:patient&_include=DiagnosticReport:result&_pretty=true
			//http://hapi.fhir.org/baseR4/DiagnosticReport?_include=DiagnosticReport:patient&_include=DiagnosticReport:result&_count=1&_pretty=true
			//http://hapi.fhir.org/baseR4/DiagnosticReport?_include=DiagnosticReport:encounter&_include=DiagnosticReport:media&_include=DiagnosticReport:patient&_include=DiagnosticReport:result&_count=1&_format=json&_pretty=true
			
			String ActualURL = querryBuffer.toString();
			
			if (debugfile)
				System.out.println(ActualURL);
			
			int conteoFile=0;
			
			

			while (ActualURL!=null &&!ActualURL.isEmpty() && !fin)
			
			{
			try {

				
				URL url = new URL(ActualURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Content-Type", "application/json; utf-8");
				

				con.setConnectTimeout(5000);
				con.setReadTimeout(5000);
				
				int status = con.getResponseCode();
				
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
						
						if (debugfile)
						{
							
							Gson gson = new GsonBuilder().setPrettyPrinting().create();
							String jsonOutput = gson.toJson(JSONELEM);
							
							
							
						String filename = "files/casosclinicos.json";
						if (conteoFile>0)
							filename = "files/casosclinicos.json."+conteoFile+".json";
						
						
						 FileWriter myWriter = new FileWriter(filename);
					      myWriter.write(jsonOutput);
					      myWriter.close();
					      
					      System.out.println("//////////File->"+filename);
					      
					      conteoFile++;
						}
						
						Reader streamReader = null;

						if (status > 299) {
						    streamReader = new InputStreamReader(con.getErrorStream());
						} else {
						    streamReader = new InputStreamReader(con.getInputStream());
						}
						
						if (streamReader!=null)
							log.add(streamReader.toString());
						
				con.disconnect();
				
				
				JsonElement LINKNEXT = JSONELEM.getAsJsonObject().get("link");
				JsonArray LINKNEXT_arra=LINKNEXT.getAsJsonArray();
				
				String next_link=null; 
				
				for (int i = 0; i < LINKNEXT_arra.size(); i++) {
					JsonObject obj=LINKNEXT_arra.get(i).getAsJsonObject();
					if (obj.get("relation").getAsJsonPrimitive().getAsString().toLowerCase().equals("next"))
					{
						next_link=obj.get("url").getAsJsonPrimitive().getAsString();
						System.out.println(next_link);
					}
					
				}

				processCasosClinicos(JSONELEM.getAsJsonObject());
				
					ActualURL=next_link;
				
				
			} catch (MalformedURLException e) {
				log.add(e.getMessage());
				e.printStackTrace();
				ActualURL=null;
			} catch (IOException e) {
				log.add(e.getMessage());
				e.printStackTrace();
				ActualURL=null;
			}
			
			}
		}
	}

	

	private void processCasosClinicos(JsonObject asJsonObject) {
		JsonElement ENTRYNEXT = asJsonObject.get("entry");
		JsonArray ENTRYNEXT_Array=ENTRYNEXT.getAsJsonArray();
		
		for (int i = 0; i < ENTRYNEXT_Array.size(); i++) {
			
			JsonObject entry_in=ENTRYNEXT_Array.get(i).getAsJsonObject();

			
			
			JsonObject oresource = entry_in.get("resource").getAsJsonObject();
			String tResource= oresource.get("resourceType").getAsJsonPrimitive().getAsString();
			
			if (tResource.toLowerCase().equals("diagnosticreport")&&
					TypetiposObjeto.get(tResource)!=null&&
							TypetiposObjeto.get(tResource).getListaDocumentos().size()==limit){
				fin=true;
				return;
			}

			
			TypeObject WrappedObjeto = TypetiposObjeto.get(tResource);
			if (WrappedObjeto==null)
				{
				WrappedObjeto=new TypeObject(tResource);
				TypetiposObjeto.put(tResource, WrappedObjeto);
				}
			
		
			produceStructureDocument(WrappedObjeto,entry_in);
			
			
			
			
//			if (tResource.toLowerCase().contentEquals("condition"))
//				processAsCondition(oresource,fURL);
//
//			if (tResource.toLowerCase().contentEquals("patient"))
//				processAsPatient(oresource,fURL);

		}

		
	}

private void produceStructureDocument(TypeObject wrappedObjeto, JsonObject entry_in) {
	String fURL= entry_in.get("fullUrl").getAsJsonPrimitive().getAsString();
	JsonObject oresource = entry_in.get("resource").getAsJsonObject();
	String id_text= oresource.get("id").getAsJsonPrimitive().getAsString();
	System.out.println(wrappedObjeto.getTname()+"->"+id_text+"  "+ fURL);
	
	CompleteDocuments CD=new CompleteDocuments(Collection, "NO DESC", "");
	
	CompleteTextElementType url_elem=wrappedObjeto.createElementType("fullUrl");
	CD.getDescription().add(new CompleteTextElement(url_elem, fURL));
	
	CompleteTextElementType id_elem=wrappedObjeto.createElementType("id");
	CD.getDescription().add(new CompleteTextElement(id_elem, id_text));
	
	wrappedObjeto.getListaDocumentos().put(id_text, CD);
	
	}

//	private void processAsPatient(JsonObject oresource, String fURL) {
//		String id_text= oresource.get("id").getAsJsonPrimitive().getAsString();
//		System.out.println("Patient"+"->"+id_text+"  "+ fURL);
//		
//	}
//
//	private void processAsCondition(JsonObject oresource, String fURL) {
//		String id_text= oresource.get("id").getAsJsonPrimitive().getAsString();
//		System.out.println("Condition"+"->"+id_text+"  "+ fURL);
//		
//	}

	public CompleteCollection getColeccion() {
		return Collection;
	}
	
	public static void main(String[] args) {
		CollectionFHIR C=new CollectionFHIR();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaFHIR("http://hapi.fhir.org/baseR4", log,2);
		
//		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
//				
//				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));
//
//				oos.writeObject(C.getColeccion());
//
//				oos.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
	}

}
