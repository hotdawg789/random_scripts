// Access the Jenkins queue
def queue = Jenkins.instance.queue

// Iterate through the items in the queue
queue.items.each { item ->
    // Get the cause of blockage
    def cause = item.getWhy()

    // Print information about the job and why it is stuck
    println "Job '${item.task.name}' is stuck in the queue. Reason: ${cause}"
}
