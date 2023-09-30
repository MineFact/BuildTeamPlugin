package net.buildtheearth.utils;

import net.buildtheearth.Main;
import net.buildtheearth.buildteam.components.universal_experience.universal_tpll.projection.Airocean;
import net.buildtheearth.buildteam.components.universal_experience.universal_tpll.projection.ModifiedAirocean;
import net.buildtheearth.terraminusminus.util.geo.LatLng; this needs to be changed to the new one
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class GeometricUtils
{
    /**
     * Creates a minecraft location object for the specified coordinates, yaw and pitch from the BTE projection.. Height is extracted from the world.
     * The world is extracted from the server config's "earth world". If no earth world is specified then the height defaults to 64
     * and the world is nullified.
     * @param coordinates Latitude and longitude of the location
     * @param fYaw Player's yaw
     * @param fPitch Player's pitch
     * @return A bukkit location matching the coordinates, yaw and pitch specified.
     */
    public static Location getLocationFromCoordinatesYawPitch(LatLng coordinates, float fYaw, float fPitch)
    {
        Airocean projection = new ModifiedAirocean();
        double mpu = projection.metersPerUnit();

        double[] xz = projection.fromGeo(coordinates.getLat(), coordinates.getLng());

        double x = xz[0] * mpu;
        double z = -xz[1] * mpu;

        //Creates the location
        Location location;
        World tpWorld = Bukkit.getWorld(Main.instance.getConfig().getString("universal_tpll.earth_world"));
        if (tpWorld == null)
            location = new Location(null, x, 64, z, fYaw, fPitch);
        else
            location = new Location(tpWorld, x, (tpWorld.getHighestBlockYAt((int) x, (int) z) + 1), z, fYaw, fPitch);

        return location;
    }
}