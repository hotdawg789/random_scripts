import hudson.model.Queue
import hudson.model.queue.QueueTaskFuture
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.workflow.job.WorkflowJob

// List of node labels you are looking for
def nodeLabels = ['label1', 'label2', 'label3'] // Replace with your labels

// Function to check if a job is intended to use any of the specified node labels
def isIntendedForAnyNodeLabel(Job job, List nodeLabels) {
    def assignedLabel = null

    if (job instanceof WorkflowJob) {
        // For WorkflowJob, the label might be in the definition property
        assignedLabel = job.assignedLabel?.name
    } else {
        // For other job types, use the assignedLabelString directly
        assignedLabel = job.assignedLabelString
    }

    if (assignedLabel != null) {
        nodeLabels.each { label ->
            if (assignedLabel.contains(label)) {
                return true
            }
        }
    }
    return false
}

// Get the build queue
Queue queue = Jenkins.instance.queue

// Iterate over all items in the build queue
queue.items.each { queueItem ->
    def task = queueItem.task
    if (task instanceof Job) {
        Job job = (Job) task
        if (isIntendedForAnyNodeLabel(job, nodeLabels)) {
            println("${job.fullName} is in the build queue and is intended for one of the labels: ${nodeLabels.join(', ')}")
        }
    }
}
