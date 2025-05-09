package theholyrailmod.form;

import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.engine.window.GameWindow;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.forms.presets.containerComponent.ContainerFormList;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.inventory.container.Container;
import theholyrailmod.container.ChestMinecartContainer;
import theholyrailmod.theholyrail.ChestMinecartMob;

public abstract class ChestMinecartInventoryForm extends ContainerFormList<Container> {
    private Form itemForm = this.addComponent(new Form("chestminecartinventory", 420, 165));
    public FormLabel storageLabel;
    public FormContainerSlot[] slots;
    public FormLocalTextButton buttonStoreStackAll;
    public FormLocalTextButton buttonTakeTakeAll;
    public FormLocalTextButton buttonSortInventory;
    public Runnable buttonStoreStackAllClicked;
    public Runnable buttonTakeTakeAllClicked;
    public Runnable buttonSortInventoryClicked;

    public ChestMinecartInventoryForm(Client client, Container container, String header,
            FormEventListener<FormInputEvent<FormButton>> backButtonPressed) {
        super(client, container);
        FormFlow itemFlow = new FormFlow(10);
        header = GameUtils.maxString(header, new FontOptions(20), this.itemForm.getWidth() - 10 - 32);
        this.itemForm.addComponent(itemFlow.nextY(new FormLabel(header, new FontOptions(20), -1, 8, 30), 5));

        ChestMinecartContainer cont = (ChestMinecartContainer) container;
        this.slots = new FormContainerSlot[cont.INVENTORY_END - cont.INVENTORY_START + 1];

        for (int i = 0; i < slots.length; ++i) {
            int slotIndex = i + cont.INVENTORY_START;
            int x = i % 10;
            int y = i / 10;
            this.slots[i] = this.itemForm
                    .addComponent(new FormContainerSlot(client, container, slotIndex, 10 + x * 40,
                            25 + y * 40 + 30));
        }

        this.storageLabel = this.itemForm.addComponent(
                new FormLabel("", new FontOptions(20), -1, 5, itemFlow.next(20), this.itemForm.getWidth() - 10));
        itemFlow.next(5);

        FormContentIconButton sortButton = this.itemForm
                .addComponent(
                        new FormContentIconButton(
                                this.itemForm.getWidth() - 107, 8, FormInputSize.SIZE_24, ButtonColor.BASE,
                                Settings.UI.inventory_sort, new LocalMessage("ui", "chestminecartsort")));
        sortButton.onClicked(e -> {
            cont.buttonSortInventory.runAndSend();
        });
        sortButton.setCooldown(350);
        itemFlow.next(5);

        FormContentIconButton storeStackAll = this.itemForm
                .addComponent(
                        new FormContentIconButton(
                                this.itemForm.getWidth() - 84, 8, FormInputSize.SIZE_24, ButtonColor.BASE,
                                Settings.UI.inventory_quickstack_out,
                                new LocalMessage("ui", "chestminecartstorestackall")));
        storeStackAll.onClicked(e -> {
            cont.buttonStoreStackAll.runAndSend();
        });
        storeStackAll.setCooldown(350);
        itemFlow.next(5);

        FormContentIconButton takeTakeAll = this.itemForm
                .addComponent(
                        new FormContentIconButton(
                                this.itemForm.getWidth() - 60, 8, FormInputSize.SIZE_24, ButtonColor.BASE,
                                Settings.UI.inventory_quickstack_in,
                                new LocalMessage("ui", "chestminecarttaketakeall")));
        takeTakeAll.onClicked(e -> {
            cont.buttonTakeTakeAll.runAndSend();
        });
        takeTakeAll.setCooldown(350);
        itemFlow.next(5);

        FormContentIconButton takeAllItems = this.itemForm
                .addComponent(
                        new FormContentIconButton(
                                this.itemForm.getWidth() - 36, 8, FormInputSize.SIZE_24, ButtonColor.BASE,
                                Settings.UI.container_loot_all,
                                new LocalMessage("ui", "chestminecarttakeallitems")));
        takeAllItems.onClicked(e -> {
            cont.buttonTakeAll.runAndSend();
        });
        takeAllItems.setCooldown(350);
    }

    public abstract ChestMinecartMob getMob();

    @Override
    public void onWindowResized(GameWindow window) {
        super.onWindowResized(window);
        ContainerComponent.setPosFocus(this.itemForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return true;
    }
}
