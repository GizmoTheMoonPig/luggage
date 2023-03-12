package com.gizmo.luggage.client;

import com.gizmo.luggage.entity.AbstractLuggage;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LuggageModel<T extends AbstractLuggage> extends HierarchicalModel<T> {

	private final ModelPart root;
	private final ModelPart lid;
	private final ModelPart[] legs = new ModelPart[21];

	public LuggageModel(ModelPart root) {
		this.root = root;
		this.lid = root.getChild("lid");

		for (int i = 0; i < legs.length; i++) {
			this.legs[i] = root.getChild("leg_" + i);
		}
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition partDefinition = mesh.getRoot();

		partDefinition.addOrReplaceChild("lid", CubeListBuilder.create()
						.texOffs(0, 22)
						.addBox(-8.0F, -1.0F, -8.0F, 16.0F, 2.0F, 8.0F, new CubeDeformation(0.01F)),
				PartPose.offset(0.0F, 13.0F, 4.0F));

		partDefinition.addOrReplaceChild("base", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-8.0F, 0.0F, -4.0F, 16.0F, 7.0F, 8.0F),
				PartPose.offset(0.0F, 13.0F, 0.0F));

		for (int i = 0; i < 21; i++) {
			int x = i % 7;
			int z = i % 3;
			partDefinition.addOrReplaceChild("leg_" + i, CubeListBuilder.create()
							.texOffs(0, 0)
							.addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
					PartPose.offset((float) x * 2 - 6.0F, 20F, (float) z * 2 - 2.0F));
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
		this.lid.xRot = Math.min(0, Mth.cos(limbSwing * 0.5F) * 1.4F * limbSwingAmount * 0.75F);

		for (int i = 0; i < legs.length; i++) {
			int x = i % 7;
			int z = i % 3;
			this.legs[i].xRot = Mth.cos(limbSwing + (x * z) * 0.6662F) * 1.4F * limbSwingAmount;
		}
	}
}
