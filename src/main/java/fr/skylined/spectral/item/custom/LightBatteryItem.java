package fr.skylined.spectral.item.custom;

import fr.skylined.spectral.block.entity.LightBatteryBlockEntity;
import fr.skylined.spectral.component.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class LightBatteryItem extends BlockItem {

    private static final int BAR_WIDTH = 20;
    private final long maxCapacity;

    public LightBatteryItem(Block block, long maxCapacity, Properties properties) {
        super(block, properties);
        this.maxCapacity = maxCapacity;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, ctx, display, tooltip, flag);

        long stored = stack.has(ModComponents.STORED_PHOTONS.get())
                ? stack.get(ModComponents.STORED_PHOTONS.get())
                : 0L;

        int pct    = (int)(stored * 100L / maxCapacity);
        int filled = (int)(BAR_WIDTH * stored / maxCapacity);

        ChatFormatting fillColor = ChatFormatting.LIGHT_PURPLE;

        // ── Barre de progression ──────────────────────────────────────────────
        MutableComponent bar = Component.empty();
        bar.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY));
        if (filled > 0) {
            bar.append(Component.literal("█".repeat(filled)).withStyle(fillColor));
        }
        if (filled < BAR_WIDTH) {
            bar.append(Component.literal("░".repeat(BAR_WIDTH - filled))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        bar.append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));
        bar.append(Component.literal(pct + "%").withStyle(ChatFormatting.GRAY));
        tooltip.accept(bar);

        // ── Valeur PH ─────────────────────────────────────────────────────────
        tooltip.accept(Component.literal(
                String.format("%,d", stored) + " / " + String.format("%,d", maxCapacity) + " PH"
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
