package com.wamteavm.models

import com.wamteavm.utilities.ColorWrapper

class NodeEdgeHandler(val animation: Animation) {

    fun init() {
        animation.nodeCollections.forEach { it.init() }
        updateNodeCollections()
    }

    private fun newNodeCollection(nodeCollectionSetPoint: NodeCollectionSetPoint) : NodeCollection {
        val newNodeCollection = NodeCollection(NodeCollectionID(animation.nodeCollectionID))
        animation.nodeCollectionID++
        newNodeCollection.alpha.newSetPoint(nodeCollectionSetPoint.time, 1f)
        newNodeCollection.interpolator.newSetPoint(nodeCollectionSetPoint.time, mutableListOf(nodeCollectionSetPoint))
        newNodeCollection.color.newSetPoint(nodeCollectionSetPoint.time, ColorWrapper.parseString("red")!!)
        newNodeCollection.init()
        return newNodeCollection
    }

    fun addNode(node: Node)
    {
        animation.nodes.add(node)
        animation.nodeId++
    }

    fun deleteNode(removeNode: Node, redirectEdge: Boolean): Boolean
    {
        removeNode.posInterpolator.setPoints.keys.forEach {
            removeNodeAt(removeNode, it, redirectEdge)
        }
        return true
    }

    fun removeNodeAt(removeNode: Node, time: Int, redirectEdge: Boolean): Boolean
    {
        if (redirectEdge) {
            return removeNodeAt(removeNode, time)
        } else {
            animation.nodes.forEach { node ->
                node.edges.forEach {
                    if (it.segment.second.value == removeNode.id.value) {
                        it.times.remove(time)
                    }
                }
                node.edges.removeIf { it.times.isEmpty() }
            }
            removeNode.edges.clear()
            val result = animation.nodes.remove(removeNode)
            updateNodeCollections()
            return result
        }
    }

    private fun removeNodeAt(removeNode: Node, time: Int): Boolean
    {
        // Redirect edges that point to the node to the next node in the Node Collection, or delete if that does not exist

        animation.nodes.forEach { node ->
            node.edges.filter { it.segment.second.value == removeNode.id.value && time in it.times }.forEach { edge ->
                val matchingEdge = removeNode.edges.find { it.collectionID.value == edge.collectionID.value && time in it.times }
                if (matchingEdge != null) {
                    edge.segment = Pair(node.id, matchingEdge.segment.second)
                } else {
                    edge.times.remove(time)
                    if (edge.times.isEmpty()) {
                        node.edges.remove(edge)
                    }
                }
            }
        }
        removeNode.edges.clear()
        val result = animation.nodes.remove(removeNode)
        updateNodeCollections()
        return result
    }

    fun addEdge(fromNode: Node, toNode: Node, time: Int, id: Int) {
        println(fromNode.edges.map { it.times.toString() })
        if (!fromNode.edges.any { it.collectionID.value == id && time in it.times}) { // Adding an edge from a node that already has an edge with the same collectionID is not allowed
            fromNode.edges.add(
                Edge(
                    NodeCollectionID(id),
                    Pair(fromNode.id, toNode.id),
                    mutableSetOf(time)
                )
            )
        } else {
            println("Edge not added")
        }

        updateNodeCollections()
    }

    fun removeEdge(removeEdge: Edge) : Boolean {
        var removed = false
        for (node in animation.nodes) {
            if (node.edges.remove(removeEdge)) {
                removed = true
            }
        }

        updateNodeCollections()
        return removed
    }

    fun deleteNodeCollectionSetPoint() {

    }

    private fun traverse(node: Node, nodeCollections: MutableList<NodeCollectionSetPoint>, currentBranch: NodeCollectionSetPoint) {
        val visited = (node.parents.find { (it.first == currentBranch.time && it.second.value == currentBranch.id.value) } != null)
        if (visited) {
            val matchingNodeCollections = nodeCollections.filter { it.id.value == currentBranch.id.value }
            for (nodeCollection in matchingNodeCollections) {
                if (node.id.value == nodeCollection.nodes.first().id.value) { // If the current node is the first node of an existing Node Collection with the same CollectionID, this branch is part of that Node Collection, so add this branch at the beginning of it
                    nodeCollection.nodes.addAll(0, currentBranch.nodes)
                    return
                }
            }
            if (currentBranch.nodes.isNotEmpty()) {
                if (node.id.value == currentBranch.nodes.first().id.value) { // If the current node is the first node of the current branch, it is forming a loop, so add it to the list
                    nodeCollections.add(currentBranch.apply { nodes.add(node) })
                    return
                }
                nodeCollections.add(currentBranch)
                println("Warning: Ambiguous topology")
            }
            return
        }

        var reachedEnd = true

        node.parents.add(Pair(currentBranch.time, currentBranch.id))

        for (edge in node.edges) { // Traverses every available edge from the node
            if (currentBranch.time in edge.times) {
                val nextNode = animation.getNodeByID(edge.segment.second)!!
                if (edge.collectionID.value == currentBranch.id.value) { // If edge continues the Node Collection that is being constructed, then continue recursion with this branch
                    if (nextNode.timeDefined(currentBranch.time)) {
                        reachedEnd = false
                        traverse(nextNode, nodeCollections, currentBranch.apply { nodes.add(node) })
                    } else {
                        edge.times.remove(currentBranch.time)
                    }
                }
            }
        }

        if (reachedEnd && currentBranch.nodes.isNotEmpty()) { // If no edges continue the Node Collection that is being constructed, that means the end has been reached, so add the current branch and stop
            nodeCollections.add(currentBranch.apply { nodes.add(node) })
        }
    }

    fun updateNodeCollections() {
        val nodeCollectionSetPoints = mutableListOf<NodeCollectionSetPoint>()

        animation.nodes.forEach { node ->
            node.edges.forEach {
                it.updateCoords(animation)
            }
            node.parents.clear()
        }
        for (node in animation.nodes) { // Build all node collections in all time
            for (time in node.posInterpolator.setPoints.keys) {
                for (edge in node.edges) {
                    traverse(
                        node,
                        nodeCollectionSetPoints,
                        NodeCollectionSetPoint(time, edge.collectionID)
                    )
                }
            }
        }

        nodeCollectionSetPoints.removeIf { it.nodes.isEmpty() }

        val nodeCollectionSetPointss = sortedMapOf<Int, MutableList<NodeCollectionSetPoint>>()
        for (i in 0..<nodeCollectionSetPoints.size) {
            val nodeCollectionSetPoint = nodeCollectionSetPoints[i]
            if (nodeCollectionSetPointss[nodeCollectionSetPoint.id.value] == null) {
                nodeCollectionSetPointss[nodeCollectionSetPoint.id.value] = mutableListOf(nodeCollectionSetPoint)
            } else {
                nodeCollectionSetPointss[nodeCollectionSetPoint.id.value]!!.add(nodeCollectionSetPoint)
            }
        }

        for (nodeCollectionSetPoints in nodeCollectionSetPointss) {
            var existingNodeCollection = animation.getNodeCollectionOrNull(nodeCollectionSetPoints.value.first().id)
            if (existingNodeCollection == null) { // Create new node collection if it does not exist
                existingNodeCollection = newNodeCollection(nodeCollectionSetPoints.value.first())
                animation.nodeCollections.add(existingNodeCollection)
            }
            existingNodeCollection.interpolator.setPoints.clear()
            for (nodeCollectionSetPoint in nodeCollectionSetPoints.value) {
                nodeCollectionSetPoint.updateInterpolators()
                val existingSetPoint = existingNodeCollection.interpolator.setPoints[nodeCollectionSetPoint.time]
                if (existingSetPoint == null) {
                    existingNodeCollection.interpolator.newSetPoint(
                        nodeCollectionSetPoint.time,
                        mutableListOf(nodeCollectionSetPoint)
                    )
                    //println("created new set point")
                } else {
                    //println("added to existing set point")
                    existingSetPoint.add(nodeCollectionSetPoint)
                }
            }
        }

        animation.drawer.updateDrawOrder(animation)
    }

    fun update(time: Int) {
        animation.nodes.forEach { it.update(time) }
        animation.nodeCollections.forEach { it.update(time, animation.camera().zoomInterpolator.value) }
    }

    fun insert(at: Node, node: Node, time: Int) {
        at.parents.forEach {
            val parent = animation.getNodeCollection(it.second)
            parent.getSetPointOfNode(at, time)?.insert(at, node)
        }
        updateNodeCollections()
    }
}
