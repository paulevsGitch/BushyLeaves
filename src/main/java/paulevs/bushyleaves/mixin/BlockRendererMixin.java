package paulevs.bushyleaves.mixin;

import net.minecraft.block.BlockBase;
import net.minecraft.block.Leaves;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas.Sprite;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.impl.client.texture.StationBlockRendererProvider;
import net.modificationstation.stationapi.mixin.render.client.TessellatorAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import paulevs.bushyleaves.listeners.TextureListener;
import paulevs.bushyleaves.listeners.TextureListener.LeafTextureInfo;

import java.util.Random;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
	@Shadow
	private int textureOverride;
	
	@Shadow
	private BlockView blockView;
	
	@Shadow
	private int field_55;
	
	private Random leaves_random = new Random();
	
	@Inject(method = "render", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/block/BlockBase;updateBoundingBox(Lnet/minecraft/level/BlockView;III)V",
		shift = Shift.BEFORE
	), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void leaves_render(BlockBase block, int x, int y, int z, CallbackInfoReturnable<Boolean> info, int side) {
		byte meta = (byte) blockView.getTileMeta(x, y, z);
		LeafTextureInfo texture = TextureListener.getTexture(block, meta);
		if (texture != null) {
			if (!leaves_canRender(x, y, z)) {
				info.setReturnValue(false);
			}
			else if (textureOverride == -1) {
				leaves_largeCross(block, texture.bushyTexture, x, y, z, true);
				int above = blockView.getTileId(x, y + 1, z);
				if (above == BlockBase.SNOW.id || above == BlockBase.SNOW_BLOCK.id) {
					leaves_largeCross(block, texture.bushySnow, x, y, z, false);
					renderStandardBlock(block, x, y, z);
					
					textureOverride = texture.snowSide;
					TextureListener.renderTopAndBottom = false;
					renderStandardBlock(block, x, y, z);
					TextureListener.renderTopAndBottom = true;
					textureOverride = -1;
					
					info.setReturnValue(true);
				}
			}
		}
	}
	
	@Shadow
	public boolean renderStandardBlock(BlockBase block, int i, int j, int k) {
		return false;
	}
	
	@Shadow
	public void renderNorthFace(BlockBase arg, double d, double d1, double d2, int i) {}
	
	@Shadow
	public void renderSouthFace(BlockBase arg, double d, double d1, double d2, int i) {}
	
	@Shadow
	public void renderEastFace(BlockBase arg, double d, double d1, double d2, int i) {}
	
	@Shadow
	public void renderWestFace(BlockBase arg, double d, double d1, double d2, int i) {}
	
	private void leaves_largeCross(BlockBase block, int texture, int x, int y, int z, boolean colored) {
		Atlas atlas = Atlases.getStationTerrain();
		Tessellator tessellator = atlas.getTessellator();
		
		TessellatorAccessor original = TessellatorAccessor.class.cast(Tessellator.INSTANCE);
		if (!(TessellatorAccessor.class.cast(tessellator)).getDrawing()) {
			StationBlockRendererProvider provider = StationBlockRendererProvider.class.cast(this);
			provider.getStationBlockRenderer().activeAtlases.add(atlas);
			tessellator.start();
			tessellator.setOffset(original.getXOffset(), original.getYOffset(), original.getZOffset());
		}
		
		float light = block.getBrightness(this.blockView, x, y, z);
		if (block.isFullOpaque()) {
			light = leaves_getMaxLight(x, y, z);
		}
		int rgb = colored ? block.getColourMultiplier(blockView, x, y, z) : 16777215;
		tessellator.colour((int) (((rgb >> 16) & 255) * light), (int) (((rgb >> 8) & 255) * light), (int) ((rgb & 255) * light));
		
		Sprite sprite = atlas.getTexture(texture);
		double u1 = sprite.getStartU();
		double u2 = sprite.getEndU();
		double v1 = sprite.getStartV();
		double v2 = sprite.getEndV();
		
		leaves_random.setSeed(leaves_getSeed(x, y, z));
		float dx = leaves_random.nextFloat() * 0.125F - 0.0626F;
		float dy = leaves_random.nextFloat() * 0.125F - 0.0626F;
		float dz = leaves_random.nextFloat() * 0.125F - 0.0626F;
		
		double x1 = x - 0.45F + 0.5F - 0.5F + dx;
		double x2 = x + 0.45F + 0.5F + 0.5F + dx;
		double z1 = z + 0.5F - 0.45F - 0.5F + dz;
		double z2 = z + 0.5F + 0.45F + 0.5F + dz;
		double y1 = y - 0.5F + dy;
		double y2 = y + 1.5F + dy;
		
		tessellator.vertex(x1, y2, z1, u1, v1);
		tessellator.vertex(x1, y1, z1, u1, v2);
		tessellator.vertex(x2, y1, z2, u2, v2);
		tessellator.vertex(x2, y2, z2, u2, v1);
		tessellator.vertex(x2, y2, z2, u1, v1);
		tessellator.vertex(x2, y1, z2, u1, v2);
		tessellator.vertex(x1, y1, z1, u2, v2);
		tessellator.vertex(x1, y2, z1, u2, v1);
		tessellator.vertex(x1, y2, z2, u1, v1);
		tessellator.vertex(x1, y1, z2, u1, v2);
		tessellator.vertex(x2, y1, z1, u2, v2);
		tessellator.vertex(x2, y2, z1, u2, v1);
		tessellator.vertex(x2, y2, z1, u1, v1);
		tessellator.vertex(x2, y1, z1, u1, v2);
		tessellator.vertex(x1, y1, z2, u2, v2);
		tessellator.vertex(x1, y2, z2, u2, v1);
	}
	
	private int leaves_getSeed(int x, int y, int z) {
		int h = y + x * 374761393 + z * 668265263;
		h = (h ^ (h >> 13)) * 1274126177;
		return h ^ (h >> 16);
	}
	
	private boolean leaves_canRender(int x, int y, int z) {
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x + 1, y, z)])) return true;
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x - 1, y, z)])) return true;
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x, y + 1, z)])) return true;
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x, y - 1, z)])) return true;
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x, y, z + 1)])) return true;
		if (!leaves_solid(BlockBase.BY_ID[blockView.getTileId(x, y, z - 1)])) return true;
		return false;
	}
	
	private boolean leaves_solid(BlockBase block) {
		return block != null && (block instanceof Leaves || (block.isFullOpaque() && block.isFullCube()));
	}
	
	private float leaves_getMaxLight(int x, int y, int z) {
		float value = 0;
		
		value = Math.max(value, blockView.getBrightness(x, y - 1, z));
		value = Math.max(value, blockView.getBrightness(x, y + 1, z));
		
		if (value > 0) {
			return value;
		}
		
		value = Math.max(value, blockView.getBrightness(x - 1, y, z));
		value = Math.max(value, blockView.getBrightness(x + 1, y, z));
		value = Math.max(value, blockView.getBrightness(x, y, z - 1));
		value = Math.max(value, blockView.getBrightness(x, y, z + 1));
		
		return value;
	}
}
