package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SolarCollectorScreen extends AbstractContainerScreen<SolarCollectorMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/solar_collector.png");

    public SolarCollectorScreen(SolarCollectorMenu menu, Inventory inv, Component title) {
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
        int prod   = menu.getProduction();

        // Energy bar fill (inside inset at x+9, y+35 to x+W-10, y+45)
        int barW = (imageWidth - 18) * stored / max;
        if (barW > 0)
            g.fill(x + 9, y + 35, x + 9 + barW, y + 45, 0xFFFF8C00);

        // Bar label
        g.centeredText(this.font, stored + " / " + max + " PH", x + imageWidth / 2, y + 37, 0xFF404040);

        // Efficiency bar (at y+H-15 to y+H-9)
        int effW = (imageWidth - 18) * prod / 10;
        if (effW > 0)
            g.fill(x + 9, y + imageHeight - 15, x + 9 + effW, y + imageHeight - 9,
                    prod > 0 ? 0xFF4CAF50 : 0xFF8B0000);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.centeredText(this.font, this.title, this.imageWidth / 2, this.titleLabelY, 0x404040);

        int prod = menu.getProduction();
        boolean active = prod > 0;
        g.text(this.font, "Production: " + prod + " PH/t", 8, 23, 0x404040);
        g.text(this.font, "Status: " + (active ? "Active" : "Inactive"),
                8, 32, active ? 0x228B22 : 0x8B0000);
        g.text(this.font, "Solar efficiency", 8, imageHeight - 22, 0x555555);
    }
}
