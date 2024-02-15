// Loop through all nodes including the master
Jenkins.instance.nodes.each { node ->
    // Get the name of the node
    String nodeName = node.getDisplayName()
    // Get the number of executors for the node
    int executors = node.getNumExecutors()
    
    // Print node name and executor count
    println("${nodeName}: ${executors} executors")
}
