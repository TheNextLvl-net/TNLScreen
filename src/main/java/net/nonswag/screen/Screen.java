package net.nonswag.screen;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Screen {
    private final int id;
    private final String name, date;
    private final State state;

    public String getFullName() {
        return getId() + "." + getName();
    }

    public void terminate() throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"screen", "-X", "-S", getFullName(), "quit"});
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public void wipe() throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"screen", "-wipe", getFullName()});
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public void detach() throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"screen", "-d", getFullName()});
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public void run(String command) throws ScreenException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"screen", "-S", getFullName(), "-X", "stuff", command + "\\n"});
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public static void terminate(String... screens) throws ScreenException {
        try {
            for (String name : screens) for (Screen screen : getScreens(name)) screen.terminate();
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public static void wipe(String... screens) throws ScreenException {
        try {
            if (screens.length == 0) {
                Process process = Runtime.getRuntime().exec(new String[]{"screen", "-wipe"});
                process.waitFor();
                process.destroy();
            } else {
                for (String name : screens) for (Screen screen : getScreens(name)) screen.wipe();
            }
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public static void detach(String... screens) throws ScreenException {
        try {
            if (screens.length == 0) {
                Process process = Runtime.getRuntime().exec(new String[]{"screen", "-d"});
                process.waitFor();
                process.destroy();
            } else {
                for (String name : screens) for (Screen screen : getScreens(name)) screen.detach();
            }
        } catch (Exception e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

    public static List<Screen> getScreens(String name) throws ScreenException {
        List<Screen> screens = new ArrayList<>();
        for (Screen screen : getScreens()) if (screen.getName().equals(name)) screens.add(screen);
        return screens;
    }

    public static Process start(String name, @Nullable File directory, String command) throws ScreenException {
        try {
            if (command.isEmpty()) throw new ScreenException("Command can't be empty");
            List<String> commands = new ArrayList<>();
            commands.add("screen");
            commands.add("-dmS");
            commands.add(name);
            commands.addAll(Arrays.asList(command.split(" ")));
            return Runtime.getRuntime().exec(commands.toArray(new String[]{}), null, directory);
        } catch (IOException e) {
            throw new ScreenException(e.getMessage(), e);
        }
    }

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

    public static List<String> list() throws ScreenException {
        List<String> callback = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"screen", "-list"});
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string;
            while ((string = br.readLine()) != null) callback.add(string);
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
        public static State getByName(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
