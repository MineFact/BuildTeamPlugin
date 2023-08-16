package net.buildtheearth.buildteam.components.generator.field;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import net.buildtheearth.Main;
import net.buildtheearth.buildteam.components.generator.Command;
import net.buildtheearth.buildteam.components.generator.Generator;
import net.buildtheearth.buildteam.components.generator.GeneratorType;
import net.buildtheearth.buildteam.components.generator.History;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FieldScripts {

    public static final String LINE_WIKI = "a"; //TODO ADD WIKI PAGE

    public static void fieldscript_v_1_0(Player p, Field field, Region region) {
        List<String> commands = new ArrayList<>();
        HashMap<Object, String> flags = field.getPlayerSettings().get(p.getUniqueId()).getValues();

        // Settings
        Crop crop = Crop.getByIdentifier(flags.get(FieldFlag.CROP));
        CropStage type = CropStage.getByIdentifier(flags.get(FieldFlag.TYPE));
        String fence = flags.get(FieldFlag.FENCE);

        // Information for later restoring original selection
        List<BlockVector2D> selectionPoints = new ArrayList<>();
        int minY = region.getMinimumPoint().getBlockY();
        int maxY = region.getMaximumPoint().getBlockY();


        if(region instanceof Polygonal2DRegion){
            Polygonal2DRegion polyRegion = (Polygonal2DRegion) region;
            selectionPoints.addAll(polyRegion.getPoints());

        }else if(region instanceof CuboidRegion){
            CuboidRegion cuboidRegion = (CuboidRegion) region;
            Vector min = cuboidRegion.getMinimumPoint();
            Vector max = cuboidRegion.getMaximumPoint();

            selectionPoints.add(new BlockVector2D(min.getBlockX(), min.getBlockZ()));
            selectionPoints.add(new BlockVector2D(max.getBlockX(), max.getBlockZ()));
        }else{
            p.sendMessage("§c§lERROR: §cRegion type not supported!");
            return;
        }

        // ----------- FIELD GENERATOR SCRIPT ----------
        // Used to generate fields

        int operations = 0;

        commands.add("/clearhistory");
        commands.add("//gmask");

        // ----------- PREPARATION 01 ----------
        // Preparing the field area

        commands.add("//expand 10 up");
        commands.add("//expand 10 down");

        // Remove all non-solid blocks
        commands.add("//gmask !#solid");
        commands.add("//replace 0");
        operations++;

        // Remove all trees and pumpkins
        commands.add("//gmask");
        commands.add("//replace leaves,log,pumpkin 0");
        operations++;

        Block[][][] blocks = Generator.analyzeRegion(p, p.getWorld());
        int maxHeight = Generator.getMaxHeight(blocks);

        //In case the player placed yellow wool for a crop which doesn't require it
        if(!crop.isLinesRequired()) {
            commands.add("//replace 35:4 0");
        }

        // Replace the field area by lime wool
        commands.add("//gmask <0,35:4");
        commands.add("//replace !35:4 35:5");
        operations++;

        // Set remembering blocks 5 blocks below lime blocks
        commands.add("//gmask =queryRel(0,5,0,35,5)");
        commands.add("//set 7");
        operations++;



        // ----------- PREPARATION 02 ----------
        // Drawing lines if the crop requires it

        if(crop.isLinesRequired()){
            // Return if there aren't at least 2 yellow wool blocks inside the selection
            if(!Generator.containsBlock(blocks, Material.WOOL, (byte) 4, 2)) {
                p.sendMessage("§cYou need to place at least 2 yellow wool blocks inside your selection!");
                p.sendMessage(" ");
                p.sendMessage("§cFor more information, please see the wiki:");
                p.sendMessage("§c" + LINE_WIKI);
                return;
            }

            // Get the most west and most east yellow block to draw a line.
            List<Block> yellowWoolBlocks = Generator.getBlocksOfMaterial(blocks, Material.WOOL, (byte) 4);
            Block westernMost = null;
            Block easternMost = null;
            double currentLowest = Double.MAX_VALUE;
            double currentHighest = Double.MIN_VALUE;

            for(Block yellowWoolBlock : yellowWoolBlocks) {
                if(yellowWoolBlock.getLocation().getBlockX() < currentLowest) {
                    currentLowest = yellowWoolBlock.getLocation().getBlockX();
                    westernMost = yellowWoolBlock;
                }
            }

            for(Block yellowWoolBlock : yellowWoolBlocks) {
                if(yellowWoolBlock.getLocation().getBlockX() > currentHighest) {
                    currentHighest = yellowWoolBlock.getLocation().getBlockX();
                    easternMost = yellowWoolBlock;
                }
            }

            if(westernMost == null || easternMost == null) {
                p.sendMessage("§cSomething went wrong while processing line data!");
                p.sendMessage(" ");
                p.sendMessage("§cPlease contact one of the developers!");
                return;
            }

            int xTargetLeft = region.getMinimumPoint().getBlockX();
            int xTargetRight = region.getMaximumPoint().getBlockX();

            int zLeft = (int) interpolateCoordinates(westernMost.getX(), westernMost.getZ(), easternMost.getX(), easternMost.getZ(), xTargetLeft);
            int zRight = (int) interpolateCoordinates(westernMost.getX(), westernMost.getZ(), easternMost.getX(), easternMost.getZ(), xTargetRight);

            //Draw the first line
            commands.add("//sel cuboid");
            commands.add("//gmask !air"); // Else the line height can't be properly adjusted

            commands.add("//pos1 " + xTargetLeft + "," + (westernMost.getY() - 2) + "," + zLeft); // New western most
            commands.add("//pos2 " + xTargetRight + "," + (easternMost.getY() - 2) + "," + zRight); // New eastern most
            commands.add("//line 35:4");
            operations++;


            commands.add("//expand 10 up");
            commands.add("//expand 10 down");

            commands.add("//gmask !7,0");
            for(int i = maxHeight; i < maxHeight + 5; i++) {
                commands.add("//replace >35:4 35:4");
                commands.add("//replace <35:4 35:4");
                operations++;
            }

            int requiredRepetitions = (region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ());


            //Select original region
            commands.add("//pos1 " + region.getMaximumPoint().getBlockX() + "," + region.getMaximumPoint().getBlockY() + "," + region.getMaximumPoint().getBlockZ());
            commands.add("//pos2 " + region.getMinimumPoint().getBlockX() + "," + region.getMinimumPoint().getBlockY() + "," + region.getMinimumPoint().getBlockZ());

            //Remove extra non solid blocks
            commands.add("//replace !#solid 0");
            operations++;

            //Make original line correct shape
            commands.add("//gmask =queryRel(1,0,0,35,4)&&queryRel(-1,0,+1,35,4)");
            commands.add("//set 35:4");
            operations++;

            //Make the line pattern extend over the field
            for(int i = 0; i <= requiredRepetitions; i++) {
                //Orange wool
                commands.add("//gmask =queryRel(0,0,-1,35,4)||queryRel(0,0,+1,35,4)||queryRel(0,1,-1,35,4)||queryRel(0,1,+1,35,4)||queryRel(0,-1,-1,35,4)||queryRel(0,-1,+1,35,4)");
                commands.add("//replace !35:4,0 35:1");
                operations++;
                //Yellow wool
                commands.add("//gmask =queryRel(0,0,-1,35,1)||queryRel(-1,0,-1,35,1)||queryRel(0,0,+1,35,1)||queryRel(+1,1,+1,35,1)||queryRel(0,0,+1,35,1)||queryRel(+1,0,+1,35,1)||queryRel(+1,-1,+1,35,1)||queryRel(-1,1,-1,35,1)||queryRel(-1,-1,-1,35,1)");
                commands.add("//replace !35:1,0 35:4");
                operations++;
            }

            // Restore field to original shape
            commands.add("//gmask !=queryRel(0,-5,0,7,0)");
            commands.add("//replace !0 2");
            operations++;

            // Remove original yellow wool blocks

            commands.add("//gmask =queryRel(0,-6,0,7,0)");
            commands.add("//replace 2 0");
            operations++;
        }



        // ----------- PLACING CROPS ----------
        // Placing the crops

        commands.add("//gmask");

        // First reselect the original poly region

        commands.add("//sel poly");

        commands.add("//pos1 " + selectionPoints.get(0).getBlockX() + "," + maxY + "," + selectionPoints.get(0).getBlockZ());
        for(int i = 1; i < selectionPoints.size(); i++) {
            commands.add("//pos2 " + selectionPoints.get(i).getBlockX() + "," + minY + "," + selectionPoints.get(i).getBlockZ());
        }

        // Fill with potato crop
        if(crop == Crop.POTATO) {
            if(type == CropStage.TALL) {
                commands.add("//replace 35:4 24%3,24%3:1,1%17:4,1%5:1");
                operations++;
                commands.add("//replace 35:1 1%3,1%3:1,24%17:4,24%5:1");
                operations++;

                commands.add("//shift 1 up");
                commands.add("//gmask 0");

                commands.add("//replace >3 35:1,35:2,31:1,31:2");
                operations++;

                commands.add("//gmask");

                commands.add("//replace 35:1 175:3");
                operations++;
                commands.add("//replace 35:2 175:2");
                operations++;

                commands.add("//shift 1 up");

                commands.add("//replace >175:3 175:15");
                operations++;
                commands.add("//replace >175:2 175:14");
                operations++;

            } else {
                commands.add("//replace 35:4 208,5");
                operations++;
                commands.add("//replace 35:1 252:13,2");
                operations++;

                commands.add("//shift 1 up");
                commands.add("//gmask 0");

                commands.add("//replace >2 31:1,31:2");
                operations++;

            }
        }




        Main.buildTeamTools.getGenerator().getCommands().add(new Command(p, field, commands, operations, blocks));
        Generator.getPlayerHistory(p).addHistoryEntry(new History.HistoryEntry(GeneratorType.FIELD, operations));
    }


    public static double interpolateCoordinates(double x1, double z1, double x2, double z2, double targetX) {
        if (x1 == x2) {
            // If the start and end points have the same X-coordinate, return the Z-coordinate of the start point
            return z1;
        }

        // Calculate the proportional distance between c1 and c2 for the target X value
        double t = (targetX - x1) / (x2 - x1);

        // Interpolate the coordinate based on the target X value
        return z1 + (z2 - z1) * t;
    }
}
