package lightOverlay.patch;

import lightOverlay.LightOverlay;
import lightOverlay.OverlayDrawable;
import lightOverlay.TorchGridOverlay;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.SharedTextureDrawOptions;
import necesse.gfx.drawables.LevelDrawUtils;
import necesse.gfx.drawables.LevelTileTerrainDrawOptions;
import necesse.gfx.drawables.LevelTileLightDrawOptions;
import necesse.gfx.drawables.LevelTileLiquidDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.drawables.LevelTileDamageDrawOptions;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

import java.util.List;

public class LevelDrawUtilsPatch {
    @ModMethodPatch(target = LevelDrawUtils.class, name = "addTileBasedDrawProcesses", arguments = {TickManager.class, GameCamera.class, LevelDrawUtils.DrawArea.class, LevelDrawUtils.DrawArea.class, LevelTileTerrainDrawOptions.class, LevelTileLiquidDrawOptions.class, LevelTileTerrainDrawOptions.class, LevelTileDamageDrawOptions.class, LevelTileLightDrawOptions.class, SharedTextureDrawOptions.class, SharedTextureDrawOptions.class, OrderableDrawables.class, List.class, boolean.class, PlayerMob.class})
    public static class AddTileBasedDrawProcessesPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.FieldValue("level") Level level, @Advice.Argument(1) GameCamera camera, @Advice.Argument(2) LevelDrawUtils.DrawArea tileArea, @Advice.Argument(11) OrderableDrawables list) {
            boolean drawOverlay = LightOverlay.settings.drawOverlay;
            boolean drawGrid = LightOverlay.settings.showGrid;
            if ((!drawOverlay && !drawGrid) || level.lightManager.ambientLightOverride != null)
                return;
            int startX = tileArea.startTileX, startY = tileArea.startTileY;
            boolean[][] spawnable = new boolean[tileArea.endTileX - startX + 1][tileArea.endTileY - startY + 1];
            for (int tileX = startX; tileX <= tileArea.endTileX; tileX++) {
                for (int tileY = startY; tileY <= tileArea.endTileY; tileY++) {
                    boolean canSpawn = LightOverlay.canMobSpawn(level, tileX, tileY);
                    spawnable[tileX - startX][tileY - startY] = canSpawn;
                    if (drawOverlay && canSpawn) {
                        list.add(new OverlayDrawable(tileX, tileY, LightOverlay.lowLightTexture.initDraw()
                                .alpha(LightOverlay.settings.alpha).pos(camera.getTileDrawX(tileX), camera.getTileDrawY(tileY))));
                    }
                }
            }
            if (drawGrid)
                TorchGridOverlay.addDrawables(level, camera, startX, startY, tileArea.endTileX, tileArea.endTileY, spawnable, list);
        }
    }
}
