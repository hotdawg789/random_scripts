import jenkins.model.*
import hudson.model.*

// Function to print job details
def printJobDetails(job) {
    println "Job: ${job.fullDisplayName}"
    println "Keep Build Records: ${job.getBuildDiscarder()}"
    if (job.getBuildDiscarder() != null) {
        println "Days to keep builds: ${job.getBuildDiscarder().getStrategy().getDaysToKeepStr()}"
        println "Max # of builds to keep: ${job.getBuildDiscarder().getStrategy().getNumToKeepStr()}"
        println "Days to keep artifacts: ${job.getBuildDiscarder().getStrategy().getArtifactDaysToKeepStr()}"
        println "Max # of artifacts to keep: ${job.getBuildDiscarder().getStrategy().getArtifactNumToKeepStr()}"
    } else {
        println "No build discard strategy is set."
    }
    println "----------------------------------"
}

// Function to process jobs
def processJob(job) {
    if (job instanceof hudson.model.AbstractProject) {
        printJobDetails(job)
    } else if (job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob) {
        printJobDetails(job)
    } else if (job instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
        job.getItems().each { branchJob ->
            processJob(branchJob)
        }
    } else if (job instanceof com.cloudbees.hudson.plugins.folder.Folder) {
        job.getItems().each { folderJob ->
            processJob(folderJob)
        }
    }
}

// Iterate over all jobs in Jenkins
Jenkins.instance.getAllItems(Job.class).each { job ->
    processJob(job)
}

println "Finished checking job configurations."
