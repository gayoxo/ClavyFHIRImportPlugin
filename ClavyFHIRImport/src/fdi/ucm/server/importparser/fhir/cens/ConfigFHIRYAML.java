package fdi.ucm.server.importparser.fhir.cens;

import java.util.LinkedList;
import java.util.List;

public class ConfigFHIRYAML {

	private List<ConfigFHIRYAMLBrowse> browseable;

	private List<ConfigFHIRYAMLlink> link;

	public List<ConfigFHIRYAMLBrowse> getBrowseable() {
		return browseable;
	}

	public void setBrowseable(List<ConfigFHIRYAMLBrowse> browseable) {
		this.browseable = browseable;
	}

	public List<ConfigFHIRYAMLlink> getLink() {
		return link;
	}

	public void setLink(List<ConfigFHIRYAMLlink> link) {
		this.link = link;
	}
	
	public ConfigFHIRYAML() {
		browseable=new LinkedList<ConfigFHIRYAMLBrowse>();
		link = new LinkedList<ConfigFHIRYAMLlink>();
	}

	public ConfigFHIRYAML(List<ConfigFHIRYAMLBrowse> browseable, List<ConfigFHIRYAMLlink> link) {
		super();
		this.browseable = browseable;
		this.link = link;
	}
	
	
	
	
}
