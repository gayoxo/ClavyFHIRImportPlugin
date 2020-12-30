package fdi.ucm.server.importparser.fhir;

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
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;


public class CollectionFHIR {
	
	private CompleteCollection Collection;
	public boolean debugfile=false;
	private ArrayList<String> log=new ArrayList<String>();
	

	public void procesaFHIR(String URLBase, ArrayList<String> log) {

		this.log=log;
		
		cargaPacientes(URLBase);
		
		
		
		
		
		
	}

	private void cargaPacientes(String URLBase) {
		if (debugfile&& (new File("pacientes.json").exists()))
		{
			System.out.println("//////////Leido archivo de pacientes");
			
			String actual= "pacientes.json";
			int indice=1;
			while ((new File(actual).exists()))
			{
				try {
					File myObj = new File(actual);
				      Scanner myReader = new Scanner(myObj);
				      System.out.println("//////////INICIO "+actual);
				      while (myReader.hasNextLine()) {
				        String data = myReader.nextLine();

				        System.out.println(data);
				      }
				      myReader.close();
				      System.out.println("//////////FIN "+actual);
				} catch (Exception e) {
					log.add(e.getMessage());
				}
				
				actual= "pacientes.json."+indice+".json";
				indice++;
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

			while (ActualURL!=null &&!ActualURL.isEmpty())
			
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
						next_link=obj.get("url").getAsJsonPrimitive().toString();
						System.out.println(next_link);
					}
					
				}

				
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

	public CompleteCollection getColeccion() {
		return Collection;
	}
	
	public static void main(String[] args) {
		CollectionFHIR C=new CollectionFHIR();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaFHIR("http://hapi.fhir.org/baseR4", log);
		
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
