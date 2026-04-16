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
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData

fun GroceryCategory.displayName(): TextData =
    when (this) {
        GroceryCategory.PRODUCE -> ResourceString(Res.string.grocery_category_produce)
        GroceryCategory.DAIRY -> ResourceString(Res.string.grocery_category_dairy)
        GroceryCategory.MEAT -> ResourceString(Res.string.grocery_category_meat)
        GroceryCategory.BAKERY -> ResourceString(Res.string.grocery_category_bakery)
        GroceryCategory.FROZEN -> ResourceString(Res.string.grocery_category_frozen)
        GroceryCategory.CANNED_GOODS -> ResourceString(Res.string.grocery_category_canned_goods)
        GroceryCategory.CONDIMENTS -> ResourceString(Res.string.grocery_category_condiments)
        GroceryCategory.SNACKS -> ResourceString(Res.string.grocery_category_snacks)
        GroceryCategory.BEVERAGES -> ResourceString(Res.string.grocery_category_beverages)
        GroceryCategory.GRAINS -> ResourceString(Res.string.grocery_category_grains)
        GroceryCategory.BAKING -> ResourceString(Res.string.grocery_category_baking)
        GroceryCategory.SPICES -> ResourceString(Res.string.grocery_category_spices)
        GroceryCategory.OTHER -> ResourceString(Res.string.grocery_category_other)
    }
