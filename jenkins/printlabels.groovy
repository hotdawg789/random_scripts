import hudson.model.Job
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import java.util.Calendar

// List of node labels you are looking for
def nodeLabels = ['label1', 'label2', 'label3'] // Replace with your labels

// Function to check if a job is using any of the specified node labels
def usesAnyNodeLabel(Job job, List nodeLabels) {
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

// Function to check if the last build was within the last 6 months
def wasBuiltInLastSixMonths(Job job) {
    def lastBuild = job.getLastBuild()
    if (lastBuild == null) {
        return false
    }
    def sixMonthsAgo = Calendar.instance
    sixMonthsAgo.add(Calendar.MONTH, -6)
    return lastBuild.getTimeInMillis() > sixMonthsAgo.timeInMillis
}

// Iterate over all items in Jenkins and check for the node labels and last build date
Jenkins.instance.allItems.each { item ->
    // Check for all types of jobs including those inside folders
    if (item instanceof Folder) {
        item.getAllJobs().each {
            if (usesAnyNodeLabel(it, nodeLabels) && wasBuiltInLastSixMonths(it)) {
                println("${it.fullName} uses one of the labels: ${nodeLabels.join(', ')} and was built in the last 6 months")
            }
        }
    } else if (item instanceof Job) {
        if (usesAnyNodeLabel(item, nodeLabels) && wasBuiltInLastSixMonths(item)) {
            println("${item.fullName} uses one of the labels: ${nodeLabels.join(', ')} and was built in the last 6 months")
        }
    }
}
