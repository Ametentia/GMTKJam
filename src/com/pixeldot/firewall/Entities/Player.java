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
package com.pixeldot.firewall.Entities;

import com.cluster.engine.Graphics.Animation;
import com.cluster.engine.Physics.Shapes.Polygon;
import com.cluster.engine.Utilities.Interfaces.EntityRenderable;
import com.cluster.engine.Utilities.Interfaces.Updateable;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Shape;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;

public class Player implements Updateable, EntityRenderable {

    public static final Vector2f SIZE = new Vector2f(20, 20);

    private Animation animation;
    private Vector2f position;

    private Polygon shape;
    private boolean colliding;

    public Player(Vector2f position, Animation animation) {
        this.position = position;
        this.animation = animation;
        colliding = false;

        float hw = SIZE.x / 2f;
        float hh = SIZE.y / 2f;
        Vector2f[] vertices = {
                new Vector2f(-hw, -hh), new Vector2f(hw, -hh),
                new Vector2f(hw, hh), new Vector2f(-hw, hh)
        };

        shape = new Polygon(vertices);
        ((Shape) shape.getDrawable()).setFillColor(Color.GREEN);
    }

    public void update(float dt) {
        float x = 0;
        float y = 0;

        if(Keyboard.isKeyPressed(Keyboard.Key.A)) {
            x = -300;
        }
        else if(Keyboard.isKeyPressed(Keyboard.Key.D)) {
            x = 300;
        }

        if(Keyboard.isKeyPressed(Keyboard.Key.W)) {
            y = -300;
        }
        else if(Keyboard.isKeyPressed(Keyboard.Key.S)) {
            y = 300;
        }

        if(!colliding) {
            position = Vector2f.add(position, new Vector2f(x * dt, y * dt));
        }
    }

    public void render(RenderWindow renderer) {
        Shape s = (Shape) shape.getDrawable();
        //s.setPosition(position);
        s.setPosition(position.x + SIZE.x / 2f, position.y + SIZE.y / 2f);

        renderer.draw(s);
    }

    public Polygon getShape() { return shape; }
    public Vector2f getPosition() { return position; }

    public void setColliding(boolean colliding) {
        this.colliding = colliding;
    }
    public void setPosition(Vector2f position) { this.position = position; }
}
