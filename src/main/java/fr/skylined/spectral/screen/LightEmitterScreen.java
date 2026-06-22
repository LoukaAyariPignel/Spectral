package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LightEmitterScreen extends AbstractContainerScreen<LightEmitterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/light_emitter.png");

    public LightEmitterScreen(LightEmitterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 120);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);

        int stored = menu.getStoredPhotons();
        int max    = Math.max(1, menu.getMaxPhotons());

        // Buffer bar fill
        int barW = (imageWidth - 18) * stored / max;
        if (barW > 0)
            g.fill(x + 9, y + 35, x + 9 + barW, y + 45, 0xFF9B59B6);
        g.centeredText(this.font, stored + " / " + max + " PH", x + imageWidth / 2, y + 37, 0xFF404040);

        // Direction indicator inside inset at y+81
        boolean emit = menu.isEmitting();
        Direction dir = menu.getFacing();
        int dirColor = emit ? 0xFF228B22 : 0xFF888888;
        String arrow = switch (dir) {
            case NORTH -> "^"; case SOUTH -> "v"; case EAST -> ">"; case WEST -> "<"; default -> "?";
        };
        g.centeredText(this.font, arrow + " " + dir.getName().toUpperCase(), x + imageWidth / 2, y + 82, dirColor);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.centeredText(this.font, this.title, this.imageWidth / 2, this.titleLabelY, 0x404040);

        boolean emit = menu.isEmitting();
        g.text(this.font, "Buffer", 8, 23, 0x404040);
        g.text(this.font, "Status: " + (emit ? "Emitting" : "Standby"),
                8, 32, emit ? 0x228B22 : 0x8B0000);
        g.text(this.font, "Direction", 8, 70, 0x555555);
    }
}
