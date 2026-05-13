package klaxon.klaxon.imperator;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.UUID;

public class Main {
    static void main(String[] args) throws Throwable {
        IO.println(System.getProperty("java.class.path"));
        if (args.length < 2) {
            throw new IllegalArgumentException("Did not receive launch class and args. Terminating.");
        }

        final var passthroughArgs = args[1]
                .replace("${auth_player_name}", "Developer")
                .replace("${version_name}", "1.7.10")
                .replace("${game_directory}", System.getProperty("user.dir"))
                .replace("${assets_root}", System.getProperty("user.dir"))
                .replace("${assets_index_name}", "nothing.json")
                // TODO get this from mojang API
                .replace("${auth_uuid}", UUID.nameUUIDFromBytes("Developer".getBytes()).toString())
                .replace("${auth_access_token}", "0")
                .replace("${user_properties}", "{}")
                .replace("--userType ${user_type}", "")
                .split(" ");
        IO.println(args[0]);
        IO.println(Arrays.toString(passthroughArgs));

        final var main = Class.forName(args[0]);
        final var lookup = MethodHandles.publicLookup();
        final var entrypoint = lookup.unreflect(main.getMethod("main", String[].class));

        // invokeExact *must* recieve this as a String[], not casting to Object!
        //noinspection ConfusingArgumentToVarargsMethod
        entrypoint.invokeExact(passthroughArgs);
    }
}
