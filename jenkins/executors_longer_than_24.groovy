import jenkins.model.*
import hudson.model.*
import java.util.concurrent.TimeUnit
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import com.tikal.jenkins.plugins.multijob.MultiJobBuild

// Function to print job details
def printJobDetails(job, build, duration) {
    println "Job: ${job.fullDisplayName}, Build: #${build.number}, Duration: ${duration / 1000 / 60 / 60} hours"
}

// Function to get the start time of different types of builds
def getBuildStartTime(build) {
    if (build instanceof Run) {
        return build.getStartTimeInMillis()
    } else {
        return -1
    }
}

// Function to handle PlaceholderExecutable
def handlePlaceholderExecutable(executable, now, longRunningThreshold, counter, processedBuilds) {
    def parentRun = executable.parentExecutable
    if (parentRun != null && parentRun instanceof Run && !processedBuilds.contains(parentRun)) {
        def build = parentRun
        if (build.isBuilding()) {
            def job = build.getParent()
            def startTime = getBuildStartTime(build)
            if (startTime > 0) {
                def duration = now - startTime
                if (duration > longRunningThreshold) {
                    printJobDetails(job, build, duration)
                    counter++
                    processedBuilds.add(build)
                }
            }
        }
    }
}

// Counter to track long-running builds
def counter = 0

// Set to track processed builds
def processedBuilds = new HashSet()

// Function to process builds in a job
def processJobBuilds(job, now, longRunningThreshold, counter, processedBuilds) {
    job.getBuilds().each { build ->
        if (build.isBuilding() && !processedBuilds.contains(build)) {
            def startTime = getBuildStartTime(build)
            if (startTime > 0) {
                def duration = now - startTime
                if (duration > longRunningThreshold) {
                    printJobDetails(job, build, duration)
                    counter++
                    processedBuilds.add(build)
                }
            }
        }
    }
}

// Function to process multibranch project builds
def processMultiBranchProjects(job, now, longRunningThreshold, counter, processedBuilds) {
    if (job instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
        job.getItems().each { branchProject ->
            processJobBuilds(branchProject, now, longRunningThreshold, counter, processedBuilds)
        }
    }
}

// Iterate over all jobs in Jenkins instance
def now = System.currentTimeMillis()
def longRunningThreshold = TimeUnit.HOURS.toMillis(24)

Jenkins.instance.getAllItems(Job.class).each { job ->
    if (job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob || 
        job instanceof com.tikal.jenkins.plugins.multijob.MultiJobProject || 
        job instanceof FreeStyleProject || 
        job instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
        
        processJobBuilds(job, now, longRunningThreshold, counter, processedBuilds)
        
        // Specifically handle multibranch projects
        processMultiBranchProjects(job, now, longRunningThreshold, counter, processedBuilds)
    }
}

// Print criteria_not_met if no long-running builds were found
if (counter == 0) {
    println "criteria_not_met"
}
