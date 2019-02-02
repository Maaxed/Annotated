package fr.max2.packeta.test.client.gui;

import java.io.IOException;

import fr.max2.packeta.test.ModTestPacketa;
import fr.max2.packeta.test.network.SimpleDataMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiTest extends GuiScreen
{
	@Override
	public void initGui()
	{
		for (int i = 1; i <= 4; i++)
		{
			this.addButton(new GuiButton(i, this.width / 2, this.height / 5 * i, "Send packet " + i));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		SimpleDataMessage message = new SimpleDataMessage(button.id);
		
		ModTestPacketa.MOD_CHANNEL.sendToServer(message);
		
		super.actionPerformed(button);
	}
}
