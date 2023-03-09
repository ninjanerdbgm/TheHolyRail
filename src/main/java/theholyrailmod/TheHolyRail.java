package theholyrailmod;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.*;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import theholyrailmod.theholyrail.PoweredRailObject;

@ModEntry
public class TheHolyRail {

    public void init() {
        System.out.println("Initializing The Holy Rail mod by kn0wmad1c. V0.2...");

        PoweredRailObject.registerPoweredRail();
    }

    public void initResources() {
        
    }

    public void postInit() {
        // Add recipes
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
    }

}
