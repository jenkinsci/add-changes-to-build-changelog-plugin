package io.jenkins.plugins.addchangestobuildchangelog;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.util.Secret;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.FreeStyleBuild;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.util.UUID;
import java.lang.IllegalArgumentException;
import org.apache.commons.io.FileUtils;
import hudson.scm.SCM;
import com.google.gson.Gson; 

/**
 * Implementation for adding Custom Changes to the Build Changelog via String or File.
 */
public class AddChangesToBuildChangelog {
	/**
	 * Entry point.
	 */
	public void perform(
		String json,
		Run<?, ?> run, 
		FilePath workspace, 
		TaskListener listener) throws Exception, IOException, IllegalArgumentException {
		String text = getChangeLogText(json);
		if(run instanceof WorkflowRun) {
			perfromAgainstWorkflowRun(text, (WorkflowRun)run, workspace, listener);
		} else if(run instanceof FreeStyleBuild) {
			perfromAgainstFreeStyleBuild(text, (FreeStyleBuild)run, workspace, listener);
		} else {
			throw new IllegalArgumentException("Only Pipeline and Freestyle jobs are supported at this time.");
		}
	}
	
	/**
	 * Creates a changelog file with a random postfix and writes it to the build directory.
	 */
	private String outputChangeLog(String text, WorkflowRun run) throws IOException {
		String buildDir = run.getArtifactsDir().getPath().replace("archive", "");
		String changelogPath = buildDir + "changelog" + UUID.randomUUID().toString();
		File f = new File(changelogPath);
		FileUtils.writeStringToFile(f, text);
		return changelogPath;
	}
	
	/**
	 * Performs the necessary logic against Pipeline jobs.
	 */
	private void perfromAgainstWorkflowRun(
		String text,
		WorkflowRun run, 
		FilePath workspace, 
		TaskListener listener) throws IOException, Exception {
		String changelogPath = outputChangeLog(text, run);
		org.jenkinsci.plugins.workflow.job.WorkflowRun.SCMListenerImpl scmListenerImpl = new org.jenkinsci.plugins.workflow.job.WorkflowRun.SCMListenerImpl();
		scmListenerImpl.onCheckout(run, new GitSCM(""), workspace, listener, new File(changelogPath), null);
	}
	
	/**
	 * Helpwer class to append  the text to the preexisting changelog.xml file.
	 */
	private void appendChangeLog(String text, FreeStyleBuild run) throws IOException,  IOException {
		String buildDir = run.getArtifactsDir().getPath().replace("archive", "");
		String changelogPath = buildDir + "changelog.xml";
		// WARNING:  The text you're appending must match what's defined in the configure screen.
		// Otherwise, nothing will show up.
		File f = new File(changelogPath);
		if(f.exists()) {
			text = FileUtils.readFileToString(f, "utf-8") + "\n" + text;
		} 
		
		FileUtils.writeStringToFile(f, text);
	}
	
	/**
	 * Performs the necessary logic against Freestyle Builds.
	 */
	private void perfromAgainstFreeStyleBuild(
		String text,
		FreeStyleBuild run, 
		FilePath workspace, 
		TaskListener listener) throws IOException, IllegalArgumentException, Exception {
		SCM scm = run.getProject().getScm();
		// Due to the way Freestyle builds work, they already define and do a checkout before any build steps run.
		// Thus, we can't just add changes to the changelog without making sure it matches what was defined earlier.
		// For Scm types of None, we can't add changes unless we change the configuration, which is way beyond what
		// this small plugin is going to do.
		if(scm instanceof hudson.scm.NullSCM) {
			throw new IllegalArgumentException("Scm cannot be None");
		} else if(!(scm instanceof GitSCM)) {
			throw new IllegalArgumentException("Only Git SCM is supported.");
		}
		
		// It seems like just appending or creating a new changelog.xml is all that's needed for FreestyleBuilds.
		// The configuration must get reloaded after the build step completes or something.
		appendChangeLog(text, run);
	}
	
	/**
	 * Convert changes json into a git changelog string
	 */
	private String getChangeLogText(String json) {
		Gson gson = new Gson();
		CustomChangeSet changes = gson.fromJson(json, CustomChangeSet.class);
		return changes.toString();
	}
}
