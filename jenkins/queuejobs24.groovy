import java.util.concurrent.TimeUnit

// Access the Jenkins instance
def jenkinsInstance = Jenkins.instance

// Access the queue
def queue = jenkinsInstance.queue

// Define the duration threshold (24 hours in milliseconds)
final long DURATION_THRESHOLD = TimeUnit.HOURS.toMillis(24)

// Get the current time
long currentTime = System.currentTimeMillis()

// Iterate through the items in the queue
queue.items.each {
  // Calculate how long the item has been in the queue
  long timeInQueue = currentTime - it.inQueueSince

  // Check if the item has been in the queue for more than 24 hours
  if (timeInQueue > DURATION_THRESHOLD) {
    // Print out the job name and how long it's been in the queue (in hours)
    println "Job name: ${it.task.name}, Time in Queue: ${timeInQueue / DURATION_THRESHOLD} hours"
  }
}
