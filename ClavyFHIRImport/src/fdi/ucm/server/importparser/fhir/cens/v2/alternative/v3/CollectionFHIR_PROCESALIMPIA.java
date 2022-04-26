/**
 * 
 */
package fdi.ucm.server.importparser.fhir.cens.v2.alternative.v3;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.crypto.spec.GCMParameterSpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;

/**
 * @author jgayoso
 *
 */
public class CollectionFHIR_PROCESALIMPIA {

	public static CompleteCollection Apply(CompleteCollection c) {
		JSONParser jsonParser = new JSONParser();
		
		try {
			FileReader reader = new FileReader(new File("src/fdi/ucm/server/importparser/fhir/cens/v2/alternative/v3/FasePersonal-1.0.json"));
		
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			
			
			CompleteCollection nuevac=new CompleteCollection(c.getName(), c.getDescription());
			
			String g_nuevac = (String) obj.get("grammarname");
			String g_nuevac_des = (String) obj.get("description");
			String unionid = (String) obj.get("unionid");
			
		    CompleteElementType ID=null;
		    ID =find(c.getMetamodelGrammar(),unionid);
			
			CompleteGrammar CGFinal=new CompleteGrammar(g_nuevac,g_nuevac_des,nuevac);
			
			nuevac.getMetamodelGrammar().add(CGFinal);
				
			JSONArray g_nuevac_hijos = (JSONArray) obj.get("structure");
			
			HashMap<String, CompleteDocuments> listaCuadra=new HashMap<String, CompleteDocuments>();
			
			for (CompleteDocuments docucrea : c.getEstructuras()) {
				for (CompleteElement docuelem : docucrea.getDescription()) {
					if (docuelem.getHastype()==ID)
					{
						CompleteDocuments crear=new CompleteDocuments(nuevac, docucrea.getDescriptionText(), docucrea.getIcon());
						listaCuadra.put(((CompleteTextElement)docuelem).getValue(), crear);
					}
				}
			}
			
			procesaHijos(g_nuevac_hijos,CGFinal,c,listaCuadra);
			
			return nuevac;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return c;
	}

	private static CompleteElementType find(List<CompleteGrammar> metamodelGrammar, String unionid) {
		String[] splited = unionid.split("/");
		Queue<String> queue = new LinkedList<>(Arrays.asList(splited));
		String grammar = queue.poll();
		for (CompleteGrammar completeGrammar : metamodelGrammar) {
			if (grammar.equals(completeGrammar.getNombre()))
				return find(completeGrammar.getSons(),(Queue<String>) new LinkedList<String>(queue));
		}
		return null;
	}



	private static CompleteElementType find(List<CompleteElementType> sons, Queue<String> queue) {
		String elem=queue.poll();
		for (CompleteElementType celemEva : sons) {
			if (celemEva.getName().equals(elem))
				if (queue.isEmpty())
					return celemEva;
				else
					{ 
					CompleteElementType salida=find(celemEva.getSons(), new LinkedList<String>(queue));
							if (salida!=null)
								return salida;
					}
			
		}
		return null;
	}

	private static void procesaHijos(JSONArray g_nuevac_hijos, CompleteGrammar cGFinal, CompleteCollection c,
			HashMap<String, CompleteDocuments> listaCuadra) {
		for (int i = 0; i < g_nuevac_hijos.size(); i++) {
			JSONObject array_element = (JSONObject) g_nuevac_hijos.get(i);
			
			
			String name = (String) array_element.get("name");
			String equi=null;
			if (array_element.get("eq")!=null)
				equi = (String) array_element.get("eq");
			String multitype = null;
			if (array_element.get("multitype")!=null)
				multitype = (String) array_element.get("multitype");
			
			if (equi==null)
				cGFinal.getSons().add(new CompleteElementType(name,cGFinal));
			else
			{
				CompleteElementType equiElem=null;
				equiElem =find(c.getMetamodelGrammar(),equi);
			}
			
			
			
			
		}
		
	}



}
