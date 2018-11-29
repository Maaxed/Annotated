package fr.max2.packeta.test.util;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomGuiUtils
{
	public static void drawRectWithSizedTexture(int x, int y, double z, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
		double f = 1.0D / textureWidth;
		double f1 = 1.0D / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        
        vertexbuffer.pos(x		  , y + height, z).tex( u * f		  , (v + height) * f1).endVertex();
        vertexbuffer.pos(x + width, y + height, z).tex((u + width) * f, (v + height) * f1).endVertex();
        vertexbuffer.pos(x + width, y		  , z).tex((u + width) * f,  v * f1			 ).endVertex();
        vertexbuffer.pos(x		  , y		  , z).tex( u * f		  ,  v * f1			 ).endVertex();
        
        tessellator.draw();
    }
}
