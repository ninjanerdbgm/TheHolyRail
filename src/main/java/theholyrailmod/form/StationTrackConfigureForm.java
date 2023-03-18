package theholyrailmod.form;

import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormButtonToggle;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormContentToggleButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.components.localComponents.FormLocalCheckBox;
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
    private Form stationTrackForm = this.addComponent(new Form("stationtrackconfigure", 400, 250));
    public FormLocalTextButton acceptButton;
    public FormLocalTextButton saveButton;
    public FormLocalTextButton cancelButton;
    public FormLocalCheckBox waitCheckbox;
    public FormContentToggleButton buttonStoreAll;
    public FormContentToggleButton buttonTakeAll;
    public FormContentToggleButton buttonNone;

    public FormTextInput waitTimeInput;

    public FormLocalLabel waitTimeErrorLabel;
    public FormLocalLabel autoChestLabel;
    public FormLocalLabel autoChestLabel_StoreAll;
    public FormLocalLabel autoChestLabel_TakeAll;
    public FormLocalLabel autoChestLabel_None;

    public LocalMessage errorText;

    public boolean wait_seconds;
    public boolean wait_empty;
    public boolean wait_full;
    public boolean role_load;
    public boolean role_unload;
    public boolean role_manual;

    private boolean hasError;

    public StationTrackConfigureForm(Client client, Container container, String header,
            FormEventListener<FormInputEvent<FormButton>> backButtonPressed) {
        // Super
        super(client, container);
        // Container
        StationTrackContainer cont = (StationTrackContainer) container;
        // Init
        this.errorText = new LocalMessage("", "");
        this.wait_seconds = this.role_manual = true;
        this.wait_empty = this.wait_full = this.role_load = this.role_unload = this.role_manual = this.hasError = false;
        // Form
        FormFlow itemFlow = new FormFlow(5);
        header = GameUtils.maxString(header, new FontOptions(20), this.stationTrackForm.getWidth() - 10 - 32);
        this.stationTrackForm.addComponent(itemFlow.next(new FormLabel(header, new FontOptions(20), -1, 8, 30), 5));

        this.waitCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackstationwaittime", 10, 40, this.wait_seconds, 300));
        this.waitCheckbox.onClicked(e -> {
            this.wait_seconds = e.from.checked;
            if (this.wait_seconds) {
                this.wait_empty = false;
                this.wait_full = false;
                this.waitTimeInput.changedTyping(true);
            }
        });

        this.waitTimeErrorLabel = this.stationTrackForm.addComponent(
                new FormLocalLabel("", "", new FontOptions(12), -1,
                        35, 35, 400));
        this.waitTimeErrorLabel.setColor(java.awt.Color.RED);
        itemFlow.next(5);

        this.waitTimeInput = stationTrackForm.addComponent(new FormTextInput(
                35 + this.waitCheckbox.getBoundingBox().width + 15, 38, FormInputSize.SIZE_16, 60, 20));
        this.waitTimeInput.setText(String.valueOf((float) cont.stationTrackObject.MAX_STATION_WAIT_TIME / 1000f));
        this.waitTimeInput.onChange(e -> {
            if (this.wait_seconds) {
                try {
                    this.hasError = true;
                    float waitTime = Float.parseFloat(this.waitTimeInput.getText());
                    waitTime *= 1000f;
                    long newStationWaitTime = Math.round(waitTime);

                    if (newStationWaitTime < 1000L) {
                        this.errorText = new LocalMessage("ui", "stationwaiterror_tooshort");
                    } else if (newStationWaitTime > 120000L) {
                        this.errorText = new LocalMessage("ui", "stationwaiterror_toolong");
                    } else {
                        this.errorText = new LocalMessage("", "");
                        this.hasError = false;
                    }
                } catch (Exception ex) {
                    this.errorText = new LocalMessage("ui", "stationwaiterror_invalid");
                } finally {
                    this.waitTimeErrorLabel.setText(this.errorText);
                    this.saveButton.setActive(!this.hasError);
                }
            }
        });

        this.saveButton = this.stationTrackForm.addComponent(new FormLocalTextButton("ui", "stationsave",
                this.stationTrackForm.getWidth() / 4 - 10, this.stationTrackForm.getHeight() - 35,
                this.stationTrackForm.getWidth() / 4 + 10, FormInputSize.SIZE_16, ButtonColor.BASE));
        this.saveButton.setActive(!this.hasError);
        this.saveButton.onClicked(e -> {
            if (this.hasError) {
                return;
            }
            if (this.wait_seconds) {
                float waitTime = Float.parseFloat(this.waitTimeInput.getText());
                waitTime *= 1000f;
                long newStationWaitTime = Math.round(waitTime);

                cont.stationTrackObject.MAX_STATION_WAIT_TIME = newStationWaitTime * 1000L;
            }

            this.setHidden(true);
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
