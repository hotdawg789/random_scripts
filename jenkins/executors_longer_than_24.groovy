import jenkins.model.*
import hudson.model.*
import java.util.concurrent.TimeUnit

// Threshold for long-running jobs (24 hours in milliseconds)
def longRunningThreshold = TimeUnit.HOURS.toMillis(24)
def now = System.currentTimeMillis()

// Function to print job details
def printJobDetails(job, build) {
    def duration = now - build.getStartTimeInMillis()
    println "Job: ${job.fullDisplayName}, Build: #${build.number}, Duration: ${duration / 1000 / 60 / 60} hours"
}

// Function to get the start time of different types of builds
def getBuildStartTime(build) {
    if (build instanceof Run) {
        return build.getStartTimeInMillis()
    } else if (build instanceof org.jenkinsci.plugins.workflow.job.WorkflowRun) {
        return build.getStartTimeInMillis()
    } else {
        return -1
    }
}

// Iterate over all executors
Jenkins.instance.computers.each { computer ->
    computer.executors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null && executable instanceof Run) {
            def build = executable
            def job = build.getParent()
            def startTime = getBuildStartTime(build)

            if (startTime > 0) {
                def duration = now - startTime
                if (duration > longRunningThreshold) {
                    printJobDetails(job, build)
                }
            }
        }
    }

    computer.oneOffExecutors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null && executable instanceof Run) {
            def build = executable
            def job = build.getParent()
            def startTime = getBuildStartTime(build)

            if (startTime > 0) {
                def duration = now - startTime
                if (duration > longRunningThreshold) {
                    printJobDetails(job, build)
                }
            }
        }
    }
}

println "Finished checking for long-running builds."
