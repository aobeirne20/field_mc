package com.gptwe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GptWeScreen extends Screen {
    private EditBox input;

    public GptWeScreen() {
        super(Component.literal("GPT -> WorldEdit"));
    }

    @Override
    protected void init() {
        input = new EditBox(
                this.font,
                10,
                this.height - 30,
                this.width - 120,
                20,
                Component.literal("Prompt")
        );
        input.setMaxLength(2000);
        this.addRenderableWidget(input);
        this.setInitialFocus(input);

        Button submitButton = Button.builder(
                Component.literal("Submit"),
                button -> submit(input.getValue())
        ).bounds(this.width - 100, this.height - 30, 90, 20).build();

        this.addRenderableWidget(submitButton);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    private void submit(String prompt) {
        Minecraft client = Minecraft.getInstance();

        new Thread(() -> {
            try {
                String response = OpenAiWeClient.getWorldEditText(prompt);
                client.execute(() -> WorldEditCommandRunner.runAllWorldEditLines(response));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "gptwe-openai").start();

        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(this.font, this.title, 10, 10, 0xFFFFFF);
    }
}