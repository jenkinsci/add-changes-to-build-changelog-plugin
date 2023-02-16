package io.jenkins.plugins.addchangestobuildchangelog;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Json Object for a path inside a change.
 */
public class CustomChangePath {
	private String path;
	private boolean modified;
	private boolean added;
	private boolean deleted;
	
	public CustomChangePath(
		String path,
		boolean modified,
		boolean added,
		boolean deleted) {
		this.path = path;
		this.modified = modified;
		this.added = added;
		this.deleted = deleted;
	}
	
	@Override
	public String toString() {
		// not really for sure what this corresponds to, but I didn't need it for my purposes
		// update if you need it
		String uniqueString = RandomStringUtils.randomNumeric(40);
		String prefix = "";
		if(modified) {
			prefix = "M";
		} else if(added) {
			prefix = "A";
		} else if(deleted) {
			prefix = "D";
		}
		
		return ":000000 000000 " + uniqueString + " " + uniqueString + " " + prefix + "\t" + path;
	}
}
