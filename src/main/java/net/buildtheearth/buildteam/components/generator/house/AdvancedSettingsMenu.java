package net.buildtheearth.buildteam.components.generator.house;

import net.buildtheearth.utils.AbstractMenu;
import net.buildtheearth.utils.Item;
import net.buildtheearth.utils.Liste;
import net.buildtheearth.utils.MenuItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.UUID;

public class AdvancedSettingsMenu extends AbstractMenu {

    public static String ADVANCED_SETTINGS_INV_NAME = "Adjust some Advanced Settings";

    public static int FLOOR_COUNT_SLOT = 11;
    public static int FLOOR_HEIGHT_SLOT = 20;
    public static int BASE_HEIGHT_SLOT = 29;
    public static int WINDOW_WIDTH_SLOT = 15;
    public static int WINDOW_HEIGHT_SLOT = 24;
    public static int WINDOW_DISTANCE_SLOT = 33;

    public static int NEXT_ITEM_SLOT = 44;

    public int floorCount, floorHeight, baseHeight, windowWidth, windowHeight, windowDistance;

    public AdvancedSettingsMenu(Player player) {
        super(5, ADVANCED_SETTINGS_INV_NAME, player);
    }

    @Override
    protected void setPreviewItems() {
        UUID uuid = getMenuPlayer().getUniqueId();

        this.floorCount = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.FLOOR_COUNT));
        this.floorHeight = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.FLOOR_HEIGHT));
        this.baseHeight = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.BASE_HEIGHT));
        this.windowWidth = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.WINDOW_WIDTH));
        this.windowHeight = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.WINDOW_HEIGHT));
        this.windowDistance = Integer.parseInt(House.playerHouseSettings.get(uuid).getValues().get(HouseFlag.WINDOW_DISTANCE));

        setSliderItems(MenuItems.SliderColor.WHITE, FLOOR_COUNT_SLOT, "Number of Floors", floorCount, 1, 10, "Floors");
        setSliderItems(MenuItems.SliderColor.LIGHT_GRAY, FLOOR_HEIGHT_SLOT, "Floors Height", floorHeight, 1, 10, "Blocks");
        setSliderItems(MenuItems.SliderColor.WHITE, BASE_HEIGHT_SLOT, "Basement Height", baseHeight, 0, 10, "Blocks");
        setSliderItems(MenuItems.SliderColor.WHITE, WINDOW_WIDTH_SLOT, "Window Width", windowWidth, 1, 5, "Blocks");
        setSliderItems(MenuItems.SliderColor.LIGHT_GRAY, WINDOW_HEIGHT_SLOT, "Window Height", windowHeight, 1, 5, "Blocks");
        setSliderItems(MenuItems.SliderColor.WHITE, WINDOW_DISTANCE_SLOT, "Window Distance", windowDistance, 1, 6, "Blocks");


        getMenu().getSlot(NEXT_ITEM_SLOT).setItem(MenuItems.getNextItem());

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {}

    @Override
    protected void setItemClickEventsAsync() {
        setSliderClickEvents(HouseFlag.FLOOR_COUNT, FLOOR_COUNT_SLOT,  1, 10);
        setSliderClickEvents(HouseFlag.FLOOR_HEIGHT, FLOOR_HEIGHT_SLOT,  1, 10);
        setSliderClickEvents(HouseFlag.BASE_HEIGHT, BASE_HEIGHT_SLOT,  0, 10);
        setSliderClickEvents(HouseFlag.WINDOW_WIDTH, WINDOW_WIDTH_SLOT,  1, 5);
        setSliderClickEvents(HouseFlag.WINDOW_HEIGHT, WINDOW_HEIGHT_SLOT,  1, 5);
        setSliderClickEvents(HouseFlag.WINDOW_DISTANCE, WINDOW_DISTANCE_SLOT,  1, 6);


        // Set click events items
        getMenu().getSlot(NEXT_ITEM_SLOT).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.playSound(clickPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

            House.generate(clickPlayer);
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(Item.create(Material.STAINED_GLASS_PANE, " ", (short)15, null))
                .pattern("111111111")
                .pattern("100010001")
                .pattern("100010001")
                .pattern("100010001")
                .pattern("111111110")
                .build();
    }

    protected void setSliderClickEvents(HouseFlag houseFlag, int slot, int minValue, int maxValue){
        // Set click event for previous page item
        getMenu().getSlot(slot - 1).setClickHandler((clickPlayer, clickInformation) -> {
            int value = Integer.parseInt(House.playerHouseSettings.get(clickPlayer.getUniqueId()).getValues().get(houseFlag));

            if (value > minValue) {
                House.playerHouseSettings.get(clickPlayer.getUniqueId()).setValue(houseFlag, "" + (value - 1));

                clickPlayer.playSound(clickPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                reloadMenuAsync();
            }else{
                clickPlayer.playSound(clickPlayer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            }
        });

        // Set click event for next page item
        getMenu().getSlot(slot + 1).setClickHandler((clickPlayer, clickInformation) -> {
            int value = Integer.parseInt(House.playerHouseSettings.get(clickPlayer.getUniqueId()).getValues().get(houseFlag));

            if (value < maxValue) {
                House.playerHouseSettings.get(clickPlayer.getUniqueId()).setValue(houseFlag, "" + (value + 1));

                clickPlayer.playSound(clickPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                reloadMenuAsync();
            }else{
                clickPlayer.playSound(clickPlayer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);

            }
        });
    }
}
