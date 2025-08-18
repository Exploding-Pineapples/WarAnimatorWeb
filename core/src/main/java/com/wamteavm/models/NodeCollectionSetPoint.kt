package com.wamteavm.models

import com.wamteavm.interpolator.interpfunction.ComparableOInterpolationFunction
import com.wamteavm.interpolator.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolator.interpfunction.PCHIPInterpolationFunction
import kotlin.math.hypot

class NodeCollectionSetPoint(val time: Int, val id: NodeCollectionID, var nodes: MutableList<Node> = mutableListOf()) {
    var tInterpolator: LinearInterpolationFunction<Int> = LinearInterpolationFunction(arrayOf(0), doubleArrayOf(0.0))
    var xInterpolator: ComparableOInterpolationFunction<Double, Double> = PCHIPInterpolationFunction(arrayOf(0.0), doubleArrayOf(0.0))
    var yInterpolator: ComparableOInterpolationFunction<Double, Double> = PCHIPInterpolationFunction(arrayOf(0.0), doubleArrayOf(0.0))
    var distanceInterpolator: LinearInterpolationFunction<Double> = LinearInterpolationFunction(arrayOf(0.0), doubleArrayOf(0.0))
    var length: Double = 0.0

    init {
        updateInterpolators()
    }

    fun updateInterpolators() {
        val tSetPoints: LinkedHashMap<Int, Double> = linkedMapOf() //LinkedHashMap instead of SortedMap is ok because t values will always be inserted in order
        val distanceMap: LinkedHashMap<Double, Double> = linkedMapOf()
        var distances = DoubleArray(0)
        var totalDistance = 0.0

        if (nodes.isNotEmpty()) {
            distances = DoubleArray(nodes.size)

            if (nodes.first().tSetPoint == null) {
                tSetPoints[0] = 0.0
            }

            for (index in 0..<nodes.size - 1) {
                val node = nodes[index]

                if (node.tSetPoint != null) {
                    tSetPoints[index] = node.tSetPoint!!
                }

                distances[index] = totalDistance

                val nextNode = nodes[index + 1]
                totalDistance += hypot(
                    nextNode.position.x - node.position.x,
                    nextNode.position.y - node.position.y
                ).toDouble()
            }

            distances[nodes.size - 1] = totalDistance
            length = totalDistance

            tSetPoints[nodes.size - 1] = nodes.last().tSetPoint ?: 1.0
        }

        tInterpolator.i = tSetPoints.keys.toTypedArray()
        tInterpolator.o = tSetPoints.values.toTypedArray()

        for ((index, distance) in distances.withIndex()) {
            distanceMap[distance] = tInterpolator.evaluate(index)
        }

        distanceInterpolator.i = distanceMap.keys.toTypedArray()
        distanceInterpolator.o = distanceMap.values.toTypedArray()

        println(distanceMap.map { it })

        val tVals = mutableListOf<Double>()
        val xVals = mutableListOf<Double>()
        val yVals = mutableListOf<Double>()

        val coordinates = nodes.map { it.position }

        for (i in coordinates.indices) {
            tVals.add(tInterpolator.evaluate(i))
            xVals.add(coordinates[i].x.toDouble())
            yVals.add(coordinates[i].y.toDouble())
        }

        xInterpolator.i = tVals.toTypedArray()
        yInterpolator.i = tVals.toTypedArray()
        xInterpolator.o = xVals.toTypedArray()
        yInterpolator.o = yVals.toTypedArray()

        xInterpolator.init()
        yInterpolator.init()
    }

    fun tOfNode(node: Node): Double? {
        val index = nodes.indexOf(node)
        if (index == -1) {
            return null
        }
        return tInterpolator.evaluate(index)
    }

    fun insert(at: Node, node: Node) { // Insert node after at
        val atEdge = at.edges.find { it.collectionID.value == id.value }
        if (atEdge != null) {
            node.edges.add(Edge(id.duplicate(), Pair(node.id.duplicate(), atEdge.segment.second.duplicate())))
            atEdge.segment = Pair(at.id.duplicate(), node.id.duplicate())
        } else {
            at.edges.add(Edge(id.duplicate(), Pair(at.id.duplicate(), node.id.duplicate())))
        }

        nodes.add(nodes.indexOf(at), node)
    }

    fun duplicate(time: Int, animation: Animation): NodeCollectionSetPoint {
        // TODO this will not work with multiple NCs sharing one node because duplication will create new nodes for each one
        val newSetPoint = NodeCollectionSetPoint(time, NodeCollectionID(id.value))
        for (node in nodes) {
            newSetPoint.nodes.add(
                animation.createObjectAtPosition(time, node.position.x, node.position.y, Node::class.java)
                    .apply { tSetPoint = node.tSetPoint })
        }
        for (index in 0..<newSetPoint.nodes.size - 1) {
            val node = newSetPoint.nodes[index]
            val nextNode = newSetPoint.nodes[index + 1]
            node.edges.add(Edge(id.duplicate(), Pair(node.id.duplicate(), nextNode.id.duplicate())).apply { updateCoords(animation) })
        }
        return newSetPoint
    }

    fun delete(animation: Animation) {
        nodes.forEach {
            animation.nodeEdgeHandler.removeNode(it, false)
        }
    }
}
