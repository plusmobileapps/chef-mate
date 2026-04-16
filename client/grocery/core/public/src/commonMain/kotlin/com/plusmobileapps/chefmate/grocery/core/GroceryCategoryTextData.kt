package com.plusmobileapps.chefmate.grocery.core

import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_category_bakery
import chefmate.client.grocery.core.public.generated.resources.grocery_category_baking
import chefmate.client.grocery.core.public.generated.resources.grocery_category_beverages
import chefmate.client.grocery.core.public.generated.resources.grocery_category_canned_goods
import chefmate.client.grocery.core.public.generated.resources.grocery_category_condiments
import chefmate.client.grocery.core.public.generated.resources.grocery_category_dairy
import chefmate.client.grocery.core.public.generated.resources.grocery_category_frozen
import chefmate.client.grocery.core.public.generated.resources.grocery_category_grains
import chefmate.client.grocery.core.public.generated.resources.grocery_category_meat
import chefmate.client.grocery.core.public.generated.resources.grocery_category_other
import chefmate.client.grocery.core.public.generated.resources.grocery_category_produce
import chefmate.client.grocery.core.public.generated.resources.grocery_category_snacks
import chefmate.client.grocery.core.public.generated.resources.grocery_category_spices
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData

fun GroceryCategory.displayName(): TextData =
    when (this) {
        GroceryCategory.PRODUCE -> Res.string.grocery_category_produce.asTextData()
        GroceryCategory.DAIRY -> Res.string.grocery_category_dairy.asTextData()
        GroceryCategory.MEAT -> Res.string.grocery_category_meat.asTextData()
        GroceryCategory.BAKERY -> Res.string.grocery_category_bakery.asTextData()
        GroceryCategory.FROZEN -> Res.string.grocery_category_frozen.asTextData()
        GroceryCategory.CANNED_GOODS -> Res.string.grocery_category_canned_goods.asTextData()
        GroceryCategory.CONDIMENTS -> Res.string.grocery_category_condiments.asTextData()
        GroceryCategory.SNACKS -> Res.string.grocery_category_snacks.asTextData()
        GroceryCategory.BEVERAGES -> Res.string.grocery_category_beverages.asTextData()
        GroceryCategory.GRAINS -> Res.string.grocery_category_grains.asTextData()
        GroceryCategory.BAKING -> Res.string.grocery_category_baking.asTextData()
        GroceryCategory.SPICES -> Res.string.grocery_category_spices.asTextData()
        GroceryCategory.OTHER -> Res.string.grocery_category_other.asTextData()
    }
