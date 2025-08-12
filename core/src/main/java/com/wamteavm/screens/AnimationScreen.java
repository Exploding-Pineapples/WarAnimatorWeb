package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Array;
import com.wamteavm.WarAnimator;
import com.wamteavm.models.screenobjects.Arrow;
import com.wamteavm.models.screenobjects.Image;
import com.wamteavm.models.screenobjects.Unit;
import com.wamteavm.ui.inputelements.SelectBoxInput;
import com.wamteavm.models.*;
import com.wamteavm.ui.InputElementShower;
import com.wamteavm.ui.input.Action;
import com.wamteavm.ui.input.Requirement;
import com.wamteavm.ui.input.TouchMode;

import java.util.ArrayList;
import java.util.List;

import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;
import static java.lang.Math.round;

public class AnimationScreen extends ScreenAdapter implements InputProcessor {
    public static final int DEFAULT_UNIT_WIDTH = 75;
    public static final int DEFAULT_UNIT_HEIGHT = 75;
    public static final double LINE_RESOLUTION = 10; // Distance per straight line
    public static final int MAX_LINES_PER_LENGTH = 5;

    public Animation animation;
    public WarAnimator game;

    OrthographicCamera orthographicCamera; // Camera actually used when running, animation.camera only updates this

    // Mouse position in unprojected units
    float mouseX;
    float mouseY;

    Integer time;
    boolean paused;

    ArrayList<AnyObject> selectedObjects;

    // Actions
    TouchMode touchMode;
    List<Action> actions;
    boolean shiftPressed;
    boolean ctrlPressed;
    long commaLastUnpressed = 0;
    long periodLastUnpressed = 0;
    boolean keysCaught = false;

    // UI
    Stage stage;
    Table selectedInfoTable;
    VerticalGroup selectedGroup;
    Table leftPanel;
    VerticalGroup leftGroup;
    Label timeAndFPS;
    Label keyOptions;
    Label selectedLabel;
    InputElementShower uiShower;
    Integer newNodeCollectionID;
    SelectBoxInput<Integer> newNodeCollectionIDInput;
    Class<? extends AnyObject> createClass;
    SelectBoxInput<String> createSelectBoxInput;
    boolean animationMode;
    boolean UIDisplayed;
    boolean newEdgeInputsDisplayed;

    public AnimationScreen(WarAnimator game, Animation animation) {
        this.animation = animation;
        this.game = game;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        time = animation.getInitTime();
        paused = true;
        animationMode = true;

        //UI
        selectedObjects = new ArrayList<>();

        touchMode = TouchMode.DEFAULT;
        actions = new ArrayList<>();
        buildActions();
        UIDisplayed = false;
        stage = new Stage();

        timeAndFPS = new Label("", game.skin);
        keyOptions = new Label("", game.skin);
        leftGroup = new VerticalGroup();

        Array<String> createChoices = new Array<>();
        createChoices.addAll("Unit", "Node", "MapLabel", "Arrow", "Image");

        createSelectBoxInput = new SelectBoxInput<>(
            game.skin,
            (String in) -> {
                createClass = switch (in) {
                    case "Unit" -> Unit.class;
                    case "Node" -> Node.class;
                    case "MapLabel" -> com.wamteavm.models.screenobjects.Label.class;
                    case "Arrow" -> Arrow.class;
                    case "Image" -> Image.class;
                    default -> AnyObject.class;
                };
                return null;
            },
            () -> createClass.getSimpleName(),
            String.class,
            "Create Type",
            createChoices, null);
        createClass = Unit.class;

        newNodeCollectionID = 0;
        Array<Integer> idChoices = new Array<>();
        idChoices.add(animation.getNodeCollectionID());
        for (NodeCollection nodeCollection : animation.getNodeCollections()) {
            idChoices.add(nodeCollection.getId().getValue());
        }
        newNodeCollectionIDInput = new SelectBoxInput<>(game.skin,
            (Integer input) -> { newNodeCollectionID = input; return null; },
            () -> newNodeCollectionID,
            Integer.class,
            "CollectionID of New Edge",
            idChoices,
            null);
        newEdgeInputsDisplayed = false;

        selectedLabel = new Label("", game.skin);
        selectedGroup = new VerticalGroup();
        uiShower = new InputElementShower(game.skin, animation);
        selectedInfoTable = new Table();
        stage.addActor(selectedInfoTable);

        leftPanel = new Table();
        stage.addActor(leftPanel);

        int[] catchKeys = new int[]{Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT, Input.Keys.SPACE, Input.Keys.ESCAPE, Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT};

        for (int key : catchKeys) {
            Gdx.input.setCatchKey(key, true);
        }

        orthographicCamera = new OrthographicCamera(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        animation.init(new Drawer(game.bitmapFont, game.fontShader, game.batch, game.shapeDrawer, orthographicCamera, time));
        updateCam();
    }

    public void buildActions() {
        // Actions available when game is not inputting
        // Actions that do not care about selection
        actions.add(Action.createBuilder(() -> {
            paused = !paused;
            return null;
        }, "Pause/Unpause the game", Input.Keys.SPACE).requiresSelected(Requirement.ANY).build());
        // Shift required
        actions.add(Action.createBuilder(() -> {
            game.animationLoader.save();
            game.menuScreen = new MenuScreen(game);
            game.setScreen(game.menuScreen);
            return null;
        }, "Return to the main menu", Input.Keys.ESCAPE).requiresSelected(Requirement.ANY).requiresShift(true).build());
        actions.add(Action.createBuilder(() -> {
            animationMode = !animationMode;
            return null;
        }, "Toggle UI", Input.Keys.V).build());
        // Selection required
        actions.add(Action.createBuilder(() -> {
            for (AnyObject selectedObject : selectedObjects) {
                if (HasPosition.class.isAssignableFrom(selectedObject.getClass())) {
                    ((HasPosition) selectedObject).holdPositionUntil(time);
                }
                if (selectedObject.getClass() == NodeCollection.class) {
                    ((NodeCollection) selectedObject).getInterpolator().holdValueUntil(time, animation);
                }
            }
            clearSelected();
            return null;
        }, "Hold last defined position to this time", Input.Keys.H).requiresSelected(Requirement.REQUIRES).requiredSelectedTypes(InterpolatedObject.class).build());
        // Does not care about selection
        actions.add(Action.createBuilder(() -> {
            updateTime((time / 200) * 200 + 200);
            return null;
        }, "Step time forward 200", Input.Keys.E).build());
        actions.add(Action.createBuilder(() -> {
            updateTime((int) Math.ceil(time / 200.0) * 200 - 200);
            return null;
        }, "Step time back 200", Input.Keys.Q).build());
        actions.add(Action.createBuilder(() -> {
            updateTime(time + 1);
            return null;
        }, "Step time forward 1", Input.Keys.PERIOD).build());
        actions.add(Action.createBuilder(() -> {
            updateTime(time - 1);
            return null;
        }, "Step time back 1", Input.Keys.COMMA).build());
        actions.add(Action.createBuilder(() -> {
            if (touchMode == TouchMode.MOVE) {
                touchMode = TouchMode.DEFAULT;
                return null;
            }
            touchMode = TouchMode.MOVE;
            return null;
        }, "Toggle move mode", Input.Keys.M).requiresSelected(Requirement.ANY).build());
        actions.add(Action.createBuilder(() -> {
                if (touchMode != TouchMode.CREATE) {
                    touchMode = TouchMode.CREATE;
                } else {
                    touchMode = TouchMode.DEFAULT;
                }
                return null;
            }, "Toggle create object mode", Input.Keys.C
        ).build());
        actions.add(Action.createBuilder(() -> {
                ArrayList<AnyObject> selectedObjectsCopy = new ArrayList<>(selectedObjects);
                for (AnyObject selectedObject : selectedObjectsCopy) {
                    if (selectedObject.getClass() == Node.class) {
                        Node newNode = animation.createObjectAtPosition(time, mouseX, mouseY, Node.class);
                        animation.getNodeEdgeHandler().insert((Node) selectedObject, newNode);
                        switchSelected(newNode);
                    }
                }
                return null;
            }, "Insert node into collection", Input.Keys.I
        ).requiresSelected(Requirement.REQUIRES).requiredSelectedTypes(Node.class).build());
        //Key presses which require control pressed
        actions.add(Action.createBuilder(() -> {
            switchSelected(animation.camera());
            return null;
        }, "Select the camera", Input.Keys.C).requiresControl(true).build());
        actions.add(Action.createBuilder(() -> {
            if (touchMode == TouchMode.NEW_EDGE) {
                touchMode = TouchMode.DEFAULT;
            } else {
                touchMode = TouchMode.NEW_EDGE;
                updateNewEdgeInputs();
            }
            return null;
        }, "Toggle new edge mode", Input.Keys.E).requiresControl(true).build());
        actions.add(Action.createBuilder(() -> {
            game.animationLoader.save();
            System.out.println("saved");
            return null;
        }, "Save project", Input.Keys.S).requiresControl(true).build());
        actions.add(Action.createBuilder(() -> {
            animation.camera().getZoomInterpolator().newSetPoint(time, orthographicCamera.zoom);
            return null;
        }, "Set a camera zoom set point", Input.Keys.Z).requiresControl(true).build());
        // Key presses which require selected Object
        actions.add(Action.createBuilder(() -> {
            clearSelected();
            System.out.println("Deselected object");
            return null;
        }, "Deselect Object", Input.Keys.D).description("Deselect object").requiresSelected(Requirement.REQUIRES).build());
        actions.add(Action.createBuilder(() -> {
            for (AnyObject selectedObject : selectedObjects) {
                animation.deleteObject(selectedObject);
            }
            System.out.println("Deleted object");
            clearSelected();
            return null;
        }, "Delete selected object", Input.Keys.FORWARD_DEL).requiresSelected(Requirement.REQUIRES).build());
        actions.add(Action.createBuilder(() -> {
            for (AnyObject selectedObject : selectedObjects) {
                if (HasPosition.class.isAssignableFrom(selectedObject.getClass())) {
                    if (((HasPosition) selectedObject).getPosInterpolator().removeFrame(time)) {
                        System.out.println("Deleted last frame");
                    } else {
                        System.out.println("Cannot delete frame on object with less than 2 frames");
                    }
                }
            }
            clearSelected();
            touchMode = TouchMode.DEFAULT;
            return null;
        }, "Delete last frame of selected object", Input.Keys.DEL).build());
    }

    public void updateCam() {
        orthographicCamera.position.x = animation.camera().getPosition().getX();
        orthographicCamera.position.y = animation.camera().getPosition().getY();
        orthographicCamera.zoom = animation.camera().getZoomInterpolator().getValue();
    }

    private void updateTime(int newTime) {
        time = newTime;
        animation.update(time, animationMode);
        updateCam();
    }

    public void updateNewEdgeInputs() { // Makes new edges match ID with first selected node collection
        System.out.println("Updating new edge inputs");
        for (AnyObject selectedObject : selectedObjects) {
            if (selectedObject.getClass() == NodeCollection.class) {
                NodeCollection selectedNodeCollection = (NodeCollection) selectedObject;
                newNodeCollectionID = selectedNodeCollection.getId().getValue();
                newNodeCollectionIDInput.hide(leftGroup);
                newEdgeInputsDisplayed = false;
                break;
            }
        }
    }

    private void moveObjects(ArrayList<AnyObject> objects) {
        for (AnyObject object : objects) {
            if (HasPosition.class.isAssignableFrom(object.getClass())) {
                Coordinate mouseCoords = new Coordinate(mouseX, mouseY);

                ((HasPosition) object).setPosition(mouseCoords);
                ((HasPosition) object).getPosInterpolator().newSetPoint(time, mouseCoords);

                if (object.getClass() == Node.class) {
                    for (NodeCollection parent : animation.getParents((Node) object)) {
                        parent.getInterpolator().updateInterpolationFunction();
                    }
                }
            }
        }
    }

    private void clearSelected() {
        uiShower.hideAll(selectedGroup);
        selectedObjects.clear();
    }

    public <T extends AnyObject> void addNewSelection(T newSelection) {
        if (newSelection != null) {
            selectedObjects.add(newSelection);

            if (newSelection.getClass() == Node.class) { // Show new selection's parent's inputs if it has parents
                for (NodeCollection collection : animation.getParents((Node) newSelection)) {
                    if (!selectedObjects.contains(collection)) {
                        if (collection != null) {
                            selectedObjects.add(collection);
                        } else {
                            System.out.println("Warning: Null node collection");
                        }
                    }
                }
            }
            if (newSelection.getClass() == Edge.class) {
                NodeCollection collection = animation.getNodeCollection(((Edge) newSelection).getCollectionID());
                if (!selectedObjects.contains(collection)) {
                    if (collection != null) {
                        selectedObjects.add(collection);
                    } else {
                        System.out.println("Warning: Null node collection");
                    }
                }
            }

            System.out.println("Selected: " + newSelection.getClass().getSimpleName());
        }
        uiShower.update(selectedGroup, selectedObjects, time);
    }

    public <T extends AnyObject> void switchSelected(T newSelection) {
        clearSelected();
        addNewSelection(newSelection);
    }

    private void updateUI() {
        if (animationMode) {
            if (paused) { // Update the selected object to go to mouse in move mode
                if ((touchMode == TouchMode.MOVE)) {
                    moveObjects(selectedObjects);
                }
            }
            // Set information about keyboard options and current animator state
            timeAndFPS.setText(Gdx.graphics.getFramesPerSecond() + " FPS \n" + "Time: " + time);

            StringBuilder options = new StringBuilder();
            options.append("Touch mode: ").append(touchMode.name()).append("\n");
            options.append("Control pressed: ").append(ctrlPressed).append(" ").append("Shift pressed: ").append(shiftPressed).append("\n");
            for (Action action : actions) {
                if (action.couldExecute(shiftPressed, ctrlPressed, selectedObjects, touchMode)) {
                    for (int key : action.getActionKeys()) {
                        options.append(Input.Keys.toString(key)).append(", ");
                    }
                    options.replace(options.length() - 2, options.length() - 1, " |"); // Replace trailing comma, StringJoiner unavailable in TeaVM
                    options.append(action.getActionName()).append("\n");
                }
            }
            keyOptions.setText(options);

            //Add information about mouse position selected object
            StringBuilder selectedInfo = new StringBuilder("Mouse: " + round(mouseX) + ", " + round(mouseY) + "\n");

            if (selectedObjects.isEmpty()) {
                selectedInfo.append("Nothing is selected").append("\n");
            } else {
                for (AnyObject selectedObject : selectedObjects) {
                    selectedInfo.append("Selected: ").append(selectedObject.getClass().getSimpleName()).append("\n");
                    if (selectedObject.getClass().isAssignableFrom(HasPosition.class)) {
                        selectedInfo.append("x: ").append(((HasPosition) selectedObject).getPosition().getX()).append("\n");
                        selectedInfo.append("y: ").append(((HasPosition) selectedObject).getPosition().getY()).append("\n");
                    }
                    if (selectedObject.getClass().isAssignableFrom(HasID.class)) {
                        selectedInfo.append("ID: ").append(((HasID) selectedObjects).getId().getValue()).append("\n");
                    }
                    if (selectedObject.getClass() == Node.class) {
                        Node node = (Node) selectedObject;
                        selectedInfo.append("NodeID: ").append(node.getId().getValue()).append("\n");
                        for (NodeCollection parent : animation.getParents((node))) {
                            // Get what parameter value the node is at within its node collection set points.
                            NodeCollectionSetPoint setPoint = parent.getInterpolator().getSetPoints().get(time);
                            if (setPoint != null) {
                                selectedInfo.append("T on Node Collection").append(parent.getId().getValue()).append(": ")
                                    .append(round(setPoint.tOfNode(node) * 10000) / 10000.0).append("\n");
                            }
                        }

                        ArrayList<Integer> toNodes = new ArrayList<>();
                        for (Edge edge : node.getEdges()) {
                            toNodes.add(edge.getSegment().getSecond().getValue());
                        }
                        selectedInfo.append("Edges: ").append(toNodes).append("\n");
                    }
                    if (selectedObject.getClass() == NodeCollection.class) {
                        selectedInfo.append("NodeCollectionID: ").append(((NodeCollection) selectedObject).getId().getValue()).append("\n");
                    }
                }
            }

            selectedLabel.setText(selectedInfo);

            if (!UIDisplayed) {
                leftPanel.add(timeAndFPS).left().pad(10);
                leftPanel.row();
                leftPanel.add(keyOptions).pad(10);
                leftPanel.row();
                leftPanel.add(leftGroup);

                selectedInfoTable.add(selectedLabel).expandX().pad(10).left();
                selectedInfoTable.row().pad(10);
                uiShower.showAll(selectedGroup);
                selectedInfoTable.add(selectedGroup);

                UIDisplayed = true;
            }

            if (touchMode == TouchMode.NEW_EDGE) {
                Array<Integer> idChoices = new Array<>();
                idChoices.add(animation.getNodeCollectionID());
                for (NodeCollection nodeCollection : animation.getNodeCollections()) {
                    idChoices.add(nodeCollection.getId().getValue());
                }
                newNodeCollectionIDInput.getChoices().clear();
                newNodeCollectionIDInput.getChoices().addAll(idChoices);
                if (!newEdgeInputsDisplayed) {
                    newNodeCollectionIDInput.show(leftGroup, game.skin);
                    newEdgeInputsDisplayed = true;
                }
            } else {
                if (newEdgeInputsDisplayed) {
                    newNodeCollectionIDInput.hide(leftGroup);
                    newEdgeInputsDisplayed = false;
                }
            }

            if (touchMode == TouchMode.CREATE) {
                if (!createSelectBoxInput.getDisplayed()) {
                    createSelectBoxInput.show(leftGroup, game.skin);
                    createSelectBoxInput.setDisplayed(true);
                }
            } else {
                if (createSelectBoxInput.getDisplayed()) {
                    createSelectBoxInput.hide(leftGroup);
                    createSelectBoxInput.setDisplayed(false);
                }
            }

            leftPanel.pack();
            leftPanel.setPosition(30, DISPLAY_HEIGHT - 30 - leftPanel.getHeight());

            selectedInfoTable.pack();
            selectedGroup.pack();
            selectedInfoTable.setPosition(DISPLAY_WIDTH - 30 - selectedInfoTable.getWidth(), DISPLAY_HEIGHT  - 30 - selectedInfoTable.getHeight());
        } else {
            if (UIDisplayed) {
                uiShower.hideAll(selectedGroup);
                leftPanel.clear();
                selectedInfoTable.clear();
                UIDisplayed = false;
            }
        }

        if (ctrlPressed) {
            if (!keysCaught) {
                for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                    Gdx.input.setCatchKey(key, true);
                }
                keysCaught = true;
            }
        } else {
            if (keysCaught) {
                for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                    Gdx.input.setCatchKey(key, false);
                }
                keysCaught = false;
            }
        }
    }

    private void update() {
        mouseX = (float) ((double) Gdx.input.getX() - orthographicCamera.position.x * (1 - orthographicCamera.zoom) - (DISPLAY_WIDTH / 2f - orthographicCamera.position.x)) / orthographicCamera.zoom;
        mouseY = (float) ((double) (DISPLAY_HEIGHT - Gdx.input.getY()) - orthographicCamera.position.y * (1 - orthographicCamera.zoom) - (DISPLAY_HEIGHT / 2f - orthographicCamera.position.y)) / orthographicCamera.zoom;
        ctrlPressed = (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT));
        shiftPressed = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));

        animation.update(time, animationMode);

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            orthographicCamera.position.y += 10 / orthographicCamera.zoom;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            orthographicCamera.position.y -= 10 / orthographicCamera.zoom;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            orthographicCamera.position.x -= 10 / orthographicCamera.zoom;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            orthographicCamera.position.x += 10 / orthographicCamera.zoom;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            if (System.currentTimeMillis() - commaLastUnpressed >= 250) {
                updateTime(time - 1);
            }
        } else {
            commaLastUnpressed = System.currentTimeMillis();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            if (System.currentTimeMillis() - periodLastUnpressed >= 250) {
                updateTime(time + 1);
            }
        } else {
            periodLastUnpressed = System.currentTimeMillis();
        }

        if (!paused) { //don't update camera when paused to allow for movement when paused
            updateCam();
        }

        updateUI();

        stage.act();
    }

    @Override
    public void render(float delta) {
        update();

        game.batch.begin();
        animation.draw();

        if (animationMode) {
            // Draw contrast backgrounds for UI
            game.shapeDrawer.setColor(new Color(0, 0, 0, 0.5f));

            game.shapeDrawer.filledRectangle(leftPanel.getX(), leftPanel.getY(), leftPanel.getWidth(), leftPanel.getHeight());
            game.shapeDrawer.filledRectangle(selectedInfoTable.getX(), selectedInfoTable.getY(), selectedInfoTable.getWidth(), selectedInfoTable.getHeight());

            //Draw the selected objects
            for (AnyObject selectedObject : selectedObjects) {
                animation.drawer.drawAsSelected(selectedObject);
            }
        }
        game.batch.end();

        stage.draw();

        if (!paused) {
            time++;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        for (Action action : actions) {
            if (action.shouldExecute(keycode, shiftPressed, ctrlPressed, selectedObjects, touchMode)) {
                action.execute();
                break;
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends AnyObject> T selectNewObject(float x, float y, ArrayList<AnyObject> selected, Class<T> type) { // Returns first selected object not already selected
        for (AnyObject selectedObject : animation.selectObjectWithType(x, y, orthographicCamera.zoom, time, type)) {
            if (!selected.contains(selectedObject)) {
                return (T) selectedObject;
            }
        }
        return null;
    }

    public void selectDefault(float x, float y) {
        if (ctrlPressed) {
            addNewSelection(selectNewObject(x, y, selectedObjects, AnyObject.class));
        } else {
            switchSelected(selectNewObject(x, y, selectedObjects, AnyObject.class));
        }
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        y = DISPLAY_HEIGHT - y;

        System.out.println("Clicked " + mouseX + " " + mouseY + " touch mode " + touchMode);

        animation.selectObjectWithType(x, y, orthographicCamera.zoom, time, Node.class);

        if (paused) {
            if (touchMode == TouchMode.DEFAULT) { // Default behavior: select an object to show info about it
                selectDefault(x, y);
            }
            if (touchMode == TouchMode.MOVE) { // Selects an object to move. If a node is selected to be moved into another node, it will be merged
                if (selectedObjects.isEmpty()) {
                    selectDefault(x, y);
                } else {
                    clearSelected();
                }

                moveObjects(selectedObjects);
            }

            if (touchMode == TouchMode.CREATE) {
                switchSelected(animation.createObjectAtPosition(time, mouseX, mouseY, createClass));
            }

            if (touchMode == TouchMode.NEW_EDGE) {
                Node newSelection = selectNewObject(x, y, selectedObjects, Node.class);
                if (newSelection != null) {
                    for (AnyObject selectedObject : selectedObjects) { // Add edge from already selected Nodes to new selected node
                        if (selectedObject.getClass() == Node.class) {
                            System.out.println("trying to select for new edge");

                            Node currentSelection = (Node) selectedObject;
                            animation.getNodeEdgeHandler().addEdge(currentSelection, newSelection, newNodeCollectionID);
                            System.out.println("Added an edge. Edges: " + currentSelection.getEdges());
                        }
                    }
                }
                if (selectedObjects.isEmpty()) { // Only change new edge collection if nothing was selected
                    switchSelected(newSelection);
                    updateNewEdgeInputs();
                } else {
                    switchSelected(newSelection);
                }
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (paused) {
            float zoomMultiplier = 1 - 0.05f * amountY;
            orthographicCamera.zoom *= zoomMultiplier;
            orthographicCamera.position.x -= (mouseX - orthographicCamera.position.x) * (1 - zoomMultiplier);
            orthographicCamera.position.y -= (mouseY - orthographicCamera.position.y) * (1 - zoomMultiplier);
        }
        return true;
    }

    @Override
    public void show() {
        game.multiplexer.clear();
        game.multiplexer.addProcessor(stage);
        game.multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(game.multiplexer);
    }
}
