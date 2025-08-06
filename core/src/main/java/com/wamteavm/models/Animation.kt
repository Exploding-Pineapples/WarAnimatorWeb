package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.wamteavm.WarAnimator
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.MapLabel
import com.wamteavm.models.screenobjects.Unit
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Animation @JvmOverloads constructor(
    var name: String = "My Animation",
    var countries: List<String> = mutableListOf(),
    val units: MutableList<Unit> = mutableListOf(),
    private var camera: Camera? = null,
    val nodes: MutableList<Node> = mutableListOf(),
    val nodeCollections: MutableList<NodeCollection> = mutableListOf(),
    val arrows: MutableList<Arrow> = mutableListOf(),
    val mapLabels: MutableList<MapLabel> = mutableListOf(),
    var images: MutableList<Image> = mutableListOf(),
    var nodeCollectionID: Int = 0,
    var nodeId: Int = 0,
    val linesPerNode: Int = 12,
    var initTime: Int = 0
)
{
    @Transient var nodeEdgeHandler = NodeEdgeHandler(this)

    fun init() {
        nodeEdgeHandler = NodeEdgeHandler(this)
        nodeEdgeHandler.init()
        units.forEach { it.init() }
        mapLabels.forEach { it.init() }
        arrows.forEach { it.init() }
        images.forEach { it.init() }
        camera().init()
    }

    fun camera(): Camera
    {
        if (camera == null)
        {
            camera = Camera(Coordinate(WarAnimator.DISPLAY_WIDTH / 2f, WarAnimator.DISPLAY_HEIGHT / 2f), initTime)
        }

        return camera!!
    }

    fun deleteObject(obj: AnyObject): Boolean {
        if (obj.javaClass == Node::class.java) {
            return nodeEdgeHandler.removeNode(obj as Node)
        }
        if (obj.javaClass == Image::class.java) {
            return images.remove(obj as Image)
        }
        if (obj.javaClass == MapLabel::class.java) {
            return mapLabels.remove(obj as MapLabel)
        }
        if (obj.javaClass == Arrow::class.java) {
            return arrows.remove(obj as Arrow)
        }
        if (obj.javaClass == Unit::class.java) {
            return units.remove(obj as Unit)
        }
        if (obj.javaClass == Edge::class.java) {
            return nodeEdgeHandler.removeEdge(obj as Edge)
        }
        return false
    }

    fun getNodeCollection(id: NodeCollectionID): NodeCollection? {
        return nodeCollections.find { it.id.value == id.value }
    }

    fun getNodeByID(id: NodeID): Node? = nodes.firstOrNull { it.id.value == id.value }

    fun newNode(x: Float, y: Float, time: Int): Node {
        val node = Node(Coordinate(x, y), time, NodeID(nodeId))
        node.init()
        nodeEdgeHandler.addNode(node)
        return node
    }

    fun newArrow(x: Float, y: Float, time: Int): Arrow {
        val new = Arrow(Coordinate(x, y), time)
        new.init()
        arrows.add(new)
        return new
    }

    fun newMapLabel(x: Float, y: Float, time: Int): MapLabel {
        val new = MapLabel(Coordinate(x, y), time)
        new.init()
        mapLabels.add(new)
        return new
    }

    fun newImage(x: Float, y: Float, time: Int): Image {
        val new = Image(Coordinate(x, y), time, "")
        new.init()
        images.add(new)
        return new
    }

    fun newUnit(x: Float, y: Float, time: Int, image: String = ""): Unit {
        val new = Unit(Coordinate(x, y), time, image)
        new.init()
        units.add(new)
        return new
    }

    fun createObjectAtPosition(time: Int, x: Float, y: Float, type: String, country: String = ""): AnyObject? {
        if (type == "Unit") {
            return newUnit(x, y, time, country)
        }

        val objectDictionary = mapOf(
            "Node" to ::newNode,
            "Arrow" to ::newArrow,
            "Map Label" to ::newMapLabel,
            "Image" to ::newImage
        )
        return objectDictionary[type]?.invoke(x, y, time)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AnyObject> selectObjectWithType(x: Float, y: Float, time: Int, type: Class<out AnyObject>): ArrayList<T> {
        val objects = ArrayList<T>()

        if (type.isAssignableFrom(Node::class.java)) {
            objects.addAll(nodes.filter { time == it.initTime && it.clicked(x, y) }.map {it as T})
        }

        if (type.isAssignableFrom(Edge::class.java)) {
            nodes.filter { time == it.initTime }.forEach { node ->
                objects.addAll(node.edges.filter { it.clicked(x, y) }.map { it as T} )
            }
        }

        if (type.isAssignableFrom(Arrow::class.java)) {
            objects.addAll(arrows.filter { it.clicked(x, y) }.map {it as T} )
        }

        if (type.isAssignableFrom(MapLabel::class.java)) {
            objects.addAll(mapLabels.filter { it.clicked(x, y) }.map {it as T} )
        }

        if (type.isAssignableFrom(Image::class.java)) {
            objects.addAll(images.filter { it.clicked(x, y) }.map {it as T} )
        }

        if (type.isAssignableFrom(Unit::class.java)) {
            objects.addAll(units.filter { it.clicked(x, y) }.map { it as T} )
        }

        if (type.isAssignableFrom(NodeCollection::class.java)) {
            objects.addAll(nodeCollections.filter { it.clicked(x, y) }.map {it as T} )
        }

        return objects
    }

    fun getParents(node: Node) : List<NodeCollection> {
        return nodeCollections.filter {
                nodeCollection -> (nodeCollection.interpolator.setPoints[node.initTime]?.nodes?.find { it.id.value == node.id.value } != null)
        }
    }

    fun update(time: Int, orthographicCamera: OrthographicCamera, paused: Boolean) {
        camera().goToTime(time)
        nodeEdgeHandler.update(time, orthographicCamera, paused)
        units.forEach { it.goToTime(time, orthographicCamera.zoom, orthographicCamera.position.x, orthographicCamera.position.y, paused) }
        images.forEach { it.goToTime(time, orthographicCamera.zoom, orthographicCamera.position.x, orthographicCamera.position.y, paused) }
        arrows.forEach { it.goToTime(time, orthographicCamera.zoom, orthographicCamera.position.x, orthographicCamera.position.y, paused) }
        mapLabels.forEach { it.goToTime(time, orthographicCamera.zoom, orthographicCamera.position.x, orthographicCamera.position.y, paused) }
    }
}
