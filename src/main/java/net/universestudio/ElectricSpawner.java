package net.universestudio;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.api.researches.Research;
import io.github.thebusybiscuit.slimefun5.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun5.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun5.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class ElectricSpawner extends SimpleSlimefunItem<BlockTicker> implements EnergyNetComponent {

    private final int energyConsumption;
    private final int capacity;

    private final EntityType entity;

    public ElectricSpawner(ItemGroup category, SlimefunItemStack itemStack, ItemStack[] recipe, EntityType type, Research research, int energyConsumption, int capacity) {
        super(category, itemStack, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);

        this.energyConsumption = energyConsumption;
        this.capacity = capacity;
        this.entity = type;

        addItemHandler(onBlockPlace());
        new BlockMenuPreset(getId(), this.getItemName()) {
            @Override
            public void init() {
                for (int i = 0; i < 9; i++) {
                    if (i != 4) {
                        addItem(i, CustomItemStack.create(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "), (p, slot, item, action) -> false);
                    }
                }
                this.optOutOfHeaderItem();
            }

            @Override
            public void newInstance(@NonNull BlockMenu menu, @NonNull Block b) {
                if (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "enabled") == null || BlockStorage.getLocationInfo(b.getLocation(), "enabled").equals("false")) {
                    menu.replaceExistingItem(4, CustomItemStack.create(Material.GUNPOWDER, "&7Enabled: &4\u2718", "", "&e> Click to enable this Machine"));
                    menu.addMenuClickHandler(4, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "enabled", "true");
                        newInstance(menu, b);
                        return false;
                    });
                }
                else {
                    menu.replaceExistingItem(4, CustomItemStack.create(Material.REDSTONE, "&7Enabled: &2\u2714", "", "&e> Click to disable this Machine"));
                    menu.addMenuClickHandler(4, (_, _, _, _) -> {
                        BlockStorage.addBlockInfo(b, "enabled", "false");
                        newInstance(menu, b);
                        return false;
                    });
                }
            }

            @Override
            public boolean canOpen(@NonNull Block b, @NonNull Player p) {
                return BlockStorage.getLocationInfo(b.getLocation(), "owner").equals(p.getUniqueId().toString()) || p.hasPermission("slimefun.cargo.bypass");
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }
        };
        research.addItems(this);
    }

    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(@NonNull BlockPlaceEvent e) {
                Block b = e.getBlock();
                Player p = e.getPlayer();
                BlockStorage.addBlockInfo(b, "enabled", "false");
                BlockStorage.addBlockInfo(b, "owner", p.getUniqueId().toString());
            }
        };
    }

    public int getEnergyConsumption() {
        return this.energyConsumption;
    }

    protected void tick(Block block) {
        Location location = block.getLocation();

        // Players can turn on or off their electric spawner
        if (!"true".equals(BlockStorage.getLocationInfo(location, "enabled"))) return;

        String raw = BlockStorage.getLocationInfo(location, "cooldown");
        int cooldown = raw == null ? 0 : Integer.parseInt(raw);
        if (cooldown > 0) {
            BlockStorage.addBlockInfo(location, "cooldown", String.valueOf(cooldown - 1), false);
            return;
        }

        if (getCharge(location) < getEnergyConsumption()) return;

        // Check spawned entities around the spawner (similar to the vanilla behavior)
        int entityCap = ElectricSpawnersV2.getMaxEntityCap();
        if (entityCap > 0) {
            int count = 0;
            for (Entity n : block.getWorld().getNearbyEntities(location, 4.0, 4.0, 4.0)) {
                if (n.getType().equals(this.entity)) {
                    count++;

                    if (count >= entityCap) {
                        return;
                    }
                }
            }
        }

        // Spawn entity
        removeCharge(location, getEnergyConsumption());
        block.getWorld().spawnEntity(new Location(block.getWorld(), block.getX() + 0.5D, block.getY() + 1.5D, block.getZ() + 0.5D), this.entity);

        // Reset spawn interval
        BlockStorage.addBlockInfo(block, "cooldown", String.valueOf(ElectricSpawnersV2.getSpawnInterval() * 2 - 1), false);
    }

    @Override
    public @NonNull BlockTicker getItemHandler() {
        return new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, Config data) {
                ElectricSpawner.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }

        };
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public @NonNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }
}
