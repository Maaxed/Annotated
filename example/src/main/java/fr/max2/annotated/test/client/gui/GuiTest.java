package fr.max2.annotated.test.client.gui;

import fr.max2.annotated.test.network.SimpleData_onServerReceive;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTest extends Screen
{
	public GuiTest()
	{
		super(new StringTextComponent("This is a test screen"));
	}
	
	@Override
	protected void init()
	{
		for (int i = 1; i <= 4; i++)
		{
			final int index = i;
			this.addButton(new Button(this.width / 2, this.height / 5 * i, 200, 20, new StringTextComponent("Send packet " + i), (b) ->
			{
				SimpleData_onServerReceive.sendToServer(index);
			}));
		}
	}
}
