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

import com.cluster.engine.Utilities.Interfaces.EntityRenderable;
import com.cluster.engine.Utilities.Interfaces.Updateable;
import com.cluster.engine.Utilities.VUtil;
import org.jsfml.graphics.CircleShape;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RectangleShape;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.system.Vector2f;

public class Terminal implements Updateable, EntityRenderable {

    private static final float radius = 45;

    private Vector2f position;
    private boolean used;
    private boolean playerNear;
    private float currentR;

    public Terminal(Vector2f position) {
        this.position = position;
        used = false;
        playerNear = false;
        currentR = 0;
    }

    public boolean checkPlayer(Vector2f playerPosition) {
        float dist = VUtil.lengthSq(Vector2f.sub(playerPosition, position));

        playerNear = !used && dist <= radius * radius;
        return playerNear;
    }

    @Override
    public void update(float dt) {
        if(playerNear) {
            currentR += (60 * dt);
            currentR = Math.min(currentR, radius);
        }
        else {
            currentR = 0;
        }
    }

    @Override
    public void render(RenderWindow renderer) {
        Vector2f size = Player.SIZE;

        if(playerNear) {
            CircleShape c = new CircleShape(currentR);
            c.setOrigin(currentR, currentR);
            c.setPosition(position.x + size.x / 2f, position.y + size.y / 2f);
            c.setOutlineThickness(1);
            c.setOutlineColor(Color.BLACK);
            c.setFillColor(Color.TRANSPARENT);
            renderer.draw(c);
        }

        RectangleShape shape = new RectangleShape(size);
        shape.setPosition(position);
        shape.setFillColor(Color.BLACK);
        renderer.draw(shape);
    }

    public void use() { used = true; }
}
