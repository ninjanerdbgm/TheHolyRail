package theholyrailmod.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.InventoryRange;
import necesse.inventory.container.SlotIndexRange;
import necesse.inventory.container.customAction.BooleanCustomAction;
import necesse.inventory.container.customAction.EmptyCustomAction;
import necesse.inventory.container.mob.MobContainer;
import necesse.inventory.container.slots.ContainerSlot;

import theholyrailmod.form.ChestMinecartContainerForm;
import theholyrailmod.theholyrail.ChestMinecartMob;

public class ChestMinecartContainer extends MobContainer {
    public int INVENTORY_START = -1;
    public int INVENTORY_END = -1;
    public static int registryId;
    public ChestMinecartMob minecartMob;
    public NetworkClient client;
    public final EmptyCustomAction buttonStoreStackAll;
    public final EmptyCustomAction buttonTakeTakeAll;
    public final EmptyCustomAction buttonSortInventory;
    public final EmptyCustomAction buttonTakeAll;
    public boolean isModifyingStorage;
    public final BooleanCustomAction setIsModifyingStorage;
    public ChestMinecartContainerForm containerForm;

    public ChestMinecartContainer(final NetworkClient client, int uniqueSeed, final ChestMinecartMob mob,
            Packet content) {
        super(client, uniqueSeed, mob);
        this.minecartMob = mob;
        this.client = client;

        this.buttonStoreStackAll = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServer()) {
                    ChestMinecartContainer.this.storeStackAll();
                }
            }
        });

        this.buttonTakeTakeAll = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServer()) {
                    ChestMinecartContainer.this.takeTakeAll();
                }
            }
        });

        this.buttonSortInventory = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServer()) {
                    ChestMinecartContainer.this.sortInventory();
                }
            }
        });

        this.buttonTakeAll = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServer()) {
                    ChestMinecartContainer.this.takeAllItems();
                }
            }
        });

        for (int i = 0; i < mob.getInventory().getSize(); ++i) {
            int index = this.addSlot(new ContainerSlot(mob.getInventory(), i));
            if (this.INVENTORY_START == -1) {
                this.INVENTORY_START = index;
            }

            if (this.INVENTORY_END == -1) {
                this.INVENTORY_END = index;
            }

            this.INVENTORY_START = Math.min(this.INVENTORY_START, index);
            this.INVENTORY_END = Math.max(this.INVENTORY_END, index);
        }

        this.setIsModifyingStorage = this.registerAction(new BooleanCustomAction() {
            @Override
            protected void run(boolean value) {
                ChestMinecartContainer.this.isModifyingStorage = value;
            }
        });

        this.addInventoryQuickTransfer(this.INVENTORY_START, this.INVENTORY_END);

        mob.setIsOpened(true);
    }

    @Override
    public void onClose() {
        minecartMob.setIsOpened(false);
        minecartMob.getAndSetFilledInventorySlots();
        super.onClose();
    }

    public static Packet getChestMinecartContent(ChestMinecartMob chestMinecart, Server server, ServerClient client) {
        Packet packet = new Packet();
        PacketWriter writer = new PacketWriter(packet);
        writer.putNextContentPacket(client.playerMob.getInv().getTempInventoryPacket(1));

        return packet;
    }

    public static Packet getChestMinecartContent(ChestMinecartMob chestMinecart, Server server, PlayerMob player) {
        Packet packet = new Packet();
        PacketWriter writer = new PacketWriter(packet);
        writer.putNextContentPacket(player.getInv().getTempInventoryPacket(1));

        return packet;
    }

    private void storeStackAll() {
        InventoryRange inv = new InventoryRange(this.minecartMob.getInventory());

        ArrayList<InventoryRange> targets = new ArrayList<>(
                Collections.singleton(
                        new InventoryRange(this.client.playerMob.getInv().main, 0,
                                this.client.playerMob.getInv().main.getSize() - 1)));

        for (InventoryRange target : targets) {
            for (int i = target.startSlot; i <= target.endSlot; ++i) {
                if (!target.inventory.isSlotClear(i) && !target.inventory.isItemLocked(i)) {
                    InventoryItem item = target.inventory.getItem(i);
                    if (inv.inventory
                            .restockFrom(
                                    this.client.playerMob.getLevel(), this.client.playerMob, item, inv.startSlot,
                                    inv.endSlot, "restockToChestMinecart", false, null)) {
                        if (item.getAmount() <= 0) {
                            target.inventory.setItem(i, null);
                        }

                        target.inventory.updateSlot(i);
                    }
                }
            }
        }
    }

    private void takeTakeAll() {
        InventoryRange inv = new InventoryRange(this.minecartMob.getInventory());

        ArrayList<InventoryRange> targets = new ArrayList<>(
                Collections.singleton(
                        new InventoryRange(this.client.playerMob.getInv().main, 0,
                                this.client.playerMob.getInv().main.getSize() - 1)));

        for (InventoryRange target : targets) {
            for (int i = inv.startSlot; i <= inv.endSlot; ++i) {
                if (!inv.inventory.isSlotClear(i) && !inv.inventory.isItemLocked(i)) {
                    if (target.inventory
                            .getAmount(
                                    this.client.playerMob.getLevel(),
                                    this.client.playerMob,
                                    inv.inventory.getItemSlot(i),
                                    target.startSlot,
                                    target.endSlot,
                                    "takeFromChestMinecart") > 0) {
                        int lastAmount = target.inventory.getAmount(i);
                        target.inventory.addItem(this.client.playerMob.getLevel(), this.client.playerMob,
                                inv.inventory.getItem(i), inv.startSlot, inv.endSlot, "takeFromChestMinecart",
                                null);
                        if (lastAmount != inv.inventory.getAmount(i)) {
                            inv.inventory.markDirty(i);
                        }

                        if (inv.inventory.getAmount(i) <= 0) {
                            inv.inventory.clearSlot(i);
                        }
                    }
                }
            }
        }
    }

    private void sortInventory() {
        this.minecartMob.sortItems();

        for (int i = 0; i < this.minecartMob.getInventory().getSize(); ++i) {
            int index = this.addSlot(new ContainerSlot(this.minecartMob.getInventory(), i));
            if (this.INVENTORY_START == -1) {
                this.INVENTORY_START = index;
            }

            if (this.INVENTORY_END == -1) {
                this.INVENTORY_END = index;
            }

            this.INVENTORY_START = Math.min(this.INVENTORY_START, index);
            this.INVENTORY_END = Math.max(this.INVENTORY_END, index);
        }

        synchronized (this.minecartMob.getLevel().entityManager.lock) {
            this.minecartMob.serverTick();
        }
    }

    private void takeAllItems() {
        Inventory inv = this.minecartMob.getInventory();

        for (int i = this.INVENTORY_START; i <= this.INVENTORY_END; ++i) {
            if (!inv.isItemLocked(i)) {
                ChestMinecartContainer.this.transferToSlots(
                        ChestMinecartContainer.this.getSlot(i),
                        Arrays.asList(
                                // Slot 0 in the player's inventory is tied to the mouse cursor.
                                new SlotIndexRange(1, this.client.playerMob.getInv().main.getSize() - 1)),
                        "takeAllFromChestMinecart");
            }
        }
    }

    public static void registerChestMinecartContainer() {
        registryId = ContainerRegistry.registerMobContainer(
                (client, uniqueSeed, mob, content) -> new ChestMinecartContainerForm(client,
                        new ChestMinecartContainer(client.getClient(), uniqueSeed, (ChestMinecartMob) mob, content)),
                (client, uniqueSeed, mob, content, serverObject) -> new ChestMinecartContainer(client, uniqueSeed,
                        (ChestMinecartMob) mob, content));
    }
}
