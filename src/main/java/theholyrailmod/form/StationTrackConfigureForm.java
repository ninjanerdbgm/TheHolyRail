package theholyrailmod.form;

import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormContentToggleButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.forms.presets.containerComponent.ContainerFormList;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.inventory.container.Container;
import theholyrailmod.container.StationTrackContainer;
import theholyrailmod.theholyrail.ChestMinecartMob;
import theholyrailmod.theholyrail.StationTrackObject;

public abstract class StationTrackConfigureForm extends ContainerFormList<Container> {
    private Form stationTrackForm = this.addComponent(new Form("stationtrackconfigure", 420, 200));
    public FormLocalTextButton acceptButton;
    public FormContentToggleButton buttonStoreAll;
    public FormContentToggleButton buttonTakeAll;
    public FormContentToggleButton buttonNone;

    public FormTextInput waitTimeInput;

    public FormLocalLabel waitTimeLabel;
    public FormLocalLabel waitTimeErrorLabel;
    public FormLocalLabel autoChestLabel;
    public FormLocalLabel autoChestLabel_StoreAll;
    public FormLocalLabel autoChestLabel_TakeAll;
    public FormLocalLabel autoChestLabel_None;

    public StationTrackConfigureForm(Client client, Container container, String header,
            FormEventListener<FormInputEvent<FormButton>> backButtonPressed) {
        super(client, container);
        FormFlow itemFlow = new FormFlow(5);
        header = GameUtils.maxString(header, new FontOptions(20), this.stationTrackForm.getWidth() - 10 - 32);
        this.stationTrackForm.addComponent(itemFlow.next(new FormLabel(header, new FontOptions(20), -1, 8, 30), 5));

        StationTrackContainer cont = (StationTrackContainer) container;

        this.waitTimeLabel = this.stationTrackForm.addComponent(
                new FormLocalLabel("ui", "stationtrackstationwaittime", new FontOptions(16),
                        10, 15, itemFlow.next(20),
                        200));
        itemFlow.next(5);

        this.waitTimeErrorLabel = this.stationTrackForm.addComponent(
                new FormLocalLabel("", "", new FontOptions(16),
                        10, 11, itemFlow.next(20),
                        200));
        this.waitTimeErrorLabel.setColor(java.awt.Color.RED);
        itemFlow.next(5);

        this.waitTimeInput = stationTrackForm.addComponent(new FormTextInput(
                10 + this.waitTimeLabel.getBoundingBox().width + 5, 10, FormInputSize.SIZE_16, 20, 20));
        this.waitTimeInput.placeHolder = new LocalMessage("ui", "stationtrackwaittip");
        this.waitTimeInput.setText(String.valueOf(cont.stationTrackObject.MAX_STATION_WAIT_TIME));
        this.waitTimeInput.onChange(e -> {
            try {
                long newStationWaitTime = Long.parseLong(this.waitTimeInput.getText(), 10);
                if (newStationWaitTime < 1000L) {
                    this.waitTimeErrorLabel.setText(new LocalMessage("ui", "stationwaiterror_tooshort"));
                } else if (newStationWaitTime > 120000L) {
                    this.waitTimeErrorLabel.setText(new LocalMessage("ui", "stationwaiterror_toolong"));
                } else {
                    this.waitTimeErrorLabel.setText("");
                    cont.stationTrackObject.MAX_STATION_WAIT_TIME = newStationWaitTime;
                }
            } catch (NumberFormatException n) {
                this.waitTimeErrorLabel.setText(new LocalMessage("ui", "stationwaiterror_invalid"));
            }
        });

    }

    public abstract StationTrackObject getStationTrack();

    @Override
    public void onWindowResized() {
        super.onWindowResized();
        ContainerComponent.setPosFocus(this.stationTrackForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return false;
    }
}
