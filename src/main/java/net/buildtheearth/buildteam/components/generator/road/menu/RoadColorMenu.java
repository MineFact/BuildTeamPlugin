package net.buildtheearth.buildteam.components.generator.road.menu;

import net.buildtheearth.Main;
import net.buildtheearth.buildteam.components.generator.Settings;
import net.buildtheearth.buildteam.components.generator.house.HouseFlag;
import net.buildtheearth.buildteam.components.generator.house.HouseSettings;
import net.buildtheearth.buildteam.components.generator.house.menu.RoofTypeMenu;
import net.buildtheearth.buildteam.components.generator.road.RoadFlag;
import net.buildtheearth.buildteam.components.generator.road.RoadSettings;
import net.buildtheearth.utils.AbstractPaginatedMenu;
import net.buildtheearth.utils.Item;
import net.buildtheearth.utils.MenuItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoadColorMenu extends AbstractPaginatedMenu {

    public static String ROAD_COLOR_INV_NAME = "Choose a Road Block";

    public static int SWITCH_PAGE_ITEM_SLOT = 31;
    public static int NEXT_ITEM_SLOT = 35;


    public ArrayList<String> selectedMaterials;


    public RoadColorMenu(Player player) {
        super(4, 3, ROAD_COLOR_INV_NAME, player);
    }

    @Override
    protected void setPreviewItems() {
        setSwitchPageItems(SWITCH_PAGE_ITEM_SLOT);

        if(canProceed())
            getMenu().getSlot(NEXT_ITEM_SLOT).setItem(MenuItems.getNextItem());
        else
            getMenu().getSlot(NEXT_ITEM_SLOT).setItem(MenuItems.ITEM_BACKGROUND);

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {}

    @Override
    protected void setItemClickEventsAsync() {
        setSwitchPageItemClickEvents(SWITCH_PAGE_ITEM_SLOT);

        // Set click event for next item
        if(canProceed())
            getMenu().getSlot(NEXT_ITEM_SLOT).setClickHandler((clickPlayer, clickInformation) -> {
                Settings settings = Main.buildTeamTools.getGenerator().getHouse().getPlayerSettings().get(clickPlayer.getUniqueId());

                if(!(settings instanceof RoadSettings))
                    return;

                RoadSettings roadSettings = (RoadSettings) settings;
                roadSettings.setValue(RoadFlag.ROAD_MATERIAL, Item.createStringFromItemList(selectedMaterials));

                clickPlayer.closeInventory();
                clickPlayer.playSound(clickPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

                new SidewalkColorMenu(clickPlayer);
            });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(Item.create(Material.STAINED_GLASS_PANE, " ", (short)15, null))
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111000110")
                .build();
    }

    @Override
    protected List<?> getSource() {
        return new ArrayList<>(Arrays.asList(MenuItems.BLOCKS_BY_COLOR_1_12));
    }

    @Override
    protected void setPaginatedPreviewItems(List<?> source) {
        if(selectedMaterials == null)
            selectedMaterials = new ArrayList<>();

        // Set pagignated items
        List<ItemStack> items = source.stream().map(l -> (ItemStack) l).collect(Collectors.toList());
        int slot = 0;
        for (ItemStack item : items) {
            if(selectedMaterials.contains(Item.getUniqueMaterialString(item)))
                item = new Item(item).setAmount(1).addEnchantment(Enchantment.LUCK, 1).hideEnchantments(true).build();

            getMenu().getSlot(slot).setItem(item);
            slot++;
        }
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        List<ItemStack> items = source.stream().map(l -> (ItemStack) l).collect(Collectors.toList());
        int slot = 0;
        for (ItemStack item : items) {
            final int _slot = slot;
            getMenu().getSlot(_slot).setClickHandler((clickPlayer, clickInformation) -> {
                String type = Item.getUniqueMaterialString(getMenu().getSlot(_slot).getItem(getMenuPlayer()));

                if(selectedMaterials.contains(type))
                    selectedMaterials.remove(type);
                else
                    selectedMaterials.add(type);

                reloadMenuAsync();
            });
            slot++;
        }
    }

    private boolean canProceed(){
        return selectedMaterials.size() > 0;
    }
}
