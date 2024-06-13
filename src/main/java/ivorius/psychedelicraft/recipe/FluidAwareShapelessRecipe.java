/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {
    public static final MapCodec<FluidAwareShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(FluidAwareShapelessRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FluidAwareShapelessRecipe::getCategory),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            OptionalFluidIngredient.LIST_CODEC.fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(instance, FluidAwareShapelessRecipe::new));
    public static final PacketCodec<RegistryByteBuf, FluidAwareShapelessRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FluidAwareShapelessRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, FluidAwareShapelessRecipe::getCategory,
            ItemStack.PACKET_CODEC, recipe -> recipe.output,
            OptionalFluidIngredient.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList(OptionalFluidIngredient.EMPTY)), recipe -> recipe.ingredients,
            FluidAwareShapelessRecipe::new
    );

    private final ItemStack output;
    private final DefaultedList<OptionalFluidIngredient> ingredients;
    private final List<OptionalFluidIngredient> fluidRestrictions;

    public FluidAwareShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<OptionalFluidIngredient> input) {
        super(group, category, output,
                // parent expects regular ingredients but we don't actually use them
                input.stream()
                .map(OptionalFluidIngredient::toVanillaIngredient)
                .collect(Collectors.toCollection(DefaultedList::of))
        );
        this.output = output;
        this.ingredients = input;
        this.fluidRestrictions = ingredients.stream().filter(i -> i.fluid().filter(f -> f.level() > 0).isPresent()).toList();
    }

    public DefaultedList<OptionalFluidIngredient> getFluidAwareIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        return inventory.getStacks().stream()
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count() == ingredients.size() && unmatchedInputs.isEmpty();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            Item item = stack.getItem();

            defaultedList.set(i, item.hasRecipeRemainder()
                ? new ItemStack(item.getRecipeRemainder())
                : fluidRestrictions.stream()
                        .filter(t -> t.test(stack))
                        .findFirst()
                        .flatMap(OptionalFluidIngredient::fluid)
                        .map(fluid -> MutableFluidContainer.of(stack).decrement(fluid.level()))
                        .map(MutableFluidContainer::asStack)
                        .orElse(ItemStack.EMPTY)
            );
        }
        return defaultedList;
    }
}
