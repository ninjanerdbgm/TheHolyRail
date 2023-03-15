package theholyrailmod;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.*;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import theholyrailmod.container.ChestMinecartContainer;
import theholyrailmod.theholyrail.ChestMinecartMob;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.RailRunnerMob;
import theholyrailmod.theholyrail.RailRunnerMountItem;
import theholyrailmod.theholyrail.StationTrackObject;
import theholyrailmod.theholyrail.ChestMinecartMountItem;

@ModEntry
public class TheHolyRail {
        //// SPECIAL SHOUT-OUTS
        // Thanks to Snoobinoob for a lot of help getting this mod going.
        // And special thanks to Fair to making this amazing game!
        //
        // Anyone interested in modding can't go wrong by joining the Discord.

        public void init() {
                System.out.println("Initializing The Holy Rail mod by kn0wmad1c. V0.4...");

                RailRunnerMountItem.registerRailRunnerMountItem();
                ChestMinecartMountItem.registerChestMinecartMountItem();
                RailRunnerMob.registerRailRunnerMob();
                ChestMinecartContainer.registerChestMinecartContainer();
                ChestMinecartMob.registerChestMinecartMob();
                PoweredRailObject.registerPoweredRail();
                StationTrackObject.registerStationTrack();
        }

        public void initResources() {
                RailRunnerMob.texture = MobRegistry.Textures.fromFile("railrunner");
                ChestMinecartMob.texture = MobRegistry.Textures.fromFile("chestminecart");
        }

        public void postInit() {
                // Recipes
                // Powered Rail
                Recipes.registerModRecipe(new Recipe(
                                "poweredrail",
                                5,
                                RecipeTechRegistry.DEMONIC,
                                new Ingredient[] {
                                                new Ingredient("wire", 5),
                                                new Ingredient("goldbar", 2),
                                                new Ingredient("ironbar", 1),
                                                new Ingredient("anylog", 1)
                                }).showAfter("minecarttrack"));
                // Station Track
                Recipes.registerModRecipe(new Recipe(
                                "stationtrack",
                                4,
                                RecipeTechRegistry.DEMONIC,
                                new Ingredient[] {
                                                new Ingredient("wire", 4),
                                                new Ingredient("tungstenbar", 1),
                                                new Ingredient("ironbar", 1),
                                                new Ingredient("anylog", 1)
                                }).showAfter("minecarttrack"));

                // Chest Minecart
                Recipes.registerModRecipe(new Recipe(
                                "chestminecart",
                                1,
                                RecipeTechRegistry.WORKSTATION,
                                new Ingredient[] {
                                                new Ingredient("minecart", 1),
                                                new Ingredient("storagebox", 1),
                                }).showAfter("minecart"));

                // Railrunner
                Recipes.registerModRecipe(new Recipe(
                                "railrunner",
                                1,
                                RecipeTechRegistry.DEMONIC,
                                new Ingredient[] {
                                                new Ingredient("tungstenbar", 5),
                                                new Ingredient("goldbar", 3),
                                                new Ingredient("fireworkdispenser", 1)
                                }).showAfter("chestminecart"));
        }

}
