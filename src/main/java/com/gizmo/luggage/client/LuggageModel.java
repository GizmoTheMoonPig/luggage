package com.gizmo.luggage.client;

import com.gizmo.luggage.entity.LuggageEntity;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public class LuggageModel extends SegmentedModel<LuggageEntity> {

	private final ModelRenderer lid;
	public final ModelRenderer horns;
	private final ModelRenderer base;
	private final ModelRenderer[] legs = new ModelRenderer[21];
	private final ImmutableList<ModelRenderer> parts;

	public LuggageModel() {
		this.texWidth = 64;
		this.texHeight = 32;
		this.lid = new ModelRenderer(this, 0, 22);
		this.lid.addBox(-8.0F, -1.0F, -8.0F, 16.0F, 2.0F, 8.0F, 0.01F);
		this.lid.setPos(0.0F, 13.0F, 4.0F);

		this.horns = new ModelRenderer(this, 40, 0);
		this.horns.addBox(-4.5F, -6.0F, -4.0F, 9.0F, 5.0F, 0.0F);
		this.lid.addChild(this.horns);

		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(-8.0F, 0.0F, -4.0F, 16.0F, 7.0F, 8.0F);
		this.base.setPos(0.0F, 13.0F, 0.0F);

		for (int i = 0; i < legs.length; i++) {
			int x = i % 7;
			int z = i % 3;
			this.legs[i] = new ModelRenderer(this, 0, 0);
			this.legs[i].addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F);
			this.legs[i].setPos((float) x * 2 - 6.0F, 20F, (float) z * 2 - 2.0F);
		}

		ImmutableList.Builder<ModelRenderer> builder = ImmutableList.builder();
		builder.add(this.lid, this.base);
		builder.addAll(Arrays.asList(this.legs));
		this.parts = builder.build();
	}

	@Override
	public void setupAnim(LuggageEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
		this.lid.xRot = Math.min(0, MathHelper.cos(limbSwing * 0.5F) * 1.4F * limbSwingAmount * 0.75F);

		this.horns.visible = true;

		for (int i = 0; i < legs.length; i++) {
			int x = i % 7;
			int z = i % 3;
			this.legs[i].xRot = MathHelper.cos(limbSwing + (x * z) * 0.6662F) * 1.4F * limbSwingAmount;
		}
	}

	@Override
	public Iterable<ModelRenderer> parts() {
		return this.parts;
	}
}
