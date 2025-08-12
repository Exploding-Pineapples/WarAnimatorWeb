package com.wamteavm.models

import com.wamteavm.WarAnimator
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.Label
import com.wamteavm.models.screenobjects.Unit
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Animation @JvmOverloads constructor(
    var name: String = "My Animation",
    var id: String = "",
    val units: MutableList<Unit> = mutableListOf(),
    private var camera: Camera? = null,
    val nodes: MutableList<Node> = mutableListOf(),
    val nodeCollections: MutableList<NodeCollection> = mutableListOf(),
    val arrows: MutableList<Arrow> = mutableListOf(),
    val labels: MutableList<Label> = mutableListOf(),
    var images: MutableList<Image> = mutableListOf(),
    var nodeCollectionID: Int = 0,
    var nodeId: Int = 0,
    var initTime: Int = 0
)
{
    @Transient var nodeEdgeHandler = NodeEdgeHandler(this)
    @Transient lateinit var drawer: Drawer // Given by AnimationScreen

    fun init(drawer: Drawer) {
        this.drawer = drawer
        drawer.init(this)
        nodeEdgeHandler = NodeEdgeHandler(this)
        nodeEdgeHandler.init()
        units.forEach { it.init() }
        labels.forEach { it.init() }
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
        var result = false
        if (obj.javaClass == Node::class.java) {
            result = nodeEdgeHandler.removeNode(obj as Node)
        }
        if (obj.javaClass == Image::class.java) {
            result = images.remove(obj as Image)
        }
        if (obj.javaClass == Label::class.java) {
            result = labels.remove(obj as Label)
        }
        if (obj.javaClass == Arrow::class.java) {
            result = arrows.remove(obj as Arrow)
        }
        if (obj.javaClass == Unit::class.java) {
            result = units.remove(obj as Unit)
        }
        if (obj.javaClass == Edge::class.java) {
            result = nodeEdgeHandler.removeEdge(obj as Edge)
        }
        drawer.updateDrawOrder(this)
        return result
    }

    fun getNodeCollection(id: NodeCollectionID): NodeCollection? {
        return nodeCollections.find { it.id.value == id.value }
    }

    fun getNodeByID(id: NodeID): Node? = nodes.firstOrNull { it.id.value == id.value }

    // May the coding gods forgive me for I have sinned
    fun <T : AnyObject>createObjectAtPosition(time: Int, x: Float, y: Float, clazz: Class<T>): T {
        val new = when (clazz) {
            Arrow::class.java -> Arrow(Coordinate(x, y), time)
            Image::class.java -> Image(Coordinate(x, y), time)
            Label::class.java -> Label(Coordinate(x, y), time)
            Unit::class.java -> Unit(Coordinate(x, y), time)
            Node::class.java -> Node(Coordinate(x, y), time, NodeID(nodeId))
            else -> throw IllegalStateException("Unexpected create class")
        }
        when (clazz) {
            Arrow::class.java -> arrows.add(new as Arrow)
            Image::class.java -> images.add(new as Image)
            Label::class.java -> labels.add(new as Label)
            Unit::class.java -> units.add(new as Unit)
            Node::class.java -> nodeEdgeHandler.addNode(new as Node)
        }
        new.init()
        if (Drawable::class.java.isAssignableFrom(clazz)) {
            drawer.addToDrawOrder(new as Drawable)
        }

        return new as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AnyObject> selectObjectWithType(x: Float, y: Float, zoom: Float, time: Int, type: Class<out AnyObject>): ArrayList<T> {
        val objects = ArrayList<T>()

        if (type.isAssignableFrom(Node::class.java)) {
            objects.addAll(nodes.filter { time == it.initTime && it.clicked(x, y, zoom) }.map {it as T})
        }

        if (type.isAssignableFrom(Edge::class.java)) {
            nodes.filter { time == it.initTime }.forEach { node ->
                objects.addAll(node.edges.filter { it.clicked(x, y, zoom) }.map { it as T} )
            }
        }

        if (type.isAssignableFrom(Arrow::class.java)) {
            objects.addAll(arrows.filter { it.clicked(x, y, zoom) }.map {it as T} )
        }

        if (type.isAssignableFrom(Label::class.java)) {
            objects.addAll(labels.filter { it.clicked(x, y, zoom) }.map {it as T} )
        }

        if (type.isAssignableFrom(Image::class.java)) {
            objects.addAll(images.filter { it.clicked(x, y, zoom) }.map {it as T} )
        }

        if (type.isAssignableFrom(Unit::class.java)) {
            objects.addAll(units.filter { it.clicked(x, y, zoom) }.map { it as T} )
        }

        if (type.isAssignableFrom(NodeCollection::class.java)) {
            objects.addAll(nodeCollections.filter { it.clicked(x, y, zoom) }.map {it as T} )
        }

        return objects
    }

    fun getParents(node: Node) : List<NodeCollection> {
        return nodeCollections.filter {
                nodeCollection -> (nodeCollection.interpolator.setPoints[node.initTime]?.nodes?.find { it.id.value == node.id.value } != null)
        }
    }

    fun update(time: Int, animationMode: Boolean) {
        camera().update(time)
        drawer.update(time, animationMode)
        nodeEdgeHandler.update(time)
        units.forEach { it.update(time) }
        images.forEach { it.update(time) }
        arrows.forEach { it.update(time) }
        labels.forEach { it.update(time) }
    }

    fun draw() {
        drawer.draw(this)
    }
}
