package fr.skylined.spectral.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.skylined.spectral.component.ModComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record PrismStandRecipe(
        Item ingredient,
        int processingTime,
        int minLightLevel,
        Result result
) implements Recipe<SingleRecipeInput> {

    public record Result(
            Item item,
            float wavelengthMin,
            float wavelengthMax,
            boolean useSkyLight
    ) {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Result::item),
                Codec.FLOAT.fieldOf("wavelength_min").forGetter(Result::wavelengthMin),
                Codec.FLOAT.fieldOf("wavelength_max").forGetter(Result::wavelengthMax),
                Codec.BOOL.optionalFieldOf("use_sky_light", true).forGetter(Result::useSkyLight)
        ).apply(inst, Result::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Result> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.idMapper(BuiltInRegistries.ITEM), Result::item,
                ByteBufCodecs.FLOAT, Result::wavelengthMin,
                ByteBufCodecs.FLOAT, Result::wavelengthMax,
                ByteBufCodecs.BOOL, Result::useSkyLight,
                Result::new
        );

        // MC 26.1 Timelines : jour 1000-12600, nuit 12600-23401 (periode 24000)
        public float calculateWavelength(Level level) {
            if (useSkyLight) {
                long dayTime = level.getOverworldClockTime() % 24000L;
                if (dayTime >= 12600 && dayTime <= 23401) return -1;
                float dayProgress = (float) Math.max(0, Math.min(dayTime, 12600L));
                float t = dayProgress / 12600.0f;
                return wavelengthMin + t * (wavelengthMax - wavelengthMin);
            }
            return (wavelengthMin + wavelengthMax) / 2.0f;
        }

        public ItemStack assemble(Level level) {
            float wavelength = calculateWavelength(level);
            if (wavelength < 0) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(item);
            stack.set(ModComponents.WAVE_LENGTH.get(), wavelength);
            return stack;
        }
    }

    private static final RecipeBookCategory CATEGORY = new RecipeBookCategory();

    public static final MapCodec<PrismStandRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("ingredient").forGetter(PrismStandRecipe::ingredient),
            Codec.INT.fieldOf("processing_time").forGetter(PrismStandRecipe::processingTime),
            Codec.INT.fieldOf("min_light_level").forGetter(PrismStandRecipe::minLightLevel),
            Result.CODEC.fieldOf("result").forGetter(PrismStandRecipe::result)
    ).apply(inst, PrismStandRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrismStandRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(BuiltInRegistries.ITEM), PrismStandRecipe::ingredient,
            ByteBufCodecs.INT, PrismStandRecipe::processingTime,
            ByteBufCodecs.INT, PrismStandRecipe::minLightLevel,
            Result.STREAM_CODEC, PrismStandRecipe::result,
            PrismStandRecipe::new
    );

    public boolean matches(ItemStack stack, int lightLevel) {
        return stack.getItem() == ingredient && lightLevel >= minLightLevel;
    }

    // true = pas affiche dans le recipe book vanilla, evite le warning "will be ignored"
    @Override
    public boolean isSpecial() { return true; }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) { return false; }

    @Override
    public ItemStack assemble(SingleRecipeInput input) { return ItemStack.EMPTY; }

    @Override
    public boolean showNotification() { return false; }

    @Override
    public String group() { return ""; }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

    @Override
    public RecipeBookCategory recipeBookCategory() { return CATEGORY; }

    @Override
    public RecipeSerializer<PrismStandRecipe> getSerializer() {
        return ModRecipes.PRISM_STAND_SERIALIZER.get();
    }

    @Override
    public RecipeType<PrismStandRecipe> getType() {
        return ModRecipes.PRISM_STAND.get();
    }
}
