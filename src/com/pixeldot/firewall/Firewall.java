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
package com.pixeldot.firewall;

import com.cluster.engine.Game;
import com.cluster.engine.Utilities.State.GameStateManager;
import com.pixeldot.firewall.Level.LevelFactory;
import com.pixeldot.firewall.States.LevelEditor;
import com.pixeldot.firewall.States.PlayState;
import org.jsfml.graphics.Color;
import org.jsfml.window.Keyboard;

public class Firewall extends Game {

    // Width and height of the game
    // TODO(James): Make this editable
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private GameStateManager manager;
    private boolean pause;

    protected void initialise() {

        // Game state management initialisation
        manager = new GameStateManager(this);
        manager.addState(new LevelEditor(manager));
        pause = false;
    }

    public void update(float dt) {
        if(pause) return;
        manager.update(dt);
    }

    @Override
    public void pause() {
        pause = true;
    }

    @Override
    public void resume() {
        pause = false;
    }

    public void render() {
        window.clear(new Color(100, 149, 237));
        manager.render();
    }

    public void dispose() {}
}
