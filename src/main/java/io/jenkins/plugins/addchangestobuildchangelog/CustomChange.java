package io.jenkins.plugins.addchangestobuildchangelog;

import java.util.List;
import java.util.ArrayList;

/**
 * Json Object for a single change.
 */
public class CustomChange {
	private String commit;
	private String author;
	private String email;
	private String message;
	private String date;
	private List<CustomChangePath> paths;
	
	public CustomChange(
		String commit,
		String author,
		String email,
		String date,
		String message,
		List<CustomChangePath> paths) {
		this.commit = commit;
		this.author = author;
		this.email = email;
		this.message = message;
		this.date = date;
		this.paths = paths;
	}
	
	@Override
	public String toString() {
		List<String> lines = new ArrayList<String>();
		lines.add("commit " + commit);
		lines.add("author " + author + " <" + email + "> " + date);
		lines.add("committer " + author + " <" + email + "> " + date);
		lines.add("");
		lines.add("    " + message);
		lines.add("");
		lines.add("");
		lines.add(getPathStrings());
		return String.join("\n", lines);
	}
	
	private String getPathStrings() {
		List<String> lines = new ArrayList<String>();
		for(CustomChangePath path : paths) {
			lines.add(path.toString());
		}
		
		return String.join("\n", lines);
	}
}
