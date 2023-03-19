package theholyrailmod.theholyrail;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.function.Predicate;

import necesse.engine.GameTileRange;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketObjectEntity;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.entity.objectEntity.interfaces.OEInventory;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.InventoryRange;
import necesse.inventory.item.Item;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import necesse.level.maps.multiTile.MultiTile;

public class StationTrackObjectEntity extends ObjectEntity {
    private final long TRANSFER_ITEM_COOLDOWN = 50L;

    private static float nearbyInventoryRange = 2.5f;
    private LinkedHashSet<Inventory> nearbyInventories = new LinkedHashSet<>();
    private boolean isTransferringItems;
    private long lastItemTransferTime = -1L;
    public final GameTileRange range;
    public boolean isPowered;

    //// Configure Form variables
    private long MAX_STATION_WAIT_TIME;
    private boolean waitSeconds;
    private boolean waitEmpty;
    private boolean waitFull;
    private boolean roleManual;
    private boolean roleLoad;
    private boolean roleUnload;
    //// -------------------------

    public StationTrackObjectEntity(Level level, int x, int y) {
        super(level, "stationtrack", x, y);
        this.waitSeconds = this.shouldSave = this.roleManual = true;
        this.isTransferringItems = this.waitEmpty = this.waitFull = this.isPowered = this.roleLoad = this.roleUnload = false;

        this.MAX_STATION_WAIT_TIME = 5200L;

        GameObject gObj = this.getLevel().getObject(this.getTileX(), this.getTileY());
        MultiTile mt = gObj.getMultiTile(this.getLevel(), this.getTileX(), this.getTileY());
        Rectangle rect = mt.getTileRectangle(0, 0);
        this.range = new GameTileRange(nearbyInventoryRange, rect);

        for (InventoryRange inventoryRange : this.findNearbyInventories(
                level, (int) this.x, (int) this.y, this.range, OEInventory::canUseForNearbyCrafting)) {
            this.nearbyInventories.add(inventoryRange.inventory);
        }
    }

    public void openContainer(int CONTAINER, ServerClient client) {
        ContainerRegistry.openAndSendContainer(client,
                PacketOpenContainer.ObjectEntity(CONTAINER, this));
    }

    public long getMaxStationWaitTime() {
        return this.MAX_STATION_WAIT_TIME;
    }

    public void setMaxStationWaitTime(long time) {
        this.MAX_STATION_WAIT_TIME = time;
    }

    public boolean getWaitSeconds() {
        return this.waitSeconds;
    }

    public void setWaitSeconds(boolean wait) {
        this.waitSeconds = wait;
    }

    public boolean getWaitEmpty() {
        return this.waitEmpty;
    }

    public void setWaitEmpty(boolean wait) {
        this.waitEmpty = wait;
    }

    public boolean getWaitFull() {
        return this.waitFull;
    }

    public void setWaitFull(boolean wait) {
        this.waitFull = wait;
    }

    public boolean getRoleManual() {
        return this.roleManual;
    }

    public void setRoleManual(boolean role) {
        this.roleManual = role;
    }

    public boolean getRoleLoad() {
        return this.roleLoad;
    }

    public void setRoleLoad(boolean role) {
        this.roleLoad = role;
    }

    public boolean getRoleUnload() {
        return this.roleUnload;
    }

    public void setRoleUnload(boolean role) {
        this.roleUnload = role;
    }

    public boolean getIsTransferringItems() {
        return this.isTransferringItems;
    }

    public void setIsTransferringItems(boolean transferring) {
        this.isTransferringItems = transferring;
    }

    public void sendUpdatePacket() {
        if (this.getLevel().isClientLevel()) {
            this.getLevel().getClient().network.sendPacket(new PacketObjectEntity(this));
        }
    }

    public LinkedHashSet<Inventory> getNearbyInventories() {
        return this.nearbyInventories;
    }

    public ArrayList<InventoryRange> findNearbyInventories(Level level, int centerTileX, int centerTileY,
            GameTileRange range, Predicate<OEInventory> filter) {
        if (level == null) {
            return new ArrayList<>();
        } else {
            ArrayList<InventoryRange> targets = new ArrayList<>();

            for (Point tile : range.getValidTiles(centerTileX, centerTileY)) {
                ObjectEntity ent = level.entityManager.getObjectEntity(tile.x, tile.y);
                if (ent instanceof OEInventory && (filter == null || filter.test((OEInventory) ent))) {
                    Inventory inventory = ((OEInventory) ent).getInventory();
                    if (!targets.stream().anyMatch(i -> i.inventory == inventory)) {
                        targets.add(new InventoryRange(inventory));
                    }
                }
            }

            return targets;
        }
    }

    public void transferItemsToMinecart(ChestMinecartMob mob) {
        if (!this.getIsTransferringItems()) {
            this.setIsTransferringItems(true);

            if (this.lastItemTransferTime == -1L) {
                this.lastItemTransferTime = this.getWorldEntity().getTime();
            }

            GameRandom random = new GameRandom(GameRandom.globalRandom.nextInt());

            ArrayList<Inventory> invArray = new ArrayList<>(this.getNearbyInventories());
            Inventory randomInv = invArray.get(random.getIntBetween(0, invArray.size() - 1));
            int invSize = randomInv.getSize();

            for (int i = 0; i < invSize; ++i) {
                if (!randomInv.isSlotClear(i)) {
                    boolean hasItem = false;
                    InventoryItem fromItem = randomInv.getItem(i);
                    for (int j = 0; j < mob.getInventory().getSize(); ++j) {
                        if (!mob.getInventory().isSlotClear(j)) {
                            if (mob.getInventory().getItem(j).getItemDisplayName() == randomInv.getItem(i)
                                    .getItemDisplayName()
                                    && mob.getInventory().canAddItem(getLevel(), null, fromItem, j, j,
                                            "chestminecartcheckstack") > 0) {
                                hasItem = true;
                                InventoryItem toItem = mob.getInventory().getItem(j);

                                fromItem.setAmount(fromItem.getAmount() - 1);
                                toItem.setAmount(toItem.getAmount() + 1);

                                randomInv.setItem(i, fromItem);
                                mob.getInventory().setItem(j, toItem);

                                randomInv.markDirty(i);
                                mob.getInventory().markDirty(j);
                                break;
                            }
                        }
                    }

                    if (!hasItem) {
                        for (int j = 0; j < mob.getInventory().getSize(); ++j) {
                            if (mob.getInventory().isSlotClear(j)) {
                                InventoryItem toItem = new InventoryItem(fromItem.item);
                                fromItem.setAmount(fromItem.getAmount() - 1);
                                toItem.setAmount(1);

                                randomInv.setItem(i, fromItem);
                                mob.getInventory().setItem(j, toItem);

                                randomInv.markDirty(i);
                                mob.getInventory().markDirty(j);
                                break;
                            }
                        }
                    }

                    break;
                }
            }
        } else {
            if (this.lastItemTransferTime != -1L
                    && this.getWorldEntity().getTime() - this.lastItemTransferTime > TRANSFER_ITEM_COOLDOWN) {
                this.lastItemTransferTime = -1L;
                mob.getAndSetFilledInventorySlots();
                this.setIsTransferringItems(false);
            }
        }
    }

    public void transferItemsFromMinecart(ChestMinecartMob mob) {
        if (!this.getIsTransferringItems()) {
            this.setIsTransferringItems(true);

            if (this.lastItemTransferTime == -1L) {
                this.lastItemTransferTime = this.getWorldEntity().getTime();
            }

            InventoryItem item = null;
            int mobItemInSlot = -1;

            for (int i = 0; i < mob.getInventory().getSize(); ++i) {
                if (!mob.getInventory().isSlotClear(i)) {
                    item = mob.getInventory().getItem(i);
                    mobItemInSlot = i;
                    break;
                }
            }

            if (item != null) {
                for (Inventory inv : this.getNearbyInventories()) {
                    boolean addedItem = false;

                    if (inv.canAddItem(this.getLevel(), null, item, 0, inv.getSize(), "unloadchestminecart") > 0) {
                        boolean hasItem = false;
                        int itemInSlot = -1;
                        for (int i = 0; i < inv.getSize(); ++i) {
                            if (!inv.isSlotClear(i)
                                    && inv.getItem(i).getItemDisplayName() == item.getItemDisplayName()) {
                                hasItem = true;
                                itemInSlot = i;
                                break;
                            }
                        }

                        if (hasItem) {
                            InventoryItem toItem = inv.getItem(itemInSlot);

                            toItem.setAmount(toItem.getAmount() + 1);
                            item.setAmount(item.getAmount() - 1);

                            inv.setItem(itemInSlot, toItem);
                            mob.getInventory().setItem(mobItemInSlot, item);

                            inv.markDirty(itemInSlot);
                            mob.getInventory().markDirty(mobItemInSlot);
                            addedItem = true;

                        } else {
                            for (int i = 0; i < inv.getSize(); ++i) {
                                if (inv.isSlotClear(i)) {
                                    InventoryItem toItem = new InventoryItem(item.item);

                                    item.setAmount(item.getAmount() - 1);
                                    toItem.setAmount(1);

                                    inv.setItem(i, toItem);
                                    mob.getInventory().setItem(mobItemInSlot, item);

                                    inv.markDirty(itemInSlot);
                                    mob.getInventory().markDirty(mobItemInSlot);
                                    addedItem = true;

                                    break;
                                }
                            }
                        }
                    }

                    if (addedItem) {
                        break;
                    }
                }
            }
        } else {
            if (this.lastItemTransferTime != -1L
                    && this.getWorldEntity().getTime() - this.lastItemTransferTime > TRANSFER_ITEM_COOLDOWN) {
                this.lastItemTransferTime = -1L;
                mob.getAndSetFilledInventorySlots();
                this.setIsTransferringItems(false);
            }
        }
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addLong("station_wait_time", this.getMaxStationWaitTime());
        save.addBoolean("wait_seconds", this.waitSeconds);
        save.addBoolean("wait_empty", this.waitEmpty);
        save.addBoolean("wait_full", this.waitFull);
        save.addBoolean("role_manual", this.roleManual);
        save.addBoolean("role_load", this.roleLoad);
        save.addBoolean("role_unload", this.roleUnload);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.setMaxStationWaitTime(save.getLong("station_wait_time", 5200L));
        this.setWaitSeconds(save.getBoolean("wait_seconds", true));
        this.setWaitEmpty(save.getBoolean("wait_empty", false));
        this.setWaitFull(save.getBoolean("wait_full", false));
        this.setRoleManual(save.getBoolean("role_manual", false));
        this.setRoleLoad(save.getBoolean("role_load", false));
        this.setRoleUnload(save.getBoolean("role_unload", false));
    }

    @Override
    public void setupContentPacket(PacketWriter writer) {
        super.setupContentPacket(writer);
        writer.putNextLong(this.getMaxStationWaitTime());
        writer.putNextBoolean(this.getWaitSeconds());
        writer.putNextBoolean(this.getWaitEmpty());
        writer.putNextBoolean(this.getWaitFull());
        writer.putNextBoolean(this.getRoleManual());
        writer.putNextBoolean(this.getRoleLoad());
        writer.putNextBoolean(this.getRoleUnload());
    }

    @Override
    public void applyContentPacket(PacketReader reader) {
        super.applyContentPacket(reader);
        this.setMaxStationWaitTime(reader.getNextLong());
        this.setWaitSeconds(reader.getNextBoolean());
        this.setWaitEmpty(reader.getNextBoolean());
        this.setWaitFull(reader.getNextBoolean());
        this.setRoleManual(reader.getNextBoolean());
        this.setRoleLoad(reader.getNextBoolean());
        this.setRoleUnload(reader.getNextBoolean());
    }
}
