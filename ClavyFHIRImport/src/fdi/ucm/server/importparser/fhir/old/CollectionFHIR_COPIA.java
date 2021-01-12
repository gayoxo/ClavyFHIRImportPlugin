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
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionFHIR_COPIA {
	
	private static final String TAB_ESPACE_CONST = "--";
	private CompleteCollection Collection;
	public boolean debugfile=false;
	private ArrayList<String> log=new ArrayList<String>();
	private Map<String, HashMap<String,Object>> PACIENTES;
	private int MAXIMO_name;
	private int MAXIMO_name_given;
	private Map<String, CompleteDocuments> PACIENTES_CLAVY;
	private CompleteGrammar Pacientes;
		
	

	public void procesaFHIR(String URLBase, ArrayList<String> log, int limit) {
				
		this.log=log;
		
		
//		http://hapi.fhir.org/baseR4/Encounter?_pretty=true
		
		cargoCasosClinicosV2(URLBase,limit);
		
		PACIENTES=new HashMap<String, HashMap<String,Object>>();
		MAXIMO_name=1;
		MAXIMO_name_given=1;
		PACIENTES_CLAVY=new HashMap<String, CompleteDocuments>();
		
		Collection=new CompleteCollection("Coleccion imported by URLBase", URLBase);
		
		cargaPacientes(URLBase,limit);
				
		//SALVO PERO SIN CASOS CLINICOS LINKEADOS
		salvaPacientes();
		
		cargaCasosClinicos(URLBase);
		
		
	}

	private void cargoCasosClinicosV2(String uRLBase, int limit) {
		// TODO Auto-generated method stub
		
	}

	private void cargaCasosClinicos(String URLBase) {
		
		
		for (String string : PACIENTES.keySet()) {
			if (debugfile&& (new File("casosclinicos_"+string+".json").exists()))
			{
				System.out.println("//////////Caso Clinico Leido " +string );
				
				StringBuffer SB= new StringBuffer();
				try {
					File myObj = new File("casosclinicos_"+string+".json");
				      Scanner myReader = new Scanner(myObj);
				      System.out.println("//////////INICIO  casosclinicos_"+string+".json");
				      while (myReader.hasNextLine()) {
				        String data = myReader.nextLine();
				        SB.append(data);
//				        System.out.println(data);
				      }
				      myReader.close();
				      System.out.println("//////////FIN  casosclinicos_"+string+".json");
				} catch (Exception e) {
					log.add(e.getMessage());
				}
				
				
				JsonElement JSONELEM = new JsonParser().parse(SB.toString());
				processJSON_CC(JSONELEM.getAsJsonObject());
			}else
			{
				if (debugfile)
					System.out.println("//////////generado archivo de caso clinico " + string);
				
				Map<String, String> parameters = new HashMap<>();
				parameters.put("subject", string);
				parameters.put("_format", "json");
				parameters.put("_pretty", "true");

				
				StringBuffer querryBuffer= new StringBuffer();
				
				try {
				querryBuffer.append(URLBase);
				querryBuffer.append("/Encounter?");
				
					querryBuffer.append(ParameterStringBuilder.getParamsString(parameters));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				
				
				
				String ActualURL = querryBuffer.toString();
				
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
							
							
							System.out.println(content);
							
							JsonElement JSONELEM = new JsonParser().parse(content.toString());
							
							if (debugfile)
							{
								
								Gson gson = new GsonBuilder().setPrettyPrinting().create();
								String jsonOutput = gson.toJson(JSONELEM);
								
								
								
							String filename = "casosclinicos_"+string+".json";

							
							 FileWriter myWriter = new FileWriter(filename);
						      myWriter.write(jsonOutput);
						      myWriter.close();
						      
						      System.out.println("//////////File->"+filename);

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
					
					

					
					
					processJSON_CC(JSONELEM.getAsJsonObject());
					
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

	private void processJSON_CC(JsonObject asJsonObject) {
		// TODO Auto-generated method stub
		
	}

	private void salvaPacientes() {
		
		Pacientes=new CompleteGrammar("Patient","Inmormation refer to patiens", Collection);
		Collection.getMetamodelGrammar().add(Pacientes);
		
		CompleteTextElementType CET_ID=new CompleteTextElementType("id", Pacientes);
		Pacientes.getSons().add(CET_ID);
		CompleteTextElementType CET_URL=new CompleteTextElementType("url", Pacientes);
		Pacientes.getSons().add(CET_URL);
		CompleteTextElementType CET_DESC=new CompleteTextElementType("desc", Pacientes);
		Pacientes.getSons().add(CET_DESC);
		CompleteTextElementType CET_GENDER=new CompleteTextElementType("gender", Pacientes);
		Pacientes.getSons().add(CET_GENDER);
		CompleteTextElementType CET_BIR=new CompleteTextElementType("birthdate", Pacientes);
		Pacientes.getSons().add(CET_BIR);
		CompleteElementType CET_NAME=new CompleteElementType("names", Pacientes);
		Pacientes.getSons().add(CET_NAME);
		CET_NAME.setMultivalued(true);
		CET_NAME.setClassOfIterator(CET_NAME);
		
		
		CompleteTextElementType CET_NAME_USO=new CompleteTextElementType("use",CET_NAME,Pacientes);
		CET_NAME.getSons().add(CET_NAME_USO);
		CET_NAME_USO.setClassOfIterator(CET_NAME_USO);
		
		CompleteTextElementType CET_NAME_FAMILIA=new CompleteTextElementType("family",CET_NAME, Pacientes);
		CET_NAME.getSons().add(CET_NAME_FAMILIA);
		CET_NAME_FAMILIA.setClassOfIterator(CET_NAME_FAMILIA);

		
		CompleteTextElementType CET_NAME_GIVEN=new CompleteTextElementType("given",CET_NAME, Pacientes);
		CET_NAME.getSons().add(CET_NAME_GIVEN);
		CET_NAME_GIVEN.setClassOfIterator(CET_NAME_GIVEN);
		CET_NAME_GIVEN.setMultivalued(true);
		
		
		List<CompleteTextElementType> _NAME_USO=new LinkedList<CompleteTextElementType>();
		List<CompleteTextElementType> _NAME_FAMILIA=new LinkedList<CompleteTextElementType>();
		List<List<CompleteTextElementType>> _NAME_GIVEN=new LinkedList<List<CompleteTextElementType>>();

		
		_NAME_USO.add(CET_NAME_USO);
		
		_NAME_FAMILIA.add(CET_NAME_FAMILIA);
		
		LinkedList<CompleteTextElementType> _CET_NAME_GIVEN = new LinkedList<CompleteTextElementType>();
		_CET_NAME_GIVEN.add(CET_NAME_GIVEN);
		for (int i = 1; i < MAXIMO_name_given; i++) {
			CompleteTextElementType CET_NAME_GIVEN_IN=new CompleteTextElementType("given",CET_NAME, Pacientes);
			CET_NAME_GIVEN_IN.setMultivalued(true);
			CET_NAME_GIVEN_IN.setClassOfIterator(CET_NAME_GIVEN);
			CET_NAME.getSons().add(CET_NAME_GIVEN_IN);
			_CET_NAME_GIVEN.add(CET_NAME_GIVEN_IN);
		}
		
		
		_NAME_GIVEN.add(_CET_NAME_GIVEN);
		
		for (int i = 1; i < MAXIMO_name; i++) {
			CompleteElementType CET_NAME_IN=new CompleteElementType("names", Pacientes);
			Pacientes.getSons().add(CET_NAME_IN);
			CET_NAME_IN.setMultivalued(true);
			CET_NAME_IN.setClassOfIterator(CET_NAME);

			CompleteTextElementType CET_NAME_USO_IN=new CompleteTextElementType("use",CET_NAME,Pacientes);
			CET_NAME_IN.getSons().add(CET_NAME_USO_IN);
			CET_NAME_USO_IN.setClassOfIterator(CET_NAME_USO);
			
			_NAME_USO.add(CET_NAME_USO);
					
			CompleteTextElementType CET_NAME_FAMILIA_IN=new CompleteTextElementType("family",CET_NAME, Pacientes);
			CET_NAME_IN.getSons().add(CET_NAME_FAMILIA_IN);
			CET_NAME_FAMILIA_IN.setClassOfIterator(CET_NAME_FAMILIA);
			
			_NAME_FAMILIA.add(CET_NAME_FAMILIA);
			
			CompleteTextElementType CET_NAME_GIVEN_IN=new CompleteTextElementType("given",CET_NAME, Pacientes);
			CET_NAME_IN.getSons().add(CET_NAME_GIVEN_IN);
			CET_NAME_GIVEN_IN.setClassOfIterator(CET_NAME_GIVEN);
			
			
			LinkedList<CompleteTextElementType> _CET_NAME_GIVEN_IN = new LinkedList<CompleteTextElementType>();
			_CET_NAME_GIVEN_IN.add(CET_NAME_GIVEN_IN);
			for (int j = 1; j < MAXIMO_name_given; j++) {
				CompleteTextElementType CET_NAME_GIVEN_IN_2=new CompleteTextElementType("given",CET_NAME, Pacientes);
				CET_NAME_GIVEN_IN_2.setMultivalued(true);
				CET_NAME_GIVEN_IN_2.setClassOfIterator(CET_NAME_GIVEN);
				CET_NAME_IN.getSons().add(CET_NAME_GIVEN_IN_2);
				_CET_NAME_GIVEN_IN.add(CET_NAME_GIVEN_IN_2);
			}
			
			_NAME_GIVEN.add(_CET_NAME_GIVEN_IN);
			
		}
		

		for (Entry<String, HashMap<String, Object>> paciente : PACIENTES.entrySet()) {
			
			HashMap<String, Object> TablaValores = paciente.getValue();
			
			String Desc=TablaValores.get("ID").toString();
			
			
			CompleteDocuments CD=new CompleteDocuments(Collection,Desc,"");
			Collection.getEstructuras().add(CD);
			
			PACIENTES_CLAVY.put(paciente.getKey(), CD);
			
			if (paciente.getValue().get("BIR")!=null)
			{
			CompleteTextElement CT_BIR=new CompleteTextElement(CET_BIR, TablaValores.get("BIR").toString());
			CD.getDescription().add(CT_BIR);
			}
			
			
			if (paciente.getValue().get("DESC")!=null)
			{
			CompleteTextElement CT_DESC=new CompleteTextElement(CET_DESC, TablaValores.get("DESC").toString());
			CD.getDescription().add(CT_DESC);
			}
			
			
			CompleteTextElement CT_ID=new CompleteTextElement(CET_ID, TablaValores.get("ID").toString());
			CD.getDescription().add(CT_ID);
			
			CompleteTextElement CT_URL=new CompleteTextElement(CET_URL, TablaValores.get("URL").toString());
			CD.getDescription().add(CT_URL);
			
			if (TablaValores.get("GEN")!=null)
			{
			CompleteTextElement CT_GEN=new CompleteTextElement(CET_GENDER, TablaValores.get("GEN").toString());
			CD.getDescription().add(CT_GEN);
			}
			
			if (TablaValores.get("NAME")!=null)
			{
				List<HashMap<String, Object>> tablaNombre=(List<HashMap<String, Object>>) TablaValores.get("NAME");
				for (int i = 0; i < tablaNombre.size(); i++) {
					
					HashMap<String, Object> ACTNAMETABLE = tablaNombre.get(i);
					 
					CompleteTextElementType NAME_FAM_ACT = _NAME_FAMILIA.get(i);
					if (ACTNAMETABLE.get("FAM")!=null)
					{
					CompleteTextElement CT_FAM=new CompleteTextElement(NAME_FAM_ACT, ACTNAMETABLE.get("FAM").toString());
					CD.getDescription().add(CT_FAM);
					}
					
					CompleteTextElementType NAME_USO_ACT = _NAME_USO.get(i);
					if (ACTNAMETABLE.get("USE")!=null)
					{
					CompleteTextElement CT_USE=new CompleteTextElement(NAME_USO_ACT, ACTNAMETABLE.get("USE").toString());
					CD.getDescription().add(CT_USE);
					}
					
					if (ACTNAMETABLE.get("GIV")!=null)
						{
						List<String> givennames=(List<String>) ACTNAMETABLE.get("GIV");
						List<CompleteTextElementType>  NAME_GIVEN_ACT = _NAME_GIVEN.get(i);
						for (int j = 0; j < givennames.size(); j++) {
							CompleteTextElementType NAME_GIVEN_ACT_ACT = NAME_GIVEN_ACT.get(j);

							CompleteTextElement CT_GIV=new CompleteTextElement(NAME_GIVEN_ACT_ACT, givennames.get(j));
							CD.getDescription().add(CT_GIV);

						}
						
						}
					
					
					
				}
				
			}
			
		}
	}

	private void cargaPacientes(String URLBase, int limit) {
		if (debugfile&& (new File("pacientes.json").exists()))
		{
			System.out.println("//////////Leido archivo de pacientes");
			
			String actual= "pacientes.json";
			int indice=1;
			while ((new File(actual).exists()))
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
				
				actual= "pacientes.json."+indice+".json";
				indice++;
				
				JsonElement JSONELEM = new JsonParser().parse(SB.toString());
				
					
				processJSON(JSONELEM.getAsJsonObject());
				
			}
		}else
		{
			
			if (debugfile)
				System.out.println("//////////generado archivo de pacientes");
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put("_include", "Patient:link");
			parameters.put("_format", "json");
			parameters.put("_pretty", "true");
			
			
			StringBuffer querryBuffer= new StringBuffer();
			
			try {
			querryBuffer.append(URLBase);
			querryBuffer.append("/Patient?");
			
				querryBuffer.append(ParameterStringBuilder.getParamsString(parameters));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			String ActualURL = querryBuffer.toString();
			int conteoFile=0;

			while (ActualURL!=null &&!ActualURL.isEmpty() && conteoFile<limit)
			
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
							
							
							
						String filename = "pacientes.json";
						if (conteoFile>0)
							filename = "pacientes.json."+conteoFile+".json";
						
						
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

				
				ActualURL=next_link;
				
				
				processJSON(JSONELEM.getAsJsonObject());
				
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

	private void processJSON(JsonObject asJsonObject) {
		JsonElement ENTRYNEXT = asJsonObject.get("entry");
		JsonArray ENTRYNEXT_Array=ENTRYNEXT.getAsJsonArray();
		
		for (int i = 0; i < ENTRYNEXT_Array.size(); i++) {
			
			
			
			
			JsonObject paciente=ENTRYNEXT_Array.get(i).getAsJsonObject();
			
			HashMap<String, Object> Propiedades=new HashMap<String, Object>();
			
			Propiedades.put("URL", paciente.get("fullUrl").getAsJsonPrimitive().getAsString());
			
			JsonObject Recurso = paciente.get("resource").getAsJsonObject();
			
			String ID = Recurso.get("id").getAsJsonPrimitive().getAsString();
			
			Propiedades.put("ID", ID);
			
			
			StringBuffer DESC= new StringBuffer();
			
			DESC.append(ID);
			DESC.append("  ");
			
			if (Recurso.get("text")!=null)
			{
				
				if (Recurso.get("text").getAsJsonObject().get("status")!=null)
					{
					DESC.append(Recurso.get("text").getAsJsonObject().get("status").getAsJsonPrimitive().getAsString());
					DESC.append("  ");
					}
				
				if (Recurso.get("text").getAsJsonObject().get("div")!=null)
					{
					DESC.append(Recurso.get("text").getAsJsonObject().get("div").getAsJsonPrimitive().getAsString());
					DESC.append("  ");
					}
					
			}
			
			Propiedades.put("DESC", DESC.toString());
			
			if (Recurso.get("name")!=null)
			{
				JsonArray Name_arra=Recurso.get("name").getAsJsonArray();
				LinkedList<HashMap<String,Object>> ListaNombres=
						new LinkedList<HashMap<String,Object>>();
				
				if (Name_arra.size()>MAXIMO_name)
					MAXIMO_name=Name_arra.size();
				
				
				for (int j = 0; j < Name_arra.size(); j++) {
					JsonObject nameUni=Name_arra.get(j).getAsJsonObject();
					HashMap<String,Object> namevalue=new HashMap<String,Object>();
					ListaNombres.add(namevalue);
					if (nameUni.get("use")!=null)
						namevalue.put("USE", nameUni.get("use").getAsJsonPrimitive().getAsString());
					if (nameUni.get("family")!=null)
						namevalue.put("FAM", nameUni.get("family").getAsJsonPrimitive().getAsString());
					if (nameUni.get("given")!=null)
					{
						LinkedList<String> givenames=new LinkedList<String>();
						JsonArray Name_given_arra=nameUni.get("given").getAsJsonArray();
						
						if (Name_given_arra.size()>MAXIMO_name_given)
							MAXIMO_name_given=Name_given_arra.size();
						
						for (int k = 0; k < Name_given_arra.size(); k++) 
							givenames.add(Name_given_arra.get(k).getAsJsonPrimitive().getAsString());
						
						namevalue.put("GIV", givenames);
						
					}
					
					
				}
				
				Propiedades.put("NAME", ListaNombres);
			}
			
			if (Recurso.get("gender")!=null)
				Propiedades.put("GEN", Recurso.get("gender").getAsJsonPrimitive().getAsString());
			
			if (Recurso.get("birthDate")!=null)
				Propiedades.put("BIR", Recurso.get("birthDate").getAsJsonPrimitive().getAsString());
			
			
			
		
			if (debugfile)
			{
			System.out.println("///////Paciente START");
			
			for (Entry<String, Object> jsonElement : Propiedades.entrySet()) {
				
				if (jsonElement.getValue() instanceof String)
					System.out.println(jsonElement.getKey()+":"+jsonElement.getValue().toString());
				
				if (jsonElement.getValue() instanceof List)
					{
					System.out.println(jsonElement.getKey()+":");
					processInternal((List)jsonElement.getValue(), 1);
					}
			}
			
			
			System.out.println("///////Paciente END");
			}
			
			
			PACIENTES.put(ID,Propiedades);
			
		}
		
	}

	private void processInternal(List value, int tabscont) {

		for (Object elementoin : value) {
			
			
			if (elementoin instanceof String)
			{	
				for (int i = 0; i < tabscont; i++) 
					System.out.print(TAB_ESPACE_CONST);
				
				System.out.println(elementoin);
			}
			
			if (elementoin instanceof HashMap)
			{
				for (int i = 0; i < tabscont; i++) 
					System.out.print(TAB_ESPACE_CONST);
				
				System.out.println("[");
		
				for (Entry<String, Object> jsonElement : ((HashMap<String, Object>)elementoin).entrySet()) {
					
					for (int i = 0; i < tabscont; i++) 
						System.out.print(TAB_ESPACE_CONST);
					
					if (jsonElement.getValue() instanceof String)
						System.out.println(jsonElement.getKey()+":"+jsonElement.getValue().toString());
					
					if (jsonElement.getValue() instanceof List)
						{
						System.out.println(jsonElement.getKey()+":");
						processInternal((List)jsonElement.getValue(), tabscont+1);
						}

					
				}
				
				for (int i = 0; i < tabscont; i++) 
					System.out.print(TAB_ESPACE_CONST);
				
				System.out.println("]");
				
			}
		}
		
	}

	public CompleteCollection getColeccion() {
		return Collection;
	}
	
	public static void main(String[] args) {
		CollectionFHIR_COPIA C=new CollectionFHIR_COPIA();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaFHIR("http://hapi.fhir.org/baseR4", log,10);
		
//		 try {
//				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
//				
//				System.out.println(FileIO);
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
