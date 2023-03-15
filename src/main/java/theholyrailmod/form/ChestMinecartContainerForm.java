package theholyrailmod.form;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.presets.containerComponent.mob.MobContainerFormSwitcher;

import theholyrailmod.container.ChestMinecartContainer;
import theholyrailmod.theholyrail.ChestMinecartMob;

public class ChestMinecartContainerForm extends MobContainerFormSwitcher<ChestMinecartContainer> {
    public ChestMinecartInventoryForm itemForm;
    public ChestMinecartMob mob;

    public ChestMinecartContainerForm(Client client, ChestMinecartContainer container) {
        super(client, container);
        container.containerForm = this;
        mob = container.minecartMob;

        GameMessage storageTitle = new LocalMessage("ui", "chestminecartstorage");

        this.itemForm = this.addComponent(
                new ChestMinecartInventoryForm(client, container, storageTitle.translate(),
                        e -> {
                            container.isModifyingStorage = false;
                        }) {
                    @Override
                    public ChestMinecartMob getMob() {
                        return ChestMinecartContainerForm.this.container.minecartMob;
                    }
                });

        this.makeCurrent(this.itemForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return true;
    }
}
