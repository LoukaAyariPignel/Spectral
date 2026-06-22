package fr.skylined.spectral.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.skylined.spectral.component.ModComponents;
import fr.skylined.spectral.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record PrismStandRecipe(
        Ingredient ingredient,
        int processingTime,
        int minLightLevel,
        float wavelengthMin,
        float wavelengthMax,
        boolean useSkyLight
) implements Recipe<SingleRecipeInput> {

    private static final RecipeBookCategory CATEGORY = new RecipeBookCategory();

    public static final MapCodec<PrismStandRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PrismStandRecipe::ingredient),
            Codec.INT.fieldOf("processing_time").forGetter(PrismStandRecipe::processingTime),
            Codec.INT.fieldOf("min_light_level").forGetter(PrismStandRecipe::minLightLevel),
            Codec.FLOAT.fieldOf("wavelength_min").forGetter(PrismStandRecipe::wavelengthMin),
            Codec.FLOAT.fieldOf("wavelength_max").forGetter(PrismStandRecipe::wavelengthMax),
            Codec.BOOL.optionalFieldOf("use_sky_light", true).forGetter(PrismStandRecipe::useSkyLight)
    ).apply(inst, PrismStandRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrismStandRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, PrismStandRecipe::ingredient,
            ByteBufCodecs.INT, PrismStandRecipe::processingTime,
            ByteBufCodecs.INT, PrismStandRecipe::minLightLevel,
            ByteBufCodecs.FLOAT, PrismStandRecipe::wavelengthMin,
            ByteBufCodecs.FLOAT, PrismStandRecipe::wavelengthMax,
            ByteBufCodecs.BOOL, PrismStandRecipe::useSkyLight,
            PrismStandRecipe::new
    );

    // Vérifie si l'item correspond ET si la lumière est suffisante
    public boolean matches(ItemStack stack, int lightLevel) {
        return ingredient.test(stack) && lightLevel >= minLightLevel;
    }

    // Calcule la longueur d'onde selon les conditions actuelles
    // Retourne -1 si la conversion ne peut pas se faire (nuit, manque de lumière)
    // MC 26.1 Timelines : jour de 1000 à 12600, nuit de 12600 à 23401 (période 24000)
    public float calculateWavelength(Level level) {
        if (useSkyLight) {
            long dayTime = level.getOverworldClockTime() % 24000L;
            // Nuit : 12600 → 23401 (soleil sous l'horizon)
            if (dayTime >= 12600 && dayTime <= 23401) return -1;
            // Normalisation : jour va de ~1000 (lever) à ~12600 (coucher)
            // t=0 à l'aube, t=1 au coucher → λ du violet au rouge
            float dayProgress = (float) Math.max(0, Math.min(dayTime, 12600L));
            float t = dayProgress / 12600.0f;
            return wavelengthMin + t * (wavelengthMax - wavelengthMin);
        }
        return (wavelengthMin + wavelengthMax) / 2.0f;
    }

    // Produit la gemme avec la longueur d'onde calculée
    public ItemStack assemble(Level level) {
        float wavelength = calculateWavelength(level);
        if (wavelength < 0) return ItemStack.EMPTY;
        ItemStack gem = new ItemStack(ModItems.GEM.get());
        gem.set(ModComponents.WAVE_LENGTH.get(), wavelength);
        return gem;
    }

    // --- Méthodes Recipe<SingleRecipeInput> non utilisées par notre machine ---

    // true = pas affiché dans le recipe book vanilla, évite le warning "will be ignored"
    @Override
    public boolean isSpecial() { return true; }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return CATEGORY;
    }

    @Override
    public RecipeSerializer<PrismStandRecipe> getSerializer() {
        return ModRecipes.PRISM_STAND_SERIALIZER.get();
    }

    @Override
    public RecipeType<PrismStandRecipe> getType() {
        return ModRecipes.PRISM_STAND.get();
    }
}
