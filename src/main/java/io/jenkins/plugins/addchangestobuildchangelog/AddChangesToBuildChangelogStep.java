package io.jenkins.plugins.addchangestobuildchangelog;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.Util;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.AncestorInPath;
import jenkins.tasks.SimpleBuildStep;
import java.io.IOException;
import java.lang.StackTraceElement;
import org.jenkinsci.Symbol;
import java.lang.IllegalArgumentException;

/**
 * Build step for adding Custom Changes to the Build Changelog via String or File.
 */
public class AddChangesToBuildChangelogStep extends Builder implements SimpleBuildStep {
	/**
     * Path to the Changelog in the workspace.
     */
	private final String changelogPath;
	
	/**
     * String version of the changelog.
     */
	private final String changelogText;
	
	@DataBoundConstructor
	public AddChangesToBuildChangelogStep(
		String changelogPath,
		String changelogText) {
		this.changelogPath = changelogPath;
		this.changelogText = changelogText;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	public String getChangelogText() {
		return changelogText;
	}

	public String getChangelogPath() {
		return changelogPath;
	}

	private void printException(Exception e, TaskListener listener) {
		listener.getLogger().println(e.getMessage());
		for(StackTraceElement trace : e.getStackTrace()) {
		  listener.getLogger().println(trace.toString());
		}
	}
	
	@Override
	public void perform(
		Run<?, ?> run, 
		FilePath workspace, 
		EnvVars env, 
		Launcher launcher, 
		TaskListener listener) throws InterruptedException, IOException {
		try {
			// If we didn't define a string version, read in the workspace file.
			String text = Util.fixEmptyAndTrim(changelogText);
			if(text == null) {
				text = workspace.child(changelogPath).readToString();
			}
		
			// Perform the addition.
			AddChangesToBuildChangelog addChanges = new AddChangesToBuildChangelog();
			addChanges.perform(text, run, workspace, listener);
			
			listener.getLogger().println("Done");
		} catch(IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Symbol("addchangestobuildchangelog")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		@Override
		public String getDisplayName() {
		  return "Add Changes to Build Changelog";
		}
		
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
	}

}
