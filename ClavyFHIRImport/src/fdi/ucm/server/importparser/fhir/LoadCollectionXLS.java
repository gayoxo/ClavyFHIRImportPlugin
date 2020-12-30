package fdi.ucm.server.importparser.fhir;
/**
 * 
 */


import java.util.ArrayList;

import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.LoadCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;

/**
 * @author Joaquin Gayoso-Cabada
 *
 */
public class LoadCollectionXLS extends LoadCollection{

	private static ArrayList<ImportExportPair> Parametros;
	
	
	public LoadCollectionXLS() {
		super();
	}
	
	@Override
	public CompleteCollectionAndLog processCollecccion(
			ArrayList<String> dateEntrada) {
		

		 ArrayList<String> Log = new ArrayList<String>();
		 CollectionFHIR C=null;
		 if (dateEntrada.size()>0 && !dateEntrada.get(0).isEmpty())
			{ 
			String fileName = dateEntrada.get(0);
			 System.out.println(fileName);
			 C = new CollectionFHIR();
			 C.procesaFHIR(fileName,Log);
			}
			else
			{
				if (dateEntrada.size()<=0)
						Log.add("Error: Numero de Elementos de entrada invalidos");
				if (dateEntrada.get(0).isEmpty()) 
					Log.add("Error: Direccion de FHIR incorrecta");
			}
		 

		 return new CompleteCollectionAndLog(C.getColeccion(),Log);
	}

	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.Text, "URL BASE FHIR"));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public String getName() {
		return "FHIR";
	}

	@Override
	public boolean getCloneLocalFiles() {
		return false;
	}

}
