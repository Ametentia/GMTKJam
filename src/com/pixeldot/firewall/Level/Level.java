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
package com.pixeldot.firewall.Level;

import com.cluster.engine.Physics.Collisions.Callbacks.CollisionJumpTable;
import com.cluster.engine.Physics.Collisions.Manifold;
import com.cluster.engine.Physics.Transform;
import com.cluster.engine.Utilities.ContentManager;
import com.cluster.engine.Utilities.State.GameStateManager;
import com.cluster.engine.Utilities.State.State;
import com.pixeldot.firewall.Entities.Guard;
import com.pixeldot.firewall.Entities.Player;
import com.pixeldot.firewall.Entities.Terminal;
import com.pixeldot.firewall.Firewall;
import com.pixeldot.firewall.Level.Blocks.Block;
import com.pixeldot.firewall.Level.Blocks.Door;
import org.jsfml.graphics.Font;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.View;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;

import java.util.ArrayList;

public class Level extends State {

    Player player;

    ArrayList<Block> blocks;
    ArrayList<Guard> guards;
    ArrayList<Terminal> terminals;
    ArrayList<Door> doors;

    private Font font;

    private boolean fromEditor;

    public Level(GameStateManager gsm, boolean fromEditor) {
        super(gsm);

        font = ContentManager.getInstance().getFont("Main");

        blocks = new ArrayList<>();
        guards = new ArrayList<>();
        terminals = new ArrayList<>();
        doors = new ArrayList<>();

        this.fromEditor = fromEditor;
    }

    public void update(float dt) {
        if(fromEditor && Keyboard.isKeyPressed(Keyboard.Key.ESCAPE)) {
            gsm.popState();
        }

        player.update(dt);

        checkCollisions();

        for(Guard guard : guards) {
            guard.update(dt);
            guard.seePlayer(player.getPosition());
        }

        for(Terminal terminal : terminals) {
            terminal.checkPlayer(player.getPosition());
            terminal.update(dt);
        }

        Vector2f pos = player.getPosition();
        view.setCenter(pos.x + Player.SIZE.x / 2f, pos.y + Player.SIZE.y / 2f);
        window.setView(view);
    }

    @Override
    public void render() {

        for(Block block : blocks) { block.render(window); }
        for(Guard guard : guards) { guard.render(window); }
        for(Terminal terminal : terminals) { terminal.render(window); }

        for(Door door : doors) { door.render(window); }

        player.render(window);

        View guiView = new View(new Vector2f(Firewall.WIDTH / 2f, Firewall.HEIGHT / 2f),
                new Vector2f(Firewall.WIDTH, Firewall.HEIGHT));
        window.setView(guiView);

        Text text = new Text("FPS: " + game.getEngine().getFramerate(), font, 25);
        text.setPosition(10, 10);
        window.draw(text);

        window.setView(view);
    }

    private void checkCollisions() {

        boolean hasCollided = false;

        for(Block block : blocks) {
            Vector2f pos = Vector2f.add(player.getPosition(), new Vector2f(Player.SIZE.x / 2f, Player.SIZE.y / 2f));
            Transform txA = new Transform(pos, 0);

            pos = Vector2f.add(block.getPosition(), new Vector2f(block.getSize().x / 2f, block.getSize().y / 2f));
            Transform txB = new Transform(pos, 0);

            Manifold m = new Manifold(txA, txB);
            CollisionJumpTable.handlers[0][0].handleCollision(m, player.getShape(), block.getShape());
            if(m.collided) {
                player.setPosition(Vector2f.sub(player.getPosition(), Vector2f.mul(m.normal, m.overlap)));
                hasCollided = true;
            }
        }

        for(Door door : doors) {
            if(door.isOpen()) continue;

            Vector2f pos = Vector2f.add(player.getPosition(), new Vector2f(Player.SIZE.x / 2f, Player.SIZE.y / 2f));
            Transform txA = new Transform(pos, 0);

            pos = Vector2f.add(door.getPosition(), new Vector2f(door.getSize().x / 2f, door.getSize().y / 2f));
            Transform txB = new Transform(pos, 0);

            Manifold m = new Manifold(txA, txB);
            CollisionJumpTable.handlers[0][0].handleCollision(m, player.getShape(), door.getShape());
            if(m.collided) {
                player.setPosition(Vector2f.sub(player.getPosition(), Vector2f.mul(m.normal, m.overlap)));
                hasCollided = true;
            }

        }

        player.setColliding(hasCollided);


    }
}
