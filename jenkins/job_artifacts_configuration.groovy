import jenkins.model.*
import hudson.model.*

// Function to interpret and print the retention policy
def printRetentionPolicy(label, value) {
    if (value == -1) {
        println "${label}: No limit"
    } else {
        println "${label}: ${value}"
    }
}

// Function to print job details
def printJobDetails(job) {
    println "Job: ${job.fullDisplayName}"
    def logRotator = job.getBuildDiscarder()
    if (logRotator instanceof hudson.tasks.LogRotator) {
        printRetentionPolicy("Days to keep builds", logRotator.getDaysToKeep())
        printRetentionPolicy("Max # of builds to keep", logRotator.getNumToKeep())
        printRetentionPolicy("Days to keep artifacts", logRotator.getArtifactDaysToKeep())
        printRetentionPolicy("Max # of artifacts to keep", logRotator.getArtifactNumToKeep())
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
