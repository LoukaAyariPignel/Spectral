package fr.skylined.spectral.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("spectral")
                .then(Commands.literal("beam")
                    .then(Commands.literal("sweep")
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            var entity = source.getEntity();
                            if (entity == null) {
                                source.sendFailure(Component.literal("Must be run by a player"));
                                return 0;
                            }
                            HitResult hit = entity.pick(5.0, 0, false);
                            if (hit instanceof BlockHitResult bhr) {
                                BlockPos pos = bhr.getBlockPos();
                                if (source.getLevel().getBlockEntity(pos) instanceof LightEmitterBlockEntity be) {
                                    boolean nowSweeping = !be.isSweeping();
                                    be.setSweeping(nowSweeping);
                                    source.sendSuccess(() -> Component.literal(
                                            "Beam sweep " + (nowSweeping ? "ON" : "OFF")), false);
                                    return 1;
                                }
                            }
                            source.sendFailure(Component.literal("Look at a Light Emitter block (within 5 blocks)"));
                            return 0;
                        })
                    )
                    .then(Commands.argument("dx", FloatArgumentType.floatArg())
                    .then(Commands.argument("dy", FloatArgumentType.floatArg())
                    .then(Commands.argument("dz", FloatArgumentType.floatArg())
                        .executes(ctx -> {
                            float dx = FloatArgumentType.getFloat(ctx, "dx");
                            float dy = FloatArgumentType.getFloat(ctx, "dy");
                            float dz = FloatArgumentType.getFloat(ctx, "dz");

                            var source = ctx.getSource();
                            var entity = source.getEntity();
                            if (entity == null) {
                                source.sendFailure(Component.literal("Must be run by a player"));
                                return 0;
                            }

                            HitResult hit = entity.pick(5.0, 0, false);
                            if (hit instanceof BlockHitResult bhr) {
                                BlockPos pos = bhr.getBlockPos();
                                if (source.getLevel().getBlockEntity(pos) instanceof LightEmitterBlockEntity be) {
                                    be.setSweeping(false);
                                    be.setDirection(dx, dy, dz);
                                    source.sendSuccess(() -> Component.literal(
                                            "Beam direction set to (" + dx + ", " + dy + ", " + dz + ")"), false);
                                    return 1;
                                }
                            }
                            source.sendFailure(Component.literal("Look at a Light Emitter block (within 5 blocks)"));
                            return 0;
                        })
                    )))
                )
        );
    }
}
