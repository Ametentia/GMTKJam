/*
    MIT License

    Copyright (c) 2017 James Bulman

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package com.pixeldot.firewall.States;

import com.cluster.engine.Utilities.ContentManager;
import com.cluster.engine.Utilities.State.GameStateManager;
import com.cluster.engine.Utilities.State.State;
import com.cluster.engine.Utilities.VUtil;
import com.pixeldot.firewall.Firewall;
import com.pixeldot.firewall.Level.BlockType;
import com.pixeldot.firewall.Level.Blocks.Block;
import com.pixeldot.firewall.Level.LevelFactory;
import com.pixeldot.firewall.Level.MapObject;
import com.pixeldot.firewall.Util.Input;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.Mouse;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LevelEditor extends State {

    public static final int MAX_PATH_LENGTH = 16;

    private static final Vector2f worldSize = new Vector2f(2560, 1440);
    private static final float gridSize = 20;

    private static final float zoomSpeed = 0.1f;

    private boolean spawnPlaced;

    private BlockType placingType;
    private ArrayList<MapObject> objects;

    private float accumulator;

    // Creating walls and cover
    private Vector2f startPosition;
    private boolean firstPlacement;

    // Keep within the world bounds
    private boolean isValid;

    // Path building for the Guards
    private Vector2f[] path;
    private int pathCount;

    // Camera movement via L ALT
    private boolean first, moving;
    private Vector2f moveS;

    // Controls
    private boolean showHelp, showGrid;

    // Input stuff
    private boolean lClick, rClick;
    private boolean lBracket, rBracket;
    private boolean lshift;
    private boolean up, down;
    private boolean z, o, s, r, h, g;

    private Input input;
    private float currentZoom;

    private Font font;

    public LevelEditor(GameStateManager gsm) {
        super(gsm);

        accumulator = 0;

        font = ContentManager.getInstance().loadFont("Main", "Font.ttf");

        placingType = BlockType.Wall;
        objects = new ArrayList<>();

        isValid = false;
        first = true;
        firstPlacement = true;

        spawnPlaced = false;

        lClick = rBracket = lBracket = false;

        path = new Vector2f[MAX_PATH_LENGTH];

        showGrid = true;

        currentZoom = 1f;
        input = new Input();
        game.getEngine().setInputHandler(input);
    }

    public void update(float dt) {
        /*accumulator += dt;
        if(accumulator >= 300) {
            accumulator = 0;
            saveMap("Map003.map");
        } */

        if(!Keyboard.isKeyPressed(Keyboard.Key.H) && h) {
            showHelp = !showHelp;
        }

        if(showHelp) {
            updateInput();
            return;
        }

        mouse = window.mapPixelToCoords(Mouse.getPosition(window));
        mouse = new Vector2f(gridSize * ((int) mouse.x / (int) gridSize),
                gridSize * ((int) mouse.y / (int) gridSize));

        isValid = mouse.x >= 0 && mouse.x <= worldSize.x
                && mouse.y >= 0 && mouse.y <= worldSize.y;

        if(Mouse.isButtonPressed(Mouse.Button.LEFT) && Keyboard.isKeyPressed(Keyboard.Key.LALT)) {
            if(first) {
                moveS = window.mapPixelToCoords(Mouse.getPosition(window));
                first = false;
            }
            else {
                Vector2f moveE = window.mapPixelToCoords(Mouse.getPosition(window));
                Vector2f dir = Vector2f.sub(moveS, moveE);
                float length = VUtil.length(Vector2f.sub(moveS, moveE));
                dir = VUtil.normalise(dir);

                view.move(dir.x * length, dir.y * length);
                window.setView(view);

                moving = true;
            }
            return;
        }

        if(!Mouse.isButtonPressed(Mouse.Button.LEFT) && lClick && isValid && !moving) {
            switch (placingType) {
                case Guard:
                case Cover:
                case Door:
                case Wall: {
                    if(firstPlacement) {
                        startPosition = mouse;
                        if(placingType == BlockType.Guard) path[pathCount++] = mouse;
                        firstPlacement = false;
                    }
                    else if(placingType != BlockType.Guard) {
                        Vector2f endPosition = mouse;
                        float x = Math.min(startPosition.x, endPosition.x);
                        float y = Math.min(startPosition.y, endPosition.y);

                        MapObject object = new MapObject();
                        object.type = placingType.ordinal();
                        object.position = new Vector2f(x, y);
                        object.size = new Vector2f(Math.abs(startPosition.x - endPosition.x) + gridSize,
                                Math.abs(startPosition.y - endPosition.y) + gridSize);
                        objects.add(object);

                        firstPlacement = true;
                    }
                    else {
                        path[pathCount++] = mouse;
                    }
                } break;

                case Spawn:
                case Socket:
                case Terminal: {
                    if(placingType == BlockType.Spawn) {
                        if(!spawnPlaced) {
                            MapObject object = new MapObject();
                            object.position = mouse;
                            object.size = new Vector2f(gridSize, gridSize);
                            object.type = placingType.ordinal();
                            objects.add(object);
                        }
                        else {
                            System.out.println("Only one spawn can be placed!");
                        }
                        spawnPlaced = true;
                    }
                    else {
                        MapObject object = new MapObject();
                        object.position = mouse;
                        object.size = new Vector2f(gridSize, gridSize);
                        object.type = placingType.ordinal();
                        objects.add(object);
                    }

                } break;
            }
        }

        if(placingType == BlockType.Guard) {
            if(!firstPlacement && (Keyboard.isKeyPressed(Keyboard.Key.RETURN) || pathCount == MAX_PATH_LENGTH)) {
                MapObject o = new MapObject();
                o.type = BlockType.Guard.ordinal();
                o.position = path[0];
                o.size = new Vector2f(gridSize, gridSize);
                o.pathLength = pathCount;
                o.path = path;

                objects.add(o);

                path = new Vector2f[MAX_PATH_LENGTH];
                pathCount = 0;
                firstPlacement = true;
            }
        }

        if(firstPlacement && !Mouse.isButtonPressed(Mouse.Button.RIGHT) && rClick) {
            remove();
        }

        if(!Keyboard.isKeyPressed(Keyboard.Key.G) && g) {
            showGrid = !showGrid;
        }

        if(Keyboard.isKeyPressed(Keyboard.Key.LCONTROL)) {
            firstPlacement = true;

            if(Keyboard.isKeyPressed(Keyboard.Key.Z) && !z) {
                int index = objects.size() - 1;
                if(index >= 0) {
                    MapObject o = objects.remove(objects.size() - 1);
                    if(o.type == BlockType.Spawn.ordinal()) {
                        spawnPlaced = false;
                    }
                }
            }

            if(Keyboard.isKeyPressed(Keyboard.Key.O) && !o) {
                loadMap("Map003.map");
            }

            if(Keyboard.isKeyPressed(Keyboard.Key.S) && !s) {
                saveMap("Map003.map");
            }

            if(Keyboard.isKeyPressed(Keyboard.Key.R) && !r) {
                if(spawnPlaced) {

                    saveMap("tmp.map");
                    gsm.addState(LevelFactory.loadLevel(gsm, "tmp.map", true));
                    try {
                        Files.delete(Paths.get(System.getProperty("user.dir")
                                + File.separator + "Maps" + File.separator + "tmp.map"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    System.out.println("Error: Player spawn point has not been placed");
                }
            }
        }

        if(!Keyboard.isKeyPressed(Keyboard.Key.RBRACKET) && rBracket) {
            firstPlacement = true;
            int index = placingType.ordinal();
            index = index + 1 == BlockType.values().length ? 0 : index + 1;
            placingType = BlockType.values()[index];
        }

        if(!Keyboard.isKeyPressed(Keyboard.Key.LBRACKET) && lBracket) {
            firstPlacement = true;
            int index = placingType.ordinal();
            index = index - 1 < 0 ? BlockType.values().length - 1 : index - 1;
            placingType = BlockType.values()[index];
        }

        int mwheel = input.getMWMoveAmount();
        if(mwheel < 0) {
            view.zoom(1.5f);
            currentZoom *= 1.5f;
            window.setView(view);
        }
        else if(mwheel > 0) {
            view.zoom(1.0f / 1.5f);
            currentZoom /= 1.5f;
            window.setView(view);
        }

        if(Keyboard.isKeyPressed(Keyboard.Key.DOWN) && !down) {
            view.zoom(2f);
            currentZoom *= 2f;
            window.setView(view);
        }
        else if(Keyboard.isKeyPressed(Keyboard.Key.UP) && !up) {
            view.zoom(0.5f);
            currentZoom /= 2f;
            window.setView(view);
        }

        updateInput();
    }

    public void render() {

        Vector2f hg = new Vector2f(gridSize / 2f, gridSize / 2f);

        RectangleShape shape = new RectangleShape(new Vector2f(gridSize, gridSize));
        switch (placingType) {
            case Terminal:
                shape.setFillColor(Color.BLACK);
                break;
            case Spawn:
                shape.setFillColor(Color.GREEN);
                break;
            case Door:
                shape.setFillColor(new Color(60, 60, 60));
                break;
            case Socket:
                shape.setFillColor(Color.YELLOW);
                break;
            case Guard:
                shape.setFillColor(Color.RED);
                break;
            case Cover:
                shape.setFillColor(Color.BLUE);
                break;

        }
        shape.setPosition(mouse);

        if(!firstPlacement && placingType != BlockType.Guard) {
            float x = Math.min(startPosition.x, mouse.x);
            float y = Math.min(startPosition.y, mouse.y);
            Vector2f start = new Vector2f(x, y);

            x = Math.max(startPosition.x, mouse.x);
            y = Math.max(startPosition.y, mouse.y);
            Vector2f end = new Vector2f(x, y);

            Vector2f size = Vector2f.sub(end, start);

            shape.setSize(Vector2f.add(size, new Vector2f(gridSize, gridSize)));
            shape.setPosition(start);
        }
        else if(!firstPlacement && placingType == BlockType.Guard) {
            shape.setPosition(path[0]);
            for(int i = 0, j = pathCount - 1; i < pathCount; j = i++) {
                Vertex[] line = new Vertex[2];

                Vector2f p1 = Vector2f.add(path[i], hg);
                Vector2f p2 = Vector2f.add(path[j], hg);

                line[0] = new Vertex(p1, Color.RED);
                line[1] = new Vertex(p2, Color.RED);
                window.draw(line, PrimitiveType.LINES);
            }

            Vertex[] line = new Vertex[2];
            line[0] = new Vertex(Vector2f.add(path[pathCount - 1], hg), Color.RED);
            line[1] = new Vertex(Vector2f.add(mouse, hg), Color.RED);
            window.draw(line, PrimitiveType.LINES);
        }

        window.draw(shape);

        for(MapObject o : objects) {
            shape = new RectangleShape(o.size);
            shape.setPosition(o.position);
            BlockType type = BlockType.values()[o.type];
            switch (type) {
                case Wall:
                    shape.setFillColor(Color.WHITE);
                    break;
                case Terminal: {
                    shape.setFillColor(Color.BLACK);
                    CircleShape circle = new CircleShape(45);
                    circle.setOrigin(45, 45);
                    circle.setFillColor(Color.TRANSPARENT);
                    circle.setOutlineThickness(1f * currentZoom);
                    circle.setOutlineColor(Color.BLACK);
                    circle.setPosition(o.position.x + hg.x, o.position.y + hg.y);
                    window.draw(circle);
                } break;
                case Spawn:
                    shape.setFillColor(Color.GREEN);
                    break;
                case Door:
                    shape.setFillColor(new Color(60, 60, 60));
                    break;
                case Socket: {
                    shape.setFillColor(Color.YELLOW);
                    CircleShape circle = new CircleShape(30);
                    circle.setOrigin(30, 30);
                    circle.setFillColor(Color.TRANSPARENT);
                    circle.setOutlineThickness(1f * currentZoom);
                    circle.setOutlineColor(Color.YELLOW);
                    circle.setPosition(o.position.x + hg.x, o.position.y + hg.y);
                    window.draw(circle);
                } break;
                case Cover:
                    shape.setFillColor(Color.BLUE);
                    break;
                case Guard:
                    shape.setFillColor(Color.RED);
                    for(int i = 0, j = o.pathLength - 1; i < o.pathLength; j = i++) {
                        Vector2f p1 = Vector2f.add(o.path[i], hg);
                        Vector2f p2 = Vector2f.add(o.path[j], hg);

                        Vertex[] line = new Vertex[2];
                        line[0] = new Vertex(p1, Color.RED);
                        line[1] = new Vertex(p2, Color.RED);
                        window.draw(line, PrimitiveType.LINE_STRIP);
                    }
                    break;
            }
            window.draw(shape);
        }

        drawGrid();

        if(lshift) {
            Vertex[] v = new Vertex[2];
            v[0] = new Vertex(new Vector2f(0, mouse.y + hg.y), Color.BLUE);
            v[1] = new Vertex(Vector2f.add(mouse, hg), Color.BLUE);

            int count = (int) mouse.x / 20 + 1;

            Text text = new Text("[" + count + "]", font, 15);
            text.setPosition(mouse.x - 35, mouse.y + 15);
            text.setColor(Color.BLUE);
            window.draw(text);

            window.draw(v, PrimitiveType.LINES);

            v[0] = new Vertex(new Vector2f(mouse.x + hg.x, 0), Color.BLUE);
            v[1] = new Vertex(Vector2f.add(mouse, hg), Color.BLUE);

            count = (int) mouse.y / 20 + 1;

            text = new Text("[" + count + "]", font, 15);
            text.setPosition(mouse.x + 20, mouse.y - 35);
            text.setColor(Color.BLUE);
            window.draw(text);

            window.draw(v, PrimitiveType.LINES);
        }

        View guiView = new View(new Vector2f(Firewall.WIDTH / 2f, Firewall.HEIGHT / 2f),
                new Vector2f(Firewall.WIDTH, Firewall.HEIGHT));

        window.setView(guiView);
        Text text = new Text("Placing: " + placingType.name(), font, 25);
        text.setPosition(25, 25);

        Vector2f size = new Vector2f(text.getLocalBounds().width + 5, text.getLocalBounds().height + 5);
        shape = new RectangleShape(size);
        shape.setFillColor(new Color(60, 60, 60, 180));
        shape.setOutlineColor(new Color(100, 100, 100, 180));
        shape.setOutlineThickness(3f);

        Vector2f pos = new Vector2f(text.getLocalBounds().left + text.getPosition().x - 2.5f,
                text.getLocalBounds().top + text.getPosition().y - 2.5f);

        shape.setPosition(pos);
        window.draw(shape);

        window.draw(text);

        text = new Text("Object Count: " + objects.size(), font, 25);
        text.setPosition(Firewall.WIDTH - text.getLocalBounds().width - 25, 25);

        shape.setSize(new Vector2f(text.getLocalBounds().width + 5, text.getLocalBounds().height + 5));
        pos = new Vector2f(text.getLocalBounds().left + text.getPosition().x - 2.5f,
                text.getLocalBounds().top + text.getPosition().y - 2.5f);
        shape.setPosition(pos);

        window.draw(shape);
        window.draw(text);

        if(showHelp) {
            shape = new RectangleShape(new Vector2f(960, 540));
            shape.setOutlineThickness(5);
            shape.setOutlineColor(Color.BLACK);
            shape.setPosition((Firewall.WIDTH - 960) / 2f, (Firewall.HEIGHT - 540) / 2f);
            window.draw(shape);

            text = new Text("Help:\n\nPlace Block: Left Click\nRemove Block: Right Click" +
                    "\nShow Alignment Indicator: Shift\nShow/ Hide Grid: G\nChange Block Type (Next): ]\n" +
                    "Change Block Type (Previous): [\nFinish guard path: Enter\n\n" +
                    "Move Camera: Alt + Left Click (and move)\nZoom: Scroll Wheel\n" +
                    "Test Level: Ctrl + R",
                    font, 25);
            text.setPosition(shape.getPosition().x + 15, shape.getPosition().y + 15);
            text.setColor(Color.BLACK);
            window.draw(text);
        }

        window.setView(view);
    }

    private void drawGrid() {
        RectangleShape grid = new RectangleShape(new Vector2f(gridSize, gridSize));
        if(showGrid) {
            grid.setOutlineThickness(0.5f * currentZoom);
            grid.setOutlineColor(Color.WHITE);
            grid.setFillColor(Color.TRANSPARENT);

            for (int i = 0; i < (int) worldSize.x / (int) gridSize; i++) {
                for (int j = 0; j < (int) worldSize.y / (int) gridSize; j++) {
                    grid.setPosition(gridSize * i, gridSize * j);
                    window.draw(grid);
                }
            }
        }

        grid = new RectangleShape(worldSize);
        grid.setFillColor(Color.TRANSPARENT);
        grid.setOutlineThickness(20);
        grid.setOutlineColor(Color.RED);
        window.draw(grid);
    }

    private void remove() {
        for(int i = 0; i < objects.size(); i++) {
            MapObject o = objects.get(i);
            Vector2f pos = window.mapPixelToCoords(Mouse.getPosition(window));

            if(pos.x > o.position.x && pos.x < o.position.x + o.size.x) {
                if(pos.y > o.position.y && pos.y < o.position.y + o.size.y) {
                    objects.remove(o);
                    i--;
                    if(o.type == BlockType.Spawn.ordinal()) {
                        spawnPlaced = false;
                    }
                }
            }
        }
    }

    private void updateInput() {
        lClick = Mouse.isButtonPressed(Mouse.Button.LEFT);
        rClick = Mouse.isButtonPressed(Mouse.Button.RIGHT);

        rBracket = Keyboard.isKeyPressed(Keyboard.Key.RBRACKET);
        lBracket = Keyboard.isKeyPressed(Keyboard.Key.LBRACKET);

        up = Keyboard.isKeyPressed(Keyboard.Key.UP);
        down = Keyboard.isKeyPressed(Keyboard.Key.DOWN);

        z = Keyboard.isKeyPressed(Keyboard.Key.Z);
        o = Keyboard.isKeyPressed(Keyboard.Key.O);
        s = Keyboard.isKeyPressed(Keyboard.Key.S);
        r = Keyboard.isKeyPressed(Keyboard.Key.R);
        h = Keyboard.isKeyPressed(Keyboard.Key.H);
        g = Keyboard.isKeyPressed(Keyboard.Key.G);

        lshift = Keyboard.isKeyPressed(Keyboard.Key.LSHIFT);

        input.clear();

        moving = false;
        first = true;
    }

    private void loadMap(String filename) {
        Path path = Paths.get(System.getProperty("user.dir") + File.separator +
                "Maps" + File.separator + filename);

        byte[] data = new byte[0];
        int length = 0;
        try {
            data = Files.readAllBytes(path);
            length = data.length;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.position() < length) {
            MapObject o = new MapObject();
            o.type = buffer.getInt();
            if(o.type != BlockType.Guard.ordinal()) {
                o.position = new Vector2f(buffer.getFloat(), buffer.getFloat());
                o.size = new Vector2f(buffer.getFloat(), buffer.getFloat());

                if(o.type == BlockType.Spawn.ordinal()) {
                    spawnPlaced = true;
                }
            }
            else {
                o.pathLength = buffer.getInt();
                o.direction = buffer.getInt();
                o.path = new Vector2f[o.pathLength];
                for(int i = 0; i < o.pathLength; i++) {
                    o.path[i] = new Vector2f(buffer.getFloat(), buffer.getFloat());
                }

                o.position = o.path[0];
                o.size = new Vector2f(gridSize, gridSize);
            }

            objects.add(o);
        }
    }

    private void saveMap(String filename) {

        int size = 0;
        for(MapObject object : objects) {
            if(object.type == BlockType.Guard.ordinal()) {
                size += (12 + (object.pathLength * 8));
            }
            else {
                size += 20;
            }
        }

        System.out.println(size);

        byte[] data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for(MapObject o : objects) {
            buffer.putInt(o.type);

            if(o.type != BlockType.Guard.ordinal()) {
                buffer.putFloat(o.position.x);
                buffer.putFloat(o.position.y);

                buffer.putFloat(o.size.x);
                buffer.putFloat(o.size.y);
            }
            else {
                buffer.putInt(o.pathLength);
                buffer.putInt(o.direction);
                for(int i = 0; i < o.pathLength; i++) {
                    buffer.putFloat(o.path[i].x);
                    buffer.putFloat(o.path[i].y);
                }
            }
        }

        Path path = Paths.get(System.getProperty("user.dir") + File.separator +
                "Maps" + File.separator + filename);
        try {
            Files.write(path, buffer.array());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
