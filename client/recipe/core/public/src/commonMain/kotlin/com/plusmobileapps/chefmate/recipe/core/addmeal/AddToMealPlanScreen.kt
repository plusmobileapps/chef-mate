@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.recipe.core.addmeal

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_breakfast
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_dinner
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_lunch
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_save
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_select_date
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_select_meal
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_snacks
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_title
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddToMealPlanScreen(bloc: AddToMealPlanBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val initialMillis =
        if (state.selectedDate.isNotEmpty()) {
            LocalDate.parse(state.selectedDate).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        } else {
            null
        }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val instant = Instant.fromEpochMilliseconds(millis)
            val localDate = instant.toLocalDateTime(TimeZone.UTC).date
            bloc.onDateSelected(localDate.toString())
        }
    }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Modal(
                title = stringResource(Res.string.add_meal_plan_title).asTextData(),
                onCloseClick = bloc::onBackClicked,
            ),
        scrollEnabled = true,
        floatingActionButton = {
            if (state.selectedDate.isNotEmpty()) {
                ExtendedFloatingActionButton(onClick = bloc::onSaveClicked) {
                    if (state.isSaving) {
                        PlusLoadingIndicator()
                    } else {
                        Text(stringResource(Res.string.add_meal_plan_save))
                    }
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
            verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
        ) {
            if (state.recipeTitle.isNotEmpty()) {
                Text(text = state.recipeTitle, style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = stringResource(Res.string.add_meal_plan_select_meal),
                style = MaterialTheme.typography.labelLarge,
            )

            MealTypeRadioGroup(
                selectedType = state.selectedMealType,
                onMealTypeSelected = bloc::onMealTypeSelected,
            )

            Text(
                text = stringResource(Res.string.add_meal_plan_select_date),
                style = MaterialTheme.typography.labelLarge,
            )

            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun MealTypeRadioGroup(
    selectedType: MealType,
    onMealTypeSelected: (MealType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MealTypeOption.entries.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = selectedType == option.type,
                    onClick = { onMealTypeSelected(option.type) },
                )
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private enum class MealTypeOption(
    val type: MealType,
    val labelRes: org.jetbrains.compose.resources.StringResource,
) {
    BREAKFAST(MealType.BREAKFAST, Res.string.add_meal_plan_breakfast),
    LUNCH(MealType.LUNCH, Res.string.add_meal_plan_lunch),
    DINNER(MealType.DINNER, Res.string.add_meal_plan_dinner),
    SNACKS(MealType.SNACKS, Res.string.add_meal_plan_snacks),
}
