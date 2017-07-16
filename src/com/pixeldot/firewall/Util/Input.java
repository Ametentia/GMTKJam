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
package com.pixeldot.firewall.Util;

import com.cluster.engine.Input.InputHandler;
import org.jsfml.window.Keyboard;

public class Input extends InputHandler {

    private Keyboard.Key lastPressed;
    private int mwMove;

    public Input() {
        lastPressed = Keyboard.Key.UNKNOWN;
    }

    public void keyPressed(Keyboard.Key key) {
        lastPressed = key;
    }

    @Override
    public void keyReleased(Keyboard.Key key) {
        if(lastPressed == Keyboard.Key.BACKSPACE) {
            lastPressed = Keyboard.Key.UNKNOWN;
        }
    }

    @Override
    public void mouseWheelMoved(int amount) {
        // To zoom with the level editor
        mwMove = amount;
    }

    public Keyboard.Key getLastPressed() { return lastPressed; }
    public int getMWMoveAmount() { return mwMove; }

    public void clear() {
        lastPressed = Keyboard.Key.UNKNOWN;
        mwMove = 0;
    }
}
