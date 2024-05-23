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
    } else if (build instanceof WorkflowRun) {
        return build.getStartTimeInMillis()
    } else if (build instanceof MultiJobBuild) {
        return build.getStartTimeInMillis()
    } else {
        return -1
    }
}

// Function to handle PlaceholderExecutable
def handlePlaceholderExecutable(executable, now, longRunningThreshold, counter) {
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
                    counter++
                }
            }
        }
    }
}

// Counter to track long-running builds
def counter = 0

// Iterate over all executors
Jenkins.instance.computers.each { computer ->
    def now = System.currentTimeMillis()
    def longRunningThreshold = TimeUnit.HOURS.toMillis(24)
    
    computer.executors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            if (executable instanceof Run || executable instanceof WorkflowRun || executable instanceof MultiJobBuild) {
                def build = executable
                if (build.isBuilding()) {  // Check if the build is still running
                    def job = build.getParent()
                    def startTime = getBuildStartTime(build)
                    if (startTime > 0) {
                        def duration = now - startTime
                        if (duration > longRunningThreshold) {
                            printJobDetails(job, build, duration)
                            counter++
                        }
                    }
                }
            } else if (executable instanceof ExecutorStepExecution.PlaceholderTask.PlaceholderExecutable) {
                handlePlaceholderExecutable(executable, now, longRunningThreshold, counter)
            }
        }
    }

    computer.oneOffExecutors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            if (executable instanceof Run || executable instanceof WorkflowRun || executable instanceof MultiJobBuild) {
                def build = executable
                if (build.isBuilding()) {  // Check if the build is still running
                    def job = build.getParent()
                    def startTime = getBuildStartTime(build)
                    if (startTime > 0) {
                        def duration = now - startTime
                        if (duration > longRunningThreshold) {
                            printJobDetails(job, build, duration)
                            counter++
                        }
                    }
                }
            } else if (executable instanceof ExecutorStepExecution.PlaceholderTask.PlaceholderExecutable) {
                handlePlaceholderExecutable(executable, now, longRunningThreshold, counter)
            }
        }
    }
}

// Print criteria_not_met if no long-running builds were found
if (counter == 0) {
    println "criteria_not_met"
}

println "Finished checking for long-running builds."
