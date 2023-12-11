// List of node labels you are looking for
def nodeLabels = ['label1', 'label2', 'label3'] // Replace with your labels

// Function to check if a job is using any of the specified node labels
def usesAnyNodeLabel(job, nodeLabels) {
    def assignedNode = job.getAssignedLabelString()
    if (assignedNode != null) {
        nodeLabels.each { label ->
            if (assignedNode.contains(label)) {
                return true
            }
        }
    }
    return false
}

// Iterate over all items in Jenkins and check for the node labels
Jenkins.instance.items.each { item ->
    // Check for all types of jobs including those inside folders
    if (item.class.canonicalName == 'com.cloudbees.hudson.plugins.folder.Folder') {
        item.getAllJobs().each {
            if (usesAnyNodeLabel(it, nodeLabels)) {
                println("${it.fullName} uses one of the labels: ${nodeLabels}")
            }
        }
    } else {
        if (usesAnyNodeLabel(item, nodeLabels)) {
            println("${item.fullName} uses one of the labels: ${nodeLabels}")
        }
    }
}
