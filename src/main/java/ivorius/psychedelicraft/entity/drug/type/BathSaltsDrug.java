/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by Sollace on Feb 6 2023.
 */
public class BathSaltsDrug extends SimpleDrug {
    public BathSaltsDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.BATH_SALTS, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getWorld().random;

            if (!entity.getWorld().isClient) {
                double chance = (getActiveValue() - 0.8F) * 0.051F;

                if (entity.age % 20 == 0 && random.nextFloat() < chance) {
                    if (random.nextFloat() < 0.4F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.STROKE), Integer.MAX_VALUE);
                    } else if (random.nextFloat() < 0.5F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.HEART_FAILURE), Integer.MAX_VALUE);
                    } else if (random.nextFloat() < 0.5F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.RESPIRATORY_FAILURE), Integer.MAX_VALUE);
                    } else if (random.nextFloat() < 0.5F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.KIDNEY_FAILURE), Integer.MAX_VALUE);
                    }
                }
            }
        }
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        if (getActiveValue() > 0) {
            Random random = drugProperties.asEntity().getWorld().random;

            if (random.nextFloat() < 0.5) {
                drugProperties.asEntity().damage(
                        drugProperties.damageOf(random.nextFloat() < 0.002 ? PSDamageTypes.KIDNEY_FAILURE : PSDamageTypes.IN_SLEEP),
                        Integer.MAX_VALUE
                );
            } else {
                drugProperties.asEntity().addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0, false, false, false));
                super.onWakeUp(drugProperties);
            }
        } else {
            super.onWakeUp(drugProperties);
        }
    }

    @Override
    public float randomJumpChance() {
        return MathUtils.inverseLerp((float) getActiveValue(), 0.6F, 1) * 0.03F;
    }

    @Override
    public float randomPunchChance() {
        return MathUtils.inverseLerp((float) getActiveValue(), 0.5F, 1) * 0.02F;
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 0.8F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue();
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float) getActiveValue() * 0.12F;
    }

    @Override
    public float colorInversionHallucinationStrength() {
        float value = (float) getActiveValue();
        value *= value;
        return MathHelper.clamp(value * 5.3F, 0, 1.5F);
    }
}
