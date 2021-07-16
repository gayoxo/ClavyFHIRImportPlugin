package fdi.ucm.server.importparser.fhir.cens;

public class ConfigFHIRYAMLlink {

	private String grammar;
	private String pathOrigen;
	
	public ConfigFHIRYAMLlink(String grammar, String pathOrigen) {
		super();
		this.grammar = grammar;
		this.pathOrigen = pathOrigen;
	}
	
	public ConfigFHIRYAMLlink() {
		this.grammar = "";
		this.pathOrigen = "";
		}

	public String getGrammar() {
		return grammar;
	}

	public void setGrammar(String grammar) {
		this.grammar = grammar;
	}

	public String getPathOrigen() {
		return pathOrigen;
	}

	public void setPathOrigen(String pathOrigen) {
		this.pathOrigen = pathOrigen;
	}
	
}
