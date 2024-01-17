import jenkins.model.Jenkins
import hudson.model.Queue
import hudson.model.Job
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject

// Access the Jenkins queue
def queue = Jenkins.instance.queue

// Make a copy of the items in the queue to avoid ConcurrentModificationException during removal
def items = queue.items.clone()

// Iterate through the items in the queue
items.each { item ->
    // Get the cause of blockage
    def cause = item.getWhy()

    // Get the intended node or label for the job, if any
    def assignedLabel = item.assignedLabel?.name ?: "none"

    // Check if the cause contains the specific message indicating no available nodes with the required label
    if (cause?.contains("There are no nodes with the label")) {
        // Print information about the job being removed and its assigned label
        println "Cancelling queued job '${item.task.name}' intended for node/label '${assignedLabel}' because there are no nodes with the required label."
        
        // Cancel the queued job
        queue.cancel(item)

        // Try to disable the job associated with the item
        if (item.task instanceof Job) {
            def job = item.task as Job
            disableJob(job)
        } else {
            println "Unable to disable '${item.task.name}' as it is not a recognizable Job type."
        }
    }
}

void disableJob(Job job) {
    if (job instanceof WorkflowJob) {
        // Handle Workflow Jobs, which may be part of Multibranch Pipelines
        def parent = job.parent
        if (parent instanceof WorkflowMultiBranchProject) {
            // Disable the specific branch job in a Multibranch Pipeline
            if (!job.isDisabled()) {
                job.setDisabled(true)
                println "Branch job '${job.fullName}' in Multibranch Pipeline '${parent.name}' has been disabled."
            }
        } else {
            // Standard Workflow Job (not part of a Multibranch Pipeline)
            disableStandardJob(job)
        }
    } else {
        // Handle other standard job types
        disableStandardJob(job)
    }
}

void disableStandardJob(Job job) {
    if (!job.isDisabled()) {
        job.disable()
        println "Job '${job.fullName}' has been disabled."
    }
}
