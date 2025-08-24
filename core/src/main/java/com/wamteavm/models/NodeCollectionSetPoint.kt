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

            if (nodes.first().tSetPoints.setPoints[time] == null) {
                tSetPoints[0] = 0.0
            }

            for (index in 0..<nodes.size - 1) {
                val node = nodes[index]

                val lastDefinedTime = node.tSetPoints.setPoints.keys.lastOrNull { it <= time }
                println(lastDefinedTime)
                val setPointValue = node.tSetPoints.setPoints[lastDefinedTime]?.get(id.value)
                if  (setPointValue != null) {
                    tSetPoints[index] = setPointValue
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

            tSetPoints[nodes.size - 1] = nodes.last().tSetPoints.setPoints[time]?.get(id.value) ?: 1.0
        }

        tInterpolator.i = tSetPoints.keys.toTypedArray()
        tInterpolator.o = tSetPoints.values.toTypedArray()

        for ((index, distance) in distances.withIndex()) {
            distanceMap[distance] = tInterpolator.evaluate(index)
        }

        distanceInterpolator.i = distanceMap.keys.toTypedArray()
        distanceInterpolator.o = distanceMap.values.toTypedArray()

        val tVals = mutableListOf<Double>()
        val xVals = mutableListOf<Double>()
        val yVals = mutableListOf<Double>()

        val coordinates = nodes.map { it.posInterpolator.setPoints[time]!! }

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

    fun contains(node: Node): Boolean {
        return nodes.any { it.id.value == node.id.value }
    }

    fun insert(at: Node, node: Node) { // Insert node after at
        val atEdge = at.edges.find { it.collectionID.value == id.value && it.times.contains(node.initTime) }
        at.edges.add(Edge(id.duplicate(), Pair(at.id.duplicate(), node.id.duplicate()), mutableListOf(node.initTime)))
        if (atEdge != null) {
            node.edges.add(Edge(id.duplicate(), Pair(node.id.duplicate(), atEdge.segment.second.duplicate()), ArrayList(node.posInterpolator.setPoints.keys)))
            atEdge.times.remove(node.initTime)
            nodes.add(nodes.indexOf(at), node)
        } else {
            nodes.add(node)
        }
    }

    fun duplicateAt(time: Int, animation: Animation) {
        val lastTime = animation.getNodeCollection(id).interpolator.setPoints.keys.last()
        for (node in nodes) {
            node.duplicateAt(time)
            node.edges.forEach {
                if (it.collectionID.value == id.value) {
                    if (it.times.contains(lastTime)) {
                        it.duplicateAt(time)
                    }
                }
            }
        }
    }

    fun delete(animation: Animation) {
        nodes.forEach {
            animation.nodeEdgeHandler.deleteNode(it, false)
        }
    }
}
