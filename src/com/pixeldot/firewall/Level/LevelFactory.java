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

import com.cluster.engine.Utilities.State.GameStateManager;
import com.pixeldot.firewall.Entities.Guard;
import com.pixeldot.firewall.Entities.Player;
import com.pixeldot.firewall.Entities.Terminal;
import com.pixeldot.firewall.Level.Blocks.Cover;
import com.pixeldot.firewall.Level.Blocks.Door;
import com.pixeldot.firewall.Level.Blocks.Wall;
import org.jsfml.system.Vector2f;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LevelFactory {

    private LevelFactory() {}

    public static Level loadLevel(GameStateManager gsm, String filename, boolean fromEditor) {
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

        Level result = new Level(gsm, fromEditor);

        while (buffer.position() < length) {
            MapObject o = new MapObject();
            o.type = buffer.getInt();

            BlockType type = BlockType.values()[o.type];

            if (type != BlockType.Guard) {
                o.position = new Vector2f(buffer.getFloat(), buffer.getFloat());
                o.size = new Vector2f(buffer.getFloat(), buffer.getFloat());

                switch (type) {
                    case Wall:
                        result.blocks.add(new Wall(o.position, o.size));
                        break;
                    case Spawn:
                        result.player = new Player(o.position, null);
                        break;
                    case Cover:
                        result.blocks.add(new Cover(o.position, o.size));
                        break;
                    case Terminal:
                        result.terminals.add(new Terminal(o.position));
                        break;
                    case Door:
                        result.doors.add(new Door(o.position, o.size));
                }
            }
            else {
                o.pathLength = buffer.getInt();
                o.direction = buffer.getInt();
                o.path = new Vector2f[o.pathLength];
                for(int i = 0; i < o.pathLength; i++) {
                    o.path[i] = new Vector2f(buffer.getFloat(), buffer.getFloat());
                }

                Guard guard = new Guard(o.path, o.direction);
                result.guards.add(guard);
            }
        }
        return result;
    }

}

