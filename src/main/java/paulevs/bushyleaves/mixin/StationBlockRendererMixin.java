package paulevs.bushyleaves.mixin;

import net.minecraft.block.BlockBase;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.impl.client.texture.StationBlockRenderer;
import net.modificationstation.stationapi.mixin.render.client.BlockRendererAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.bushyleaves.listeners.TextureListener;

@Mixin(value = StationBlockRenderer.class, remap = false)
public class StationBlockRendererMixin {
	@Final
	@Shadow
	public BlockRendererAccessor blockRendererAccessor;
	
	@ModifyVariable(method = "renderNorthFace", at = @At("STORE"), ordinal = 0)
	private Atlas leaves_changeNorth(Atlas atlas) {
		int override = blockRendererAccessor.getTextureOverride();
		if (override > 255) {
			return Atlases.getStationTerrain();
		}
		return atlas;
	}
	
	@ModifyVariable(method = "renderSouthFace", at = @At("STORE"), ordinal = 0)
	private Atlas leaves_changeSouth(Atlas atlas) {
		int override = blockRendererAccessor.getTextureOverride();
		if (override > 255) {
			return Atlases.getStationTerrain();
		}
		return atlas;
	}
	
	@ModifyVariable(method = "renderEastFace", at = @At("STORE"), ordinal = 0)
	private Atlas leaves_changeEast(Atlas atlas) {
		int override = blockRendererAccessor.getTextureOverride();
		if (override > 255) {
			return Atlases.getStationTerrain();
		}
		return atlas;
	}
	
	@ModifyVariable(method = "renderWestFace", at = @At("STORE"), ordinal = 0)
	private Atlas leaves_changeWest(Atlas atlas) {
		int override = blockRendererAccessor.getTextureOverride();
		if (override > 255) {
			return Atlases.getStationTerrain();
		}
		return atlas;
	}
	
	@Inject(method = "renderBottomFace", at = @At("HEAD"), cancellable = true)
	public void leaves_renderBottomFace(BlockBase block, double renderX, double renderY, double renderZ, int textureIndex, boolean renderingInInventory, CallbackInfo info) {
		if (!TextureListener.renderTopAndBottom) {
			info.cancel();
		}
	}
	
	@Inject(method = "renderTopFace", at = @At("HEAD"), cancellable = true)
	public void leaves_renderTopFace(BlockBase block, double renderX, double renderY, double renderZ, int textureIndex, boolean renderingInInventory, CallbackInfo info) {
		if (!TextureListener.renderTopAndBottom) {
			info.cancel();
		}
	}
}
