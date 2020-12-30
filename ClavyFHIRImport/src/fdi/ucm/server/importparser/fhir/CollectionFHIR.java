package fdi.ucm.server.importparser.fhir;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;


public class CollectionFHIR {
	
	private CompleteCollection Collection;
	

	public void procesaFHIR(String URLBase, ArrayList<String> log) {

		
		try {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put("_include", "Patient:link");
			parameters.put("_format", "json");
			parameters.put("_pretty", "true");
			
			
			StringBuffer querryBuffer= new StringBuffer();
			
			querryBuffer.append(URLBase);
			querryBuffer.append("/Patient?");
			querryBuffer.append(ParameterStringBuilder.getParamsString(parameters));
			
			
			URL url = new URL(querryBuffer.toString());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			

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
					
					Reader streamReader = null;

					if (status > 299) {
					    streamReader = new InputStreamReader(con.getErrorStream());
					} else {
					    streamReader = new InputStreamReader(con.getInputStream());
					}
					
					if (streamReader!=null)
						log.add(streamReader.toString());
					
			con.disconnect();
			
		} catch (MalformedURLException e) {
			log.add(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.add(e.getMessage());
			e.printStackTrace();
		}
		
		
	}

	public CompleteCollection getColeccion() {
		return Collection;
	}
	
	public static void main(String[] args) {
		CollectionFHIR C=new CollectionFHIR();
		ArrayList<String> log = new ArrayList<String>();
		
		C.procesaFHIR("http://hapi.fhir.org/baseR4", log);
		
		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));

				oos.writeObject(C.getColeccion());

				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
