package fdi.ucm.server.importparser.fhir.cens;

public class ConfigFHIRYAMLBrowse {

	private String grammar;
	private String path;
	
	public ConfigFHIRYAMLBrowse() {
		this.grammar = "";
		this.path = "";
	}

	public ConfigFHIRYAMLBrowse(String grammar, String path) {
		super();
		this.grammar = grammar;
		this.path = path;
	}

	public String getGrammar() {
		return grammar;
	}

	public void setGrammar(String grammar) {
		this.grammar = grammar;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
}
