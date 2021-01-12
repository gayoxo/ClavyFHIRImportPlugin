/**
 * 
 */
package fdi.ucm.server.importparser.fhir.old;

import java.util.HashMap;

import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author PhD. Joaquin Gayoso
 *
 */
public class TypeObject {

	private String tname;
	private HashMap<String, CompleteDocuments> listaDocumentos;
	private CompleteGrammar tgramar;
	private HashMap<String, CompleteElementType> path_elem;

	public TypeObject(String tResource) {
		tname = tResource;
		tgramar = new CompleteGrammar(tResource, tResource, null);
		path_elem=new HashMap<String, CompleteElementType>();
		listaDocumentos=new HashMap<String, CompleteDocuments>();
	}

	public HashMap<String, CompleteDocuments> getListaDocumentos() {
		return listaDocumentos;
	}

	public void setListaDocumentos(HashMap<String, CompleteDocuments> listaDocumentos) {
		this.listaDocumentos = listaDocumentos;
	}

	public String getTname() {
		return tname;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}

	public CompleteTextElementType createElementType(String string) {
		CompleteElementType elemena = createElementTypeInternal(string);
		
	
		CompleteTextElementType s_elemena=null;
		

			if (elemena instanceof CompleteTextElementType)
				s_elemena=(CompleteTextElementType)elemena;
			else
			{
				
				s_elemena=new CompleteTextElementType(elemena.getName(),elemena.getFather(),
						tgramar);
				
				if (elemena.getFather()==null)
				{
						int posele=tgramar.getSons().indexOf(elemena);	
						if (posele>=0)
						{
						tgramar.getSons().remove(posele);
						tgramar.getSons().add(posele, s_elemena);
						
						for (CompleteElementType hijos : elemena.getSons()) {
							s_elemena.getSons().add(hijos);
							hijos.setFather(s_elemena);
							}
						
						}
				}else
				{
					int posele=elemena.getFather().getSons().indexOf(elemena);	
					if (posele>=0)
					{
						elemena.getFather().getSons().remove(posele);
						elemena.getFather().getSons().add(posele, s_elemena);
					
					for (CompleteElementType hijos : elemena.getSons()) {
						s_elemena.getSons().add(hijos);
						hijos.setFather(s_elemena);
						}
					
					}
				}
				
				
				
				
				
			
		}
		
		path_elem.put(string, s_elemena);
		return s_elemena;
		
		
	}

	private CompleteElementType createElementTypeInternal(String string) {
		
		CompleteElementType salida = path_elem.get(string);
		
		
		if (salida!=null)
			return salida;
		

		 salida=new CompleteElementType(string,tgramar);
		
		String[] path=string.split("\\\\");
		//AQUI PODEMOS GENERAR EL SISTEMA CON path \A\B\C
		
		if (path.length==1)
			tgramar.getSons().add(salida);
		
		else
			{
			StringBuffer patpre=new StringBuffer();
			CompleteElementType padre=null;
			for (int i = 0; i < path.length-1; i++) {
				patpre.append(path[i]);
				padre=createElementTypeInternal(patpre.toString());
			}
			}
		
		path_elem.put(string, salida);
		return salida;
	}

	public CompleteGrammar getTgramar() {
		return tgramar;
	}

	public void setTgramar(CompleteGrammar tgramar) {
		this.tgramar = tgramar;
	}

	public HashMap<String, CompleteElementType> getPath_elem() {
		return path_elem;
	}

	public void setPath_elem(HashMap<String, CompleteElementType> path_elem) {
		this.path_elem = path_elem;
	}
	
	

}
