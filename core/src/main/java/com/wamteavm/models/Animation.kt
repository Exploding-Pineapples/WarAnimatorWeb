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
    private var camera: Camera? = null,
    val objects: MutableList<AnyObject> = mutableListOf(),
    val nodes: MutableList<Node> = mutableListOf(),
    val nodeCollections: MutableList<NodeCollection> = mutableListOf(),
    var nodeCollectionID: Int = 0,
    var nodeID: Int = 0,
    var initTime: Int = 0
)
{
    @Transient var nodeEdgeHandler = NodeEdgeHandler(this)

    fun init() {
        nodeEdgeHandler = NodeEdgeHandler(this)
        nodes.forEach { node ->
            node.init()
            node.edges.forEach { it.updateScreenCoords(this) }
        }
        nodeEdgeHandler.updateNodeCollections()
        objects.forEach { it.init() }
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
        return objects.remove(obj)
    }

    fun getNodeCollection(id: NodeCollectionID): NodeCollection? {
        return nodeCollections.find { it.id.value == id.value }
    }

    fun getNodeByID(id: NodeID): Node? = nodes.firstOrNull { it.id.value == id.value }

    fun newNode(x: Float, y: Float, time: Int): Node {
        val node = Node(Coordinate(x, y), time, NodeID(nodeID))
        node.buildInputs()
        nodeEdgeHandler.addNode(node)
        return node
    }

    fun createObjectAtPosition(time: Int, x: Float, y: Float, type: String, country: String = ""): AnyObject? {
        val new = when (type) {
            "Node" -> (::newNode)(x, y, time)
            "Arrow" -> Arrow(Coordinate(x, y), time)
            "Image" -> Image(Coordinate(x, y), time)
            "Map Label" -> MapLabel(Coordinate(x, y), time)
            "Unit" -> Unit(Coordinate(x, y), time, country)
            else -> null
        }
        if (new != null) {
            new.init()
            objects.add(new)
        }
        return new
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AnyObject> selectObjectWithType(x: Float, y: Float, time: Int, type: Class<out AnyObject>): ArrayList<T> {
        val clickedObjects = ArrayList<T>()

        if (type.isAssignableFrom(Node::class.java)) {
            clickedObjects.addAll(nodes.filter { time == it.initTime && it.clicked(x, y) }.map {it as T})
        }

        for (obj in objects) {
            if (Clickable::class.java.isAssignableFrom(obj.javaClass) && type::class.java.isAssignableFrom(obj.javaClass)) {
                if ((obj as Clickable).clicked(x, y)) {
                    clickedObjects.add(obj as T)
                }
            }
        }

        return clickedObjects
    }

    fun getParents(node: Node) : List<NodeCollection> {
        return nodeCollections.filter {
                nodeCollection -> (nodeCollection.interpolator.setPoints[node.initTime]?.nodes?.find { it.id.value == node.id.value } != null)
        }
    }

    fun update(time: Int, orthographicCamera: OrthographicCamera, paused: Boolean) {
        nodeEdgeHandler.update(time, orthographicCamera, paused)
        objects.forEach {
            if (ScreenObjectWithAlpha::class.java.isAssignableFrom(it.javaClass)) {
                (it as ScreenObjectWithAlpha).update(
                    time,
                    orthographicCamera.zoom,
                    orthographicCamera.position.x,
                    orthographicCamera.position.y,
                    paused
                )
            }
        }
    }

    fun draw(drawer: Drawer) {
        drawer.draw(this)
    }
}
