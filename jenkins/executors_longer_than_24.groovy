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

// Iterate over all executors
Jenkins.instance.computers.each { computer ->
    computer.executors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            def build = executable.getParent()
            def job = build.getParent()
            def duration = now - build.getStartTimeInMillis()

            if (duration > longRunningThreshold) {
                printJobDetails(job, build)
            }
        }
    }

    computer.oneOffExecutors.each { executor ->
        def executable = executor.currentExecutable
        if (executable != null) {
            def build = executable.getParent()
            def job = build.getParent()
            def duration = now - build.getStartTimeInMillis()

            if (duration > longRunningThreshold) {
                printJobDetails(job, build)
            }
        }
    }
}

println "Finished checking for long-running builds."
