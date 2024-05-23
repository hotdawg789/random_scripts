import jenkins.model.*
import hudson.model.*
import java.util.concurrent.TimeUnit
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution

// Threshold for long-running jobs (24 hours in milliseconds)
def longRunningThreshold = TimeUnit.HOURS.toMillis(24)

// Function to print job details
def printJobDetails(job, build, duration) {
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

// Function to handle PlaceholderExecutable
def handlePlaceholderExecutable(executable, now) {
    def parentRun = executable.parentExecutable
    if (parentRun != null && parentRun instanceof Run) {
        def build = parentRun
        if (build.isBuilding()) {
            def job = build.getParent()
            def startTime = getBuildStartTime(build)
            if (startTime > 0) {
                def duration = now - startTime
                if (duration > longRunningThreshold) {
                    printJobDetails(job, build, duration)
                }
            }
        }
    }
}

// Iterate over all executors
def now = System.currentTimeMillis()
Jenkins.instance.computers.each { computer ->
    computer.executors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            if (executable instanceof Run) {
                def build = executable
                if (build.isBuilding()) {  // Check if the build is still running
                    def job = build.getParent()
                    def startTime = getBuildStartTime(build)
                    if (startTime > 0) {
                        def duration = now - startTime
                        if (duration > longRunningThreshold) {
                            printJobDetails(job, build, duration)
                        }
                    }
                }
            } else if (executable instanceof ExecutorStepExecution.PlaceholderTask.PlaceholderExecutable) {
                handlePlaceholderExecutable(executable, now)
            }
        }
    }

    computer.oneOffExecutors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            if (executable instanceof Run) {
                def build = executable
                if (build.isBuilding()) {  // Check if the build is still running
                    def job = build.getParent()
                    def startTime = getBuildStartTime(build)
                    if (startTime > 0) {
                        def duration = now - startTime
                        if (duration > longRunningThreshold) {
                            printJobDetails(job, build, duration)
                        }
                    }
                }
            } else if (executable instanceof ExecutorStepExecution.PlaceholderTask.PlaceholderExecutable) {
                handlePlaceholderExecutable(executable, now)
            }
        }
    }
}

println "Finished checking for long-running builds."
