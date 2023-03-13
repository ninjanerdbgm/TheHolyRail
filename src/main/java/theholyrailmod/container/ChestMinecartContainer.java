package theholyrailmod.container;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
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
    public final EmptyCustomAction buttonStoreStackAll;
    public final EmptyCustomAction buttonTakeTakeAll;
    public final EmptyCustomAction buttonSortInventory;
    public boolean isModifyingStorage;
    public final BooleanCustomAction setIsModifyingStorage;
    public ChestMinecartContainerForm containerForm;

    public ChestMinecartContainer(final NetworkClient client, int uniqueSeed, final ChestMinecartMob mob,
            Packet content) {
        super(client, uniqueSeed, mob);
        this.minecartMob = mob;

        this.buttonStoreStackAll = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServerClient()) {
                    ChestMinecartContainer.this.storeStackAll();
                }
            }
        });

        this.buttonTakeTakeAll = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServerClient()) {
                    ChestMinecartContainer.this.takeTakeAll();
                }
            }
        });

        this.buttonSortInventory = this.registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                if (client.isServerClient()) {
                    ChestMinecartContainer.this.sortInventory();
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
    }

    public static Packet getChestMinecartContent(ChestMinecartMob chestMinecart, Server server, ServerClient client) {
        Packet packet = new Packet();
        PacketWriter writer = new PacketWriter(packet);
        writer.putNextContentPacket(client.playerMob.getInv().getTempInventoryPacket(1));

        return packet;
    }

    private void storeStackAll() {
        System.out.println("storeStackAll clicked");
    }

    private void takeTakeAll() {
        System.out.println("takeTakeAll clicked");
    }

    private void sortInventory() {
        System.out.println("sortInventory clicked");
    }

    public static void registerChestMinecartContainer() {
        registryId = ContainerRegistry.registerMobContainer(
                (client, uniqueSeed, mob, content) -> new ChestMinecartContainerForm(client,
                        new ChestMinecartContainer(client.getClient(), uniqueSeed, (ChestMinecartMob) mob, content)),
                (client, uniqueSeed, mob, content, serverObject) -> new ChestMinecartContainer(client, uniqueSeed,
                        (ChestMinecartMob) mob, content));
    }
}
