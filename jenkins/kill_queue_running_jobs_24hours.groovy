import java.util.concurrent.TimeUnit
import hudson.model.Executor
import hudson.model.Queue
import hudson.model.Result

// Define the duration threshold for running jobs and queued items (24 hours in milliseconds)
final long DURATION_THRESHOLD = TimeUnit.HOURS.toMillis(24)

// Get the current time
long currentTime = System.currentTimeMillis()

// Abort running jobs that exceed the duration threshold
Jenkins.instance.nodes.each { node ->
    node.toComputer().executors.each { Executor executor ->
        if (executor.isBusy()) {
            Queue.Executable currentExecutable = executor.currentExecutable
            long buildingSince = currentExecutable.getExecutor().startTimeInMillis
            long timeRunning = currentTime - buildingSince

            if (timeRunning > DURATION_THRESHOLD) {
                println "Terminating running job: ${currentExecutable.getParent().fullDisplayName}, Running Time: ${timeRunning / 1000 / 60 / 60} hours"
                executor.interrupt(Result.ABORTED)
            }
        }
    }
}

// Remove queued items that exceed the duration threshold
def queue = Jenkins.instance.queue
def items = queue.items.clone() // clone to avoid ConcurrentModificationException
items.each {
    long timeInQueue = currentTime - it.inQueueSince
    if (timeInQueue > DURATION_THRESHOLD) {
        println "Removing queued job: ${it.task.name}, Time in Queue: ${timeInQueue / 1000 / 60 / 60} hours"
        queue.cancel(it)
    }
}
