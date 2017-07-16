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
import com.cluster.engine.Utilities.MUtil;
import com.cluster.engine.Utilities.State.GameStateManager;
import com.cluster.engine.Utilities.State.State;
import com.pixeldot.firewall.Firewall;
import com.pixeldot.firewall.Util.Input;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.Font;
import org.jsfml.graphics.RectangleShape;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;

public class Hacking extends State {

    private static final String[] commands = { "mov", "jmp", "add", "call", "inc", "or", "sft", "and" };

    private PlayState background;
    private Font font;

    private int commandCount;
    private String command;
    private int index;

    private float topline;
    private String terminalOutput;
    private int minLength;

    private Input input;

    public Hacking(GameStateManager gsm, PlayState state) {
        super(gsm);
        background = state;

        font = ContentManager.getInstance().loadFont("Mono", "Mono.ttf");
        command = commands[MUtil.randomInt(0, commands.length)];
        commandCount = MUtil.randomInt(5, 8);
        index = 0;

        topline = Firewall.HEIGHT - 155;
        terminalOutput = "Type Command: " + command + "\nroot:/$ ";
        minLength = terminalOutput.length();

        input = new Input();
        game.getEngine().setInputHandler(input);
    }

    public void update(float dt) {
        Keyboard.Key last = input.getLastPressed();

        if(last == Keyboard.Key.RETURN) {
            if(terminalOutput.endsWith(command)) {
                System.out.println("Correct!");
                if(index == commandCount) {
                    System.out.println("Hacking Complete");
                    gsm.popState();
                }

                index++;
                command = commands[MUtil.randomInt(0, commands.length)];
                terminalOutput += "\nType: " + command;
                topline -= 25;
            }

            terminalOutput += "\nroot:/$ ";
            topline -= 25;
            minLength = terminalOutput.length();
        }
        else if(last == Keyboard.Key.BACKSPACE) {
            String tmp = terminalOutput.substring(0, terminalOutput.length() - 1);
            if(tmp.length() >= minLength) {
                terminalOutput = tmp;
            }
        }
        else if(last.ordinal() >= 1 && last.ordinal() < 27) {
            terminalOutput += last.toString().toLowerCase();
        }

        if(last != Keyboard.Key.BACKSPACE) input.clear();
    }

    public void render() {
        background.render();


        RectangleShape shape = new RectangleShape(new Vector2f(Firewall.WIDTH - 180, Firewall.HEIGHT - 180));
        shape.setFillColor(new Color(0, 0, 0, 170));
        shape.setOutlineThickness(10);
        shape.setOutlineColor(new Color(70, 70, 70, 170));
        shape.setPosition(90, 90);
        window.draw(shape);

        Text text = new Text(terminalOutput, font, 25);
        text.setPosition(100, topline);
        text.setColor(Color.GREEN);
        window.draw(text);
    }

    public void dispose() {}
}
