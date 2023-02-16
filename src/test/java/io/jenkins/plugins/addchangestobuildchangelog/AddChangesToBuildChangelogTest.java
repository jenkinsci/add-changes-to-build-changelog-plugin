package io.jenkins.plugins.addchangestobuildchangelog;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.scm.ChangeLogSet;
import hudson.model.Result;
import hudson.model.Cause;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Assert;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DecimalFormat;
import java.io.IOException;
import hudson.plugins.git.GitSCM;
import com.google.gson.Gson; 

/**
 * Test cases for adding Custom Changes to the Build Changelog via String or File.
 */
public class AddChangesToBuildChangelogTest {
	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	/**
	 * Test case where someone tries to add custom changes to a Freestyle that doesn't have SCM defined.
	 * This should result in an exception and build failure.
	 */
	@Test
	public void testFreestyleBuildWithNoScm() throws IOException, Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		AddChangesToBuildChangelogStep builder = new AddChangesToBuildChangelogStep(null, getChangeLogText());
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0, new Cause.UserIdCause()).get();
		jenkins.assertBuildStatus(Result.FAILURE, build);
		jenkins.assertLogContains("Scm cannot be None", build);
	}
	
	/**
	 * Test case where someone tries to add custom changes to a Freestyle that has Git SCM defined.
	 * This should add the changes to the Build Changelog.
	 */
	@Test
	public void testFreestyleBuildWithGitScm() throws IOException, Exception {
		// We need to checkout something at the beginning.
		// Let's use an empty repo I created for this purpose.
		GitSCM scm = new GitSCM("https://github.com/danielomoto/emptyrepo.git");
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.setScm(scm);
		
		// create our builder passing in the text version of 3 git commits.
		AddChangesToBuildChangelogStep builder = new AddChangesToBuildChangelogStep(null, getChangeLogText());
		project.getBuildersList().add(builder);
		FreeStyleBuild completedBuild = jenkins.buildAndAssertSuccess(project);
		
		// Test no failure.
		jenkins.assertLogContains("Done", completedBuild);
		
		// Make sure there are 3 commits in the changeSets.
		Assert.assertEquals(3, ((ChangeLogSet)completedBuild.getChangeSets().toArray()[0]).getItems().length);
	}
	
	/**
	 * Test case where someone tries to add custom changes to a pipeline.
	 * This should add the changes to the Build Changelog.
	 */
	@Test
	public void testSupportedScriptedPipelineWithCommits() throws IOException, Exception {
		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
		// Create the pipeline script and assign the entire changelog to the text variable. 
		// Note that I'm using triple quotes to allow the string to span multiple lines.
		String pipelineScript
				= "node {\n"
				+ "  def text = \"\"\"" + getChangeLogText() + "\"\"\"\n"
				+ "  addchangestobuildchangelog changelogText: text\n"
				+ "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
		WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
		
		// Test no failure.
		jenkins.assertLogContains("Done", completedBuild);
		
		// Make sure there are 3 commits in the changeSets.
		Assert.assertEquals(3, ((ChangeLogSet)completedBuild.getChangeSets().toArray()[0]).getItems().length);
	}
	
	/**
	 * Helper class to create 3 bogus commits in a changelog string
	 */
	private String getChangeLogText() throws Exception {
		List<CustomChange> commits = new ArrayList<CustomChange>();
		
		commits.add(new CustomChange(
			"659d00186d94581c05283afefe885a4ad5a186a8", 
			"Joe Smith",
			"joe@smith.com",
			"2023-02-01 13:35:03 +0530",
			"Hello",
			Arrays.asList(
				new CustomChangePath("/home/test.jpg", false, false, true),
				new CustomChangePath("/var/log.txt", false, true, false),
				new CustomChangePath("/user/foo.png", true, false, false)
			)
		));
		commits.add(new CustomChange(
			"5fa9113196c54518a3f91bbe2bfac46842af9cac", 
			"Jane Doe", 
			"jane@doe.com", 
			"2024-03-02 14:46:14 +0530", 
			"Bonjour", 
			new ArrayList<CustomChangePath>()
		));
		commits.add(new CustomChange(
			"26dede03e5dd169b0ccf5c0e5b9bc4bb9cbafdf2", 
			"Richard Doctor", 
			"richard@doctor.com", 
			"2024-03-02 14:46:14 +0530",
			"Konnichiwa", 
			Arrays.asList(
				new CustomChangePath("/soul/sister.png", true, false, false),
				new CustomChangePath("/holy/cow.png", true, false, false)
			)
		));
		
		CustomChangeSet set = new CustomChangeSet(commits);
		Gson gson = new Gson();
		return gson.toJson(set);
	}
}