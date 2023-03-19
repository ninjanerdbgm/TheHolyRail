package theholyrailmod.form;

import java.util.List;

import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.tickManager.TickManager;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.SharedTextureDrawOptions;
import necesse.gfx.drawables.SortedDrawable;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormBreakLine;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentToggleButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.components.localComponents.FormLocalCheckBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.forms.presets.containerComponent.ContainerFormList;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.GameTooltips;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.ui.ButtonColor;
import necesse.inventory.container.Container;
import necesse.level.maps.hudManager.HudDrawElement;
import theholyrailmod.container.StationTrackContainer;

public abstract class StationTrackConfigureForm extends ContainerFormList<Container> {
    private Form stationTrackForm = this.addComponent(new Form("stationtrackconfigure", 400, 250));
    public FormLocalTextButton acceptButton;
    public FormLocalTextButton saveButton;
    public FormLocalTextButton cancelButton;
    public FormLocalCheckBox waitCheckbox;
    public FormLocalCheckBox waitEmptyCheckbox;
    public FormLocalCheckBox waitFullCheckbox;
    public FormLocalCheckBox roleManualCheckbox;
    public FormLocalCheckBox roleLoadCheckbox;
    public FormLocalCheckBox roleUnloadCheckbox;
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
    private java.awt.Color hideErrorColor = new java.awt.Color(0.85f, 0f, 0f, 0f);
    private java.awt.Color showErrorColor = new java.awt.Color(0.85f, 0f, 0f, 1f);

    protected HudDrawElement rangeElement;

    public StationTrackConfigureForm(Client client, Container container, String header,
            FormEventListener<FormInputEvent<FormButton>> backButtonPressed) {
        // Super
        super(client, container);
        // Container
        StationTrackContainer cont = (StationTrackContainer) container;
        // Init
        this.errorText = new LocalMessage("", "");
        this.wait_seconds = cont.stationTrackEntity.getWaitSeconds();
        this.wait_empty = cont.stationTrackEntity.getWaitEmpty();
        this.wait_full = cont.stationTrackEntity.getWaitFull();
        this.role_manual = cont.stationTrackEntity.getRoleManual();
        this.role_load = cont.stationTrackEntity.getRoleLoad();
        this.role_unload = cont.stationTrackEntity.getRoleUnload();
        this.hasError = false;

        if (!this.wait_seconds && !this.wait_empty && !this.wait_full) {
            this.wait_seconds = true;
        }
        if (!this.role_manual && !this.role_load && !this.role_unload) {
            this.role_manual = true;
        }

        // Form
        header = GameUtils.maxString(header, new FontOptions(20), this.stationTrackForm.getWidth() - 10 - 32);
        this.stationTrackForm.addComponent(new FormLabel(header, new FontOptions(20), FormLabel.ALIGN_LEFT,
                15, 10));

        this.waitCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackstationwaittime", 10, 43, this.wait_seconds, 300));
        this.waitCheckbox.onClicked(e -> {
            this.wait_seconds = e.from.checked;
            this.configureFormElements(FromField.WAITSECONDS);
        });
        this.waitCheckbox.checked = this.wait_seconds;

        this.waitTimeErrorLabel = this.stationTrackForm.addComponent(
                new FormLocalLabel("", "", new FontOptions(12), -1,
                        28, 28, 400));
        this.waitTimeErrorLabel.setColor(hideErrorColor);

        this.waitTimeInput = stationTrackForm.addComponent(new FormTextInput(
                35 + this.waitCheckbox.getBoundingBox().width + 15, 41, FormInputSize.SIZE_16, 60, 20));
        this.waitTimeInput.setText(String.valueOf((float) cont.stationTrackEntity.getMaxStationWaitTime() / 1000f));
        this.waitTimeInput.onChange(e -> {
            if (this.wait_seconds) {
                try {
                    this.hasError = true;
                    float waitTime = Float.parseFloat(this.waitTimeInput.getText());
                    waitTime *= 1000f;
                    long newStationWaitTime = Math.round(waitTime);

                    if (newStationWaitTime < 0L) {
                        this.errorText = new LocalMessage("ui", "stationwaiterror_tooshort");
                    } else if (newStationWaitTime > 180000L) {
                        this.errorText = new LocalMessage("ui", "stationwaiterror_toolong");
                    } else {
                        this.errorText = new LocalMessage("", "");
                        this.hasError = false;
                    }
                } catch (Exception ex) {
                    this.errorText = new LocalMessage("ui", "stationwaiterror_invalid");
                } finally {
                    this.waitTimeErrorLabel.setText(this.errorText);
                    this.waitTimeErrorLabel.setColor(this.hasError ? showErrorColor : hideErrorColor);

                    this.saveButton.setActive(!this.hasError);
                }
            }
        });
        this.waitTimeInput.setActive(this.wait_seconds);

        this.waitEmptyCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackstationwaittime_empty", 10, 68, this.wait_empty,
                                360));
        this.waitEmptyCheckbox.onClicked(e -> {
            this.wait_empty = e.from.checked;
            this.configureFormElements(FromField.WAITEMPTY);
        });
        this.waitEmptyCheckbox.checked = this.wait_empty;

        this.waitFullCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackstationwaittime_full", 10, 93, this.wait_full,
                                360));
        this.waitFullCheckbox.onClicked(e -> {
            this.wait_full = e.from.checked;
            this.configureFormElements(FromField.WAITFULL);
        });
        this.waitFullCheckbox.checked = this.wait_full;

        this.stationTrackForm.addComponent(
                new FormBreakLine(FormBreakLine.ALIGN_BEGINNING, 15, 118, this.stationTrackForm.getWidth() - 30, true));

        this.stationTrackForm.addComponent(new FormLocalLabel("ui", "stationtrackautochestlabel", new FontOptions(20),
                FormLocalLabel.ALIGN_LEFT, 15, 130,
                this.stationTrackForm.getWidth() - 8));

        this.roleManualCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackautochestlabel_manual",
                                this.stationTrackForm.getWidth() / 4 - this.stationTrackForm.getWidth() / 6 - 15, 160,
                                this.role_manual,
                                80) {
                            @Override
                            public GameTooltips getTooltip() {
                                return new StringTooltips(Localization.translate("ui", "stationrolemanualtip"), 400);
                            }
                        });
        this.roleManualCheckbox.onClicked(e -> {
            this.role_manual = e.from.checked;
            this.configureFormElements(FromField.ROLEMANUAL);
        });
        this.roleManualCheckbox.checked = this.role_manual;

        this.roleLoadCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackautochestlabel_load",
                                this.stationTrackForm.getWidth() / 2
                                        - this.roleManualCheckbox.getBoundingBox().width / 4 - 15,
                                160,
                                this.role_load,
                                80) {
                            @Override
                            public GameTooltips getTooltip() {
                                return new StringTooltips(Localization.translate("ui", "stationroleloadtip"), 400);
                            }
                        });
        this.roleLoadCheckbox.onClicked(e -> {
            this.role_load = e.from.checked;

            this.configureFormElements(FromField.ROLELOAD);
        });
        this.roleLoadCheckbox.checked = this.role_load;

        this.roleUnloadCheckbox = this.stationTrackForm
                .addComponent(
                        new FormLocalCheckBox("ui", "stationtrackautochestlabel_unload",
                                (3 * this.stationTrackForm.getWidth() / 4)
                                        + this.roleLoadCheckbox.getBoundingBox().width / 4 - 15,
                                160,
                                this.role_unload,
                                80) {
                            @Override
                            public GameTooltips getTooltip() {
                                return new StringTooltips(Localization.translate("ui", "stationroleunloadtip"), 400);
                            }
                        });
        this.roleUnloadCheckbox.onClicked(e -> {
            this.role_unload = e.from.checked;
            this.configureFormElements(FromField.ROLEUNLOAD);
        });
        this.roleUnloadCheckbox.checked = this.role_unload;

        this.saveButton = this.stationTrackForm.addComponent(new FormLocalTextButton("ui", "stationsave",
                this.stationTrackForm.getWidth() / 6 - 15,
                this.stationTrackForm.getHeight() - 50,
                this.stationTrackForm.getWidth() / 4 + 10, FormInputSize.SIZE_20, ButtonColor.BASE));
        this.saveButton.setActive(!this.hasError);
        this.saveButton.onClicked(e -> {
            if (this.hasError) {
                return;
            }

            cont.stationTrackEntity.setWaitSeconds(this.wait_seconds);
            cont.stationTrackEntity.setWaitEmpty(this.wait_empty);
            cont.stationTrackEntity.setWaitFull(this.wait_full);
            cont.stationTrackEntity.setRoleManual(this.role_manual);
            cont.stationTrackEntity.setRoleLoad(this.role_load);
            cont.stationTrackEntity.setRoleUnload(this.role_unload);

            if (this.wait_seconds) {
                float waitTime = Float.parseFloat(this.waitTimeInput.getText());
                waitTime *= 1000f;
                long newStationWaitTime = Math.round(waitTime);

                cont.stationTrackEntity.setMaxStationWaitTime(newStationWaitTime);
            }

            cont.stationTrackEntity.sendUpdatePacket();
            this.setHidden(true);
        });

        this.cancelButton = this.stationTrackForm.addComponent(new FormLocalTextButton("ui", "stationcancel",
                this.stationTrackForm.getWidth() - this.stationTrackForm.getWidth() / 3
                        - this.stationTrackForm.getWidth() / 6 + 30,
                this.stationTrackForm.getHeight() - 50,
                this.stationTrackForm.getWidth() / 4 + 10, FormInputSize.SIZE_20, ButtonColor.BASE));
        this.cancelButton.onClicked(e -> {
            this.setHidden(true);
        });

        this.configureFormElements(FromField.INIT);

        cont.stationTrackEntity.sendUpdatePacket();
    }

    @Override
    protected void init() {
        super.init();
        if (this.rangeElement != null) {
            this.rangeElement.remove();
        }

        this.rangeElement = new HudDrawElement() {
            @Override
            public void addDrawables(List<SortedDrawable> list, GameCamera camera, PlayerMob perspective) {
                if (StationTrackConfigureForm.this.roleLoadCheckbox.isHovering()
                        || StationTrackConfigureForm.this.roleUnloadCheckbox.isHovering()) {
                    StationTrackContainer cont = (StationTrackContainer) StationTrackConfigureForm.this.container;
                    final SharedTextureDrawOptions options = cont.stationTrackEntity.range.getDrawOptions(
                            new java.awt.Color(255, 255, 255, 150),
                            new java.awt.Color(255, 255, 255, 50),
                            (int) cont.stationTrackEntity.x,
                            (int) cont.stationTrackEntity.y,
                            camera);
                    if (options != null) {
                        list.add(new SortedDrawable() {
                            @Override
                            public int getPriority() {
                                return -1000000;
                            }

                            @Override
                            public void draw(TickManager tickManager) {
                                options.draw();
                            }
                        });
                    }
                }
            }
        };
        this.client.getLevel().hudManager.addElement(this.rangeElement);
    }

    @Override
    public void onWindowResized() {
        super.onWindowResized();
        ContainerComponent.setPosFocus(this.stationTrackForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return false;
    }

    private void syncCheckboxes() {
        this.waitFullCheckbox.checked = this.wait_full;
        this.waitEmptyCheckbox.checked = this.wait_empty;
        this.waitCheckbox.checked = this.wait_seconds;
        this.roleManualCheckbox.checked = this.role_manual;
        this.roleLoadCheckbox.checked = this.role_load;
        this.roleUnloadCheckbox.checked = this.role_unload;
    }

    private void configureFormElements(FromField fromField) {
        if (fromField == FromField.WAITSECONDS && this.wait_seconds) {
            this.wait_empty = false;
            this.wait_full = false;
            this.waitTimeInput.setActive(true);
            this.waitTimeInput.changedTyping(true);
            this.roleLoadCheckbox.setActive(true);
            this.roleUnloadCheckbox.setActive(true);
        }

        if (fromField == FromField.WAITEMPTY && this.wait_empty) {
            this.wait_seconds = false;
            this.wait_full = false;
            this.role_load = false;
            this.roleUnloadCheckbox.setActive(true);
            this.roleLoadCheckbox.setActive(false);
            this.waitTimeInput.setActive(false);
        }

        if (fromField == FromField.WAITFULL && this.wait_full) {
            this.wait_empty = false;
            this.wait_seconds = false;
            this.role_unload = false;
            this.roleLoadCheckbox.setActive(true);
            this.roleUnloadCheckbox.setActive(false);
            this.waitTimeInput.setActive(false);
        }

        if (fromField == FromField.ROLEMANUAL && this.role_manual) {
            this.role_load = false;
            this.role_unload = false;
        }

        if (fromField == FromField.ROLELOAD && this.role_load) {
            this.role_manual = false;
            this.role_unload = false;
        }

        if (fromField == FromField.ROLEUNLOAD && this.role_unload) {
            this.role_load = false;
            this.role_manual = false;
        }

        if (!this.wait_full && !this.wait_seconds && !this.wait_empty) {
            switch (fromField) {
                case WAITSECONDS:
                    this.wait_seconds = true;
                    break;
                case WAITEMPTY:
                    this.wait_empty = true;
                    break;
                case WAITFULL:
                    this.wait_full = true;
                    break;
                default:
                    this.wait_seconds = true;
                    break;
            }
        }

        if (!this.role_manual && !this.role_load && !this.role_unload) {
            switch (fromField) {
                case ROLEMANUAL:
                    this.role_manual = true;
                    break;
                case ROLELOAD:
                    this.role_load = true;
                    break;
                case ROLEUNLOAD:
                    this.role_unload = true;
                    break;
                default:
                    this.role_manual = true;
                    break;
            }
        }

        this.syncCheckboxes();
    }

    private enum FromField {
        INIT,
        WAITSECONDS,
        WAITEMPTY,
        WAITFULL,
        ROLEMANUAL,
        ROLELOAD,
        ROLEUNLOAD
    }
}
