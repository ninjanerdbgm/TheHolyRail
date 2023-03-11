package theholyrailmod;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.*;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.RailRunnerMob;
import theholyrailmod.theholyrail.RailRunnerMountItem;

@ModEntry
public class TheHolyRail {

    public void init() {
        System.out.println("Initializing The Holy Rail mod by kn0wmad1c. V0.4...");

        RailRunnerMountItem.registerRailRunnerMountItem();
        RailRunnerMob.registerRailRunnerMob();
        PoweredRailObject.registerPoweredRail();
    }

    public void initResources() {
        
    }

    public void postInit() {
        // Recipes
        // Powered Rail
        Recipes.registerModRecipe(new Recipe(
                "poweredrail",
                5,
                RecipeTechRegistry.ADVANCED_WORKSTATION,
                new Ingredient[]{
                        new Ingredient("wire", 5),
                        new Ingredient("goldbar", 2),
                        new Ingredient("ironbar", 1),
                        new Ingredient("anylog", 1)
                }
        ).showAfter("minecarttrack"));

        // Railrunner
        Recipes.registerModRecipe(new Recipe(
                "railrunner",
                1,
                RecipeTechRegistry.ADVANCED_WORKSTATION,
                new Ingredient[]{
                        new Ingredient("tungstenbar", 5),
                        new Ingredient("goldbar", 3),
                        new Ingredient("fireworkdispenser", 1)
                }
        ).showAfter("minecart"));
    }

}
