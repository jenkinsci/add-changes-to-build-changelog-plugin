package io.jenkins.plugins.addchangestobuildchangelog;

import java.util.List;
import java.util.ArrayList;

/**
 * Json Object for a collection of changes.
 */
public class CustomChangeSet {
	private List<CustomChange> changes;
	public CustomChangeSet(
		List<CustomChange> changes) {
		this.changes = changes;
	}
	
	@Override
	public String toString() {
		List<String> lines = new ArrayList<String>();
		for(CustomChange change : changes) {
			lines.add(change.toString());
		}
		
		return String.join("\n", lines);
	}
}
