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
import com.cluster.engine.Utilities.MUtil;
import com.cluster.engine.Utilities.VUtil;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

public class Guard implements Updateable, EntityRenderable {

    private static final float sin = MUtil.sin(45 * MUtil.DEG_TO_RAD);
    private static final float cos = MUtil.cos(45 * MUtil.DEG_TO_RAD);
    private Vector2f position;


    private int index;
    private Vector2f[] path;

    private Vector2f facing;
    private int direction;

    private float speed;

    public Guard(Vector2f[] path, int direction) {
        position = path[0];
        this.path = path;
        index = 1;


        facing = VUtil.normalise(Vector2f.sub(path[1], path[0]));

        if(direction == 1) {
            facing = Vector2f.neg(facing);
        }

        this.direction = direction;

        speed = 60;
    }

    public void update(float dt) {
        Vector2f movement = Vector2f.mul(facing, dt * speed);
        position = Vector2f.add(position, movement);

        float len = VUtil.lengthSq(Vector2f.sub(path[index], position));
        if(MUtil.isZero(len, 0.5f)) {
            position = path[index];
            index = index + 1 == path.length ? 0 : index + 1;
            facing = VUtil.normalise(Vector2f.sub(path[index], position));

            if(direction == 1)  {
                facing = Vector2f.neg(facing);
            }
        }
    }

    public void render(RenderWindow renderer) {
        RectangleShape shape = new RectangleShape(new Vector2f(20, 20));
        shape.setPosition(Vector2f.sub(position, new Vector2f(10, 10)));
        shape.setFillColor(Color.RED);
        renderer.draw(shape);
/*
        for(int i = 0, j = path.length - 1; i < path.length; j = i++) {
            Vector2f p1 = path[i];
            Vector2f p2 = path[j];

            Vertex[] v = {
                    new Vertex(p1, Color.RED), new Vertex(p2, Color.RED)
            };
            renderer.draw(v, PrimitiveType.LINES);
        }
*/
    }

    public boolean seePlayer(Vector2f playerPos) {
        Vector2f dir = VUtil.normalise(Vector2f.sub(playerPos, position));

        boolean result = VUtil.dot(dir, facing) > 0.8f;
        if(result) {
            facing = dir;
        }
        return result;
    }

    public void setPath(Vector2f[] path) {
        position = path[0];
        index = 1;
        facing = VUtil.normalise(Vector2f.sub(path[index], path[0]));
        this.path = path;
    }

    public Vector2f getPosition() { return position; }
}
