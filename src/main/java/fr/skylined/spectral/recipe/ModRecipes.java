package fr.skylined.spectral.recipe;

import fr.skylined.spectral.Spectral;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Spectral.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Spectral.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<PrismStandRecipe>> PRISM_STAND =
            RECIPE_TYPES.register("prism_stand",
                    () -> RecipeType.simple(Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "prism_stand")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PrismStandRecipe>> PRISM_STAND_SERIALIZER =
            RECIPE_SERIALIZERS.register("prism_stand",
                    () -> new RecipeSerializer<>(PrismStandRecipe.CODEC, PrismStandRecipe.STREAM_CODEC));
}
