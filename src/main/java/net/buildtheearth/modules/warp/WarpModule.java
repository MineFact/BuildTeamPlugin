package net.buildtheearth.modules.warp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.buildtheearth.Main;
import net.buildtheearth.modules.Module;
import net.buildtheearth.modules.network.api.NetworkAPI;
import net.buildtheearth.modules.network.api.OpenStreetMapAPI;
import net.buildtheearth.modules.network.model.BuildTeam;
import net.buildtheearth.modules.utils.ChatHelper;
import net.buildtheearth.modules.utils.GeometricUtils;
import net.buildtheearth.modules.utils.geo.CoordinateConversion;
import net.buildtheearth.modules.warp.menu.WarpEditMenu;
import net.buildtheearth.modules.warp.menu.WarpGroupEditMenu;
import net.buildtheearth.modules.warp.model.Warp;
import net.buildtheearth.modules.warp.model.WarpGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpModule implements Module {


    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public String getModuleName() {
        return "Warps";
    }

    /**
     * Stores a List of the warp operations that need to happen on join
     */
    private static final HashMap<UUID, Location> warpQueue = new HashMap<>();

    /**
     * Adds a warp operation to the queue
     *
     * @param in     The ByteArray received through the PluginMessageChannel
     * @param player The player to whom the warp operation belongs
     */
    public static void addWarpToQueue(ByteArrayDataInput in, Player player) {
        //Check the target server
        String targetServer = in.readUTF();
        if (targetServer.equals(Main.getBuildTeamTools().getProxyModule().getBuildTeam().getServerName())) {
            //Extracts the warp key from the plugin message
            String warpKey = in.readUTF();

            Warp warp = NetworkAPI.getWarpByKey(warpKey);

            if (warp == null) {
                player.sendMessage(ChatHelper.error("The warp you tried to warp to does not exist anymore."));
                return;
            }

            Location targetWarpLocation = GeometricUtils.getLocationFromCoordinatesYawPitch(new double[]{warp.getLat(), warp.getLon()}, warp.getYaw(), warp.getPitch());
            targetWarpLocation.setY(warp.getY());
            targetWarpLocation.setWorld(Bukkit.getWorld(warp.getWorldName()));

            // Adds the event to the list, to be dealt with by the join listener
            warpQueue.put(player.getUniqueId(), targetWarpLocation);
        }
    }

    /**
     * Checks if there is a warp in the queue of the current server and teleports the player if this is the case
     * @param player the player to check the queue for
     */
    public static void processQueueForPlayer(Player player) {
        Location targetWarpLocation = warpQueue.get(player.getUniqueId());

        if (targetWarpLocation == null) {
            return;
        }

        if(player.teleport(targetWarpLocation)) {
            player.sendMessage(ChatHelper.successful("Successfully warped you to the desired location!"));
        } else {
            player.sendMessage(ChatHelper.error("Something went wrong trying to warp you to the desired location."));
        }

        warpQueue.remove(player.getUniqueId());
    }


    /**
     * Sends a plugin message to add the warp to the queue of the target server
     * Then switches the player to that server
     * @param player The player to warp
     * @param warp The warp to teleport the player to
     */
    public static void warpPlayer(Player player, Warp warp) {
        // If the warp is in the same team, just teleport the player
        if(warp.getWarpGroup().getBuildTeam().getID().equals(Main.getBuildTeamTools().getProxyModule().getBuildTeam().getID())) {
            Location loc = GeometricUtils.getLocationFromCoordinatesYawPitch(new double[]{warp.getLat(), warp.getLon()}, warp.getYaw(), warp.getPitch());

            if(loc.getWorld() == null) {
                World world = Bukkit.getWorld(warp.getWorldName()) == null ? player.getWorld() : Bukkit.getWorld(warp.getWorldName());
                loc.setWorld(world);
            }

            loc.setY(warp.getY());

            player.teleport(loc);
            player.sendMessage(ChatHelper.successful("Successfully warped you to %s.", warp.getName()));

            return;
        }

        // Get the server the warp is on
        String targetServer = "Empty"; // NetworkAPI.getServerNameByCountryCode(warp.getCountryCode());

        // Send a plugin message to that server which adds the warp to the queue
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("UniversalWarps");
        out.writeUTF(targetServer);
        out.writeUTF(warp.getId().toString());

        player.sendPluginMessage(Main.instance, "BuildTeam", out.toByteArray());

        // Switch the player to the target server
        Main.getBuildTeamTools().getProxyModule().switchServer(player, targetServer);
    }



    /** Creates a warp at the player's location and opens the warp edit menu.
     *
     * @param creator The player that is creating the warp
     */
    public static void createWarp(Player creator){
        // Get the geographic coordinates of the player's location.
        Location location = creator.getLocation();
        double[] coordinates = CoordinateConversion.convertToGeo(location.getX(), location.getZ());

        //Get the country belonging to the coordinates
        CompletableFuture<String[]> future = OpenStreetMapAPI.getCountryFromLocationAsync(coordinates);

        future.thenAccept(result -> {
            String regionName = result[0];
            String countryCodeCCA2 = result[1].toUpperCase();

            //Check if the team owns this region/country
            boolean ownsRegion = Main.getBuildTeamTools().getProxyModule().ownsRegion(regionName, countryCodeCCA2);

            if(!ownsRegion) {
                creator.sendMessage(ChatHelper.error("This team does not own the country %s!", result[0]));
                return;
            }

            // Get the Other Group for default warp group
            WarpGroup group = Main.getBuildTeamTools().getProxyModule().getBuildTeam().getWarpGroups().stream().filter(warpGroup -> warpGroup.getName().equalsIgnoreCase("Other")).findFirst().orElse(null);

            // Create a default name for the warp
            String name = creator.getName() + "'s Warp";

            // Create an instance of the warp POJO
            Warp warp = new Warp(group, name, countryCodeCCA2, "cca2", null, null, null, location.getWorld().getName(), coordinates[0], coordinates[1], location.getY(), location.getYaw(), location.getPitch(), false);

            // Create the actual warp
            new WarpEditMenu(creator, warp, false);

        }).exceptionally(e -> {
            creator.sendMessage(ChatHelper.error("An error occurred while creating the warp!"));
            e.printStackTrace();
            return null;
        });
    }


    public static void createWarpGroup(Player creator){
        // Create a default name for the warp
        String name = creator.getName() + "'s Warp Group";
        String description = "This is a warp group.";

        WarpGroup warpGroup = new WarpGroup(Main.getBuildTeamTools().getProxyModule().getBuildTeam(), name, description);

        new WarpGroupEditMenu(creator, warpGroup, false);
    }


    // ------------------------- //
    //          GETTER           //
    // ------------------------- //

    public static Warp getWarpByName(String name){
        return getWarpByName(Main.getBuildTeamTools().getProxyModule().getBuildTeam(), name);
    }

    public static Warp getWarpByName(BuildTeam buildTeam, String name) {
        return buildTeam.getWarpGroups().stream().flatMap(warpGroup -> warpGroup.getWarps().stream())
                .filter(warp1 -> warp1.getName() != null && warp1.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}