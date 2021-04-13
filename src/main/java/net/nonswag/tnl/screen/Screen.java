package net.nonswag.tnl.screen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Screen {

    private final int id;
    @Nonnull
    private final String name;
    @Nonnull
    private final String date;
    @Nonnull
    private final State state;

    public Screen(int id, @Nonnull String name, @Nonnull String date, @Nonnull State state) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDate() {
        return date;
    }

    @Nonnull
    public State getState() {
        return state;
    }

    public void terminate() throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec("screen -X -S " + getId() + "." + getName() + " quit");
            process.waitFor();
            process.destroy();
            System.out.println("terminated screen " + getName() + " with id " + getId());
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public void wipe() throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec("screen -wipe " + getId() + "." + getName());
            process.waitFor();
            process.destroy();
            System.out.println("wiped screen " + getName() + " with id " + getId());
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "net.nonswag.tnl.screen.Screen{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Screen screen = (Screen) o;
        return id == screen.id && name.equals(screen.name) && date.equals(screen.date) && state == screen.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, state);
    }

    public static void terminate(@Nonnull String... screens) throws ScreenException {
        try {
            for (String name : screens) {
                for (Screen screen : getScreens(name)) {
                    screen.terminate();
                }
            }
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public static void wipe(@Nonnull String... screens) throws ScreenException {
        try {
            if (screens.length == 0) {
                Process process = Runtime.getRuntime().exec("screen -wipe");
                process.waitFor();
                process.destroy();
            } else {
                for (String name : screens) {
                    for (Screen screen : getScreens(name)) {
                        screen.wipe();
                    }
                }
            }
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    @Nonnull
    public static List<Screen> getScreens(@Nonnull String name) throws ScreenException {
        List<Screen> screens = new ArrayList<>();
        for (Screen screen : getScreens()) {
            if (screen.getName().equals(name)) {
                screens.add(screen);
            }
        }
        return screens;
    }

    @Nonnull
    public static Process start(@Nonnull String name, @Nullable File directory, @Nonnull String command) throws ScreenException {
        try {
            if (command.isEmpty()) {
                throw new ScreenException("Command can't be empty");
            }
            return Runtime.getRuntime().exec("screen -dmS " + name + " " + command, null, directory);
        } catch (IOException e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    @Nonnull
    public static List<Screen> getScreens() throws ScreenException {
        List<Screen> screens = new ArrayList<>();
        try {
            for (String s : list()) {
                String[] split = s.split("\t");
                if (split.length == 4) {
                    State state = State.getByName(split[3].replace("(", "").replace(")", ""));
                    if (state != null) {
                        int id = Integer.parseInt(split[1].split("\\.")[0]);
                        screens.add(new Screen(id, split[1].replace(id + ".", ""), split[2].replace("(", "").replace(")", ""), state));
                    }
                }
            }
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
        return screens;
    }

    @Nonnull
    public static List<String> list() throws ScreenException {
        List<String> callback = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("screen -list");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string;
            while ((string = br.readLine()) != null) {
                callback.add(string);
            }
            process.waitFor();
            process.destroy();
            br.close();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
        return callback;
    }

    public enum State {
        ATTACHED,
        DETACHED,
        TERMINATED;

        @Nullable
        public static State getByName(@Nonnull String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
