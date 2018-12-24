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
		this.addButton(new GuiButton(1, this.width / 2, this.height / 5, "Send packet"));
		this.addButton(new GuiButton(2, this.width / 2, this.height / 5 * 2, "Send packet"));
		this.addButton(new GuiButton(3, this.width / 2, this.height / 5 * 3, "Send packet"));
		this.addButton(new GuiButton(4, this.width / 2, this.height / 5 * 4, "Send packet"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		SimpleDataMessage message = new SimpleDataMessage(button.id);
		
		ModTestPacketa.MOD_CHANNEL.sendToServer(message);
		
		super.actionPerformed(button);
	}
}
