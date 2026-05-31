package com.plusmobileapps.chefmate.recipe.categories

import chefmate.client.recipe.categories.public.generated.resources.Res
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_ai
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_appetizer
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_breakfast
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_dessert
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_dinner
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_drink
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_lunch
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_other
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_side
import chefmate.client.recipe.categories.public.generated.resources.recipe_category_snack
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import org.jetbrains.compose.resources.StringResource

fun BuiltinCategory.pickerLabelRes(): StringResource =
    when (this) {
        BuiltinCategory.BREAKFAST -> Res.string.recipe_category_breakfast
        BuiltinCategory.LUNCH -> Res.string.recipe_category_lunch
        BuiltinCategory.DINNER -> Res.string.recipe_category_dinner
        BuiltinCategory.APPETIZER -> Res.string.recipe_category_appetizer
        BuiltinCategory.SIDE -> Res.string.recipe_category_side
        BuiltinCategory.DESSERT -> Res.string.recipe_category_dessert
        BuiltinCategory.SNACK -> Res.string.recipe_category_snack
        BuiltinCategory.DRINK -> Res.string.recipe_category_drink
        BuiltinCategory.OTHER -> Res.string.recipe_category_other
        BuiltinCategory.AI -> Res.string.recipe_category_ai
    }
