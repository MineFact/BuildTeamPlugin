package net.buildtheearth.modules.generator.components.road;

import com.cryptomorin.xseries.XMaterial;
import net.buildtheearth.BuildTeamTools;
import net.buildtheearth.modules.generator.model.Flag;
import net.buildtheearth.modules.generator.model.Settings;
import net.buildtheearth.utils.Item;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RoadSettings extends Settings {

    public static List<String> streetLampTypes = new ArrayList<>();


    public RoadSettings(Player player){
        super(player);

        File directory = new File(BuildTeamTools.getInstance().getDataFolder().getAbsolutePath() + "/../WorldEdit/schematics/GeneratorCollections/roadpack/");
        File[] files = directory.listFiles();

        for(File file : files)
            if(file.getName().contains("streetlamp"))
                streetLampTypes.add(file.getName().replace(".schematic", "").replace("streetlamp", ""));
    }

    public void setDefaultValues(){

        // Lane Count (Default: Fixed Value)
        setValue(RoadFlag.LANE_COUNT, "2");

        // Lane Width (Default: Fixed Value)
        setValue(RoadFlag.LANE_WIDTH, "4");

        // Road Material (Default: Fixed Value)
        setValue(RoadFlag.ROAD_MATERIAL, Item.getUniqueMaterialString(XMaterial.GRAY_CONCRETE_POWDER.parseItem()));

        // Lane Gap (Default: Fixed Value)
        setValue(RoadFlag.LANE_GAP, "0");

        // Marking Length (Default: Fixed Value)
        setValue(RoadFlag.MARKING_LENGTH, "3");

        // Marking Gap (Default: Fixed Value)
        setValue(RoadFlag.MARKING_GAP, "5");

        // Marking Material (Default: Fixed Value)
        setValue(RoadFlag.MARKING_MATERIAL, Item.getUniqueMaterialString(XMaterial.WHITE_CONCRETE.parseItem()));

        // Sidewalk Width (Default: Fixed Value)
        setValue(RoadFlag.SIDEWALK_WIDTH, "5");

        // Sidewalk Material (Default: Fixed Value)
        setValue(RoadFlag.SIDEWALK_MATERIAL, Item.getUniqueMaterialString(XMaterial.STONE.parseItem()));

        // Sidewalk Slab Material (Default: Fixed Value)
        setValue(RoadFlag.SIDEWALK_SLAB_COLOR, Item.getUniqueMaterialString(XMaterial.STONE_SLAB.parseItem()));

        // Road Slab Material (Default: Fixed Value)
        setValue(RoadFlag.ROAD_SLAB_COLOR, Flag.DISABLED);

        // Crosswalk (Default: Fixed Value)
        setValue(RoadFlag.CROSSWALK, Flag.ENABLED);

        // Street Lamp Type (Default: Fixed Value)
        setValue(RoadFlag.STREET_LAMP_TYPE, "001");

        // Street Lamp Distance (Default: Fixed Value)
        setValue(RoadFlag.STREET_LAMP_DISTANCE, "40");

        // Road Side (Default: Fixed Value)
        setValue(RoadFlag.ROAD_SIDE, "10");
    }
}
