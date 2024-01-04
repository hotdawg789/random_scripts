// Access the Jenkins queue
def queue = Jenkins.instance.queue

// Make a copy of the items in the queue to avoid ConcurrentModificationException during removal
def items = queue.items.clone()

// Iterate through the items in the queue
items.each { item ->
    // Get the cause of blockage
    def cause = item.getWhy()

    // Check if the cause contains the specific message indicating no available nodes with the required label
    if (cause.contains("There are no nodes with the label")) {
        // Print information about the job being removed
        println "Cancelling queued job '${item.task.name}' because there are no nodes with the required label."
        
        // Cancel the queued job
        queue.cancel(item)
    }
}
