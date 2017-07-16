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

import com.cluster.engine.Physics.Collisions.Callbacks.CollisionJumpTable;
import com.cluster.engine.Physics.Collisions.Manifold;
import com.cluster.engine.Physics.Transform;
import com.cluster.engine.Utilities.ContentManager;
import com.cluster.engine.Utilities.State.GameStateManager;
import com.cluster.engine.Utilities.State.State;
import com.pixeldot.firewall.Entities.Guard;
import com.pixeldot.firewall.Entities.Player;
import com.pixeldot.firewall.Level.Blocks.Wall;
import com.sun.org.apache.bcel.internal.generic.RET;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.Mouse;

import java.util.ArrayList;

public class PlayState extends State {

    private Wall wall;
    private Player player;

    private Guard guard;

    private Font font;
    private Color tColour;

    Vector2f[] path = {
            new Vector2f(400, 400), new Vector2f(500, 400),
            new Vector2f(600, 450),
            new Vector2f(500, 500), new Vector2f(400, 500)
    };

    private boolean lClick, enter;

    ArrayList<Vector2f> p;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        font = ContentManager.getInstance().loadFont("Main", "Font.ttf");

        p = new ArrayList<>();


        wall = new Wall(new Vector2f(100, 100), new Vector2f(40, 680));
        player = new Player(new Vector2f(300, 100), null);

        guard = new Guard(path, 0);
        tColour = Color.RED;
    }

    public void update(float dt) {
        player.update(dt);
        guard.update(dt);

        if(guard.seePlayer(player.getPosition())) {
            tColour = Color.GREEN;
        }
        else {
            tColour = Color.RED;
        }

        if(Keyboard.isKeyPressed(Keyboard.Key.LCONTROL) && Mouse.isButtonPressed(Mouse.Button.LEFT) && !lClick) {
            p.add(window.mapPixelToCoords(Mouse.getPosition(window)));
        }

        if(Keyboard.isKeyPressed(Keyboard.Key.RETURN) && !enter) {
            Vector2f[] v = new Vector2f[p.size()];
            v = p.toArray(v);
            guard.setPath(v);
            p.clear();
        }

        checkCollisions();

        view.setCenter(Vector2f.add(player.getPosition(), new Vector2f(Player.SIZE.x / 2f, Player.SIZE.y / 2f)));
        window.setView(view);

        enter = Keyboard.isKeyPressed(Keyboard.Key.RETURN);
        lClick = Mouse.isButtonPressed(Mouse.Button.LEFT);
    }

    public void render() {


        for(int i = 0, j = p.size() - 1; i < p.size(); j = i++) {
            Vector2f p1 = p.get(i);
            Vector2f p2 = p.get(j);

            Vertex[] v = new Vertex[2];
            v[0] = new Vertex(p1, Color.GREEN);
            v[1] = new Vertex(p2, Color.GREEN);
            window.draw(v, PrimitiveType.LINES);
        }

        Shape s = (Shape) wall.getShape().getDrawable();
        s.setPosition(wall.getPosition());

        window.draw(s);

        guard.render(window);

        s = (Shape) player.getShape().getDrawable();
        s.setPosition(player.getPosition());

        window.draw(s);

    }

    private void checkCollisions() {
        Transform txA = new Transform(player.getPosition(), 0);
        Transform txB = new Transform(wall.getPosition(), 0);
        Manifold m = new Manifold(txA, txB);
        CollisionJumpTable.handlers[0][0].handleCollision(m, player.getShape(), wall.getShape());
        player.setColliding(m.collided);
        if(m.collided) {
            player.setPosition(Vector2f.sub(player.getPosition(), Vector2f.mul(m.normal, m.overlap)));
        }
    }
}
