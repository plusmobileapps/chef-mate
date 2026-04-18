@file:OptIn(ExperimentalFoundationApi::class)

package com.plusmobileapps.chefmate.meal.core

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import chefmate.client.meal.core.public.generated.resources.Res
import chefmate.client.meal.core.public.generated.resources.meal_plan_breakfast
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_cancel
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_confirm
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_message
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_title
import chefmate.client.meal.core.public.generated.resources.meal_plan_dinner
import chefmate.client.meal.core.public.generated.resources.meal_plan_lunch
import chefmate.client.meal.core.public.generated.resources.meal_plan_no_meals
import chefmate.client.meal.core.public.generated.resources.meal_plan_snacks
import chefmate.client.meal.core.public.generated.resources.meal_plan_title
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun MealPlanScreen(bloc: MealPlanBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    state.mealToDelete?.let {
        PlusDialog(
            title = ResourceString(Res.string.meal_plan_delete_title),
            message = ResourceString(Res.string.meal_plan_delete_message),
            confirmButtonText = ResourceString(Res.string.meal_plan_delete_confirm),
            dismissButtonText = ResourceString(Res.string.meal_plan_delete_cancel),
            onConfirmClick = bloc::onDeleteMealConfirmed,
            onDismissRequest = bloc::onDeleteMealDismissed,
        )
    }

    PlusNavContainer(
        data =
            PlusHeaderData.Parent(
                title = Res.string.meal_plan_title.asTextData(),
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        IconButton(onClick = bloc::onViewModeToggled) {
                            Icon(
                                imageVector =
                                    when (state.viewMode) {
                                        MealPlanBloc.ViewMode.DAY -> Icons.Default.CalendarViewWeek
                                        MealPlanBloc.ViewMode.WEEK -> Icons.Default.CalendarViewDay
                                    },
                                contentDescription = null,
                            )
                        }
                    },
            ),
        scrollEnabled = false,
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                DateNavigationRow(
                    dateLabel = state.dateLabel.localized(),
                    onPrevious = bloc::onPreviousClicked,
                    onNext = bloc::onNextClicked,
                )

                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        PlusLoadingIndicator()
                    }
                } else {
                    when (state.viewMode) {
                        MealPlanBloc.ViewMode.DAY ->
                            DayView(
                                dayMeals = state.dayMeals,
                                onMealClicked = bloc::onMealClicked,
                                onDeleteClicked = bloc::onDeleteMealClicked,
                                modifier = Modifier.weight(1f),
                            )
                        MealPlanBloc.ViewMode.WEEK ->
                            WeekView(
                                weekMeals = state.weekMeals.orEmpty(),
                                onMealClicked = bloc::onMealClicked,
                                onDeleteClicked = bloc::onDeleteMealClicked,
                                modifier = Modifier.weight(1f),
                            )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun DateNavigationRow(
    dateLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = ChefMateTheme.dimens.paddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
        }
        Text(text = dateLabel, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun DayView(
    dayMeals: MealPlanBloc.DayMeals?,
    onMealClicked: (MealPlanItem) -> Unit,
    onDeleteClicked: (MealPlanItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dayMeals == null) {
        EmptyMealsMessage(modifier)
        return
    }

    val allEmpty =
        dayMeals.breakfast.isEmpty() &&
            dayMeals.lunch.isEmpty() &&
            dayMeals.dinner.isEmpty() &&
            dayMeals.snacks.isEmpty()

    if (allEmpty) {
        EmptyMealsMessage(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        if (dayMeals.breakfast.isNotEmpty()) {
            stickyHeader(key = "breakfast") {
                MealSectionHeader(stringResource(Res.string.meal_plan_breakfast))
            }
            items(dayMeals.breakfast, key = { it.id }) { meal ->
                MealItemCard(
                    meal = meal,
                    onClick = { onMealClicked(meal) },
                    onDeleteClick = { onDeleteClicked(meal) },
                )
            }
        }
        if (dayMeals.lunch.isNotEmpty()) {
            stickyHeader(key = "lunch") {
                MealSectionHeader(stringResource(Res.string.meal_plan_lunch))
            }
            items(dayMeals.lunch, key = { it.id }) { meal ->
                MealItemCard(
                    meal = meal,
                    onClick = { onMealClicked(meal) },
                    onDeleteClick = { onDeleteClicked(meal) },
                )
            }
        }
        if (dayMeals.dinner.isNotEmpty()) {
            stickyHeader(key = "dinner") {
                MealSectionHeader(stringResource(Res.string.meal_plan_dinner))
            }
            items(dayMeals.dinner, key = { it.id }) { meal ->
                MealItemCard(
                    meal = meal,
                    onClick = { onMealClicked(meal) },
                    onDeleteClick = { onDeleteClicked(meal) },
                )
            }
        }
        if (dayMeals.snacks.isNotEmpty()) {
            stickyHeader(key = "snacks") {
                MealSectionHeader(stringResource(Res.string.meal_plan_snacks))
            }
            items(dayMeals.snacks, key = { it.id }) { meal ->
                MealItemCard(
                    meal = meal,
                    onClick = { onMealClicked(meal) },
                    onDeleteClick = { onDeleteClicked(meal) },
                )
            }
        }
    }
}

@Composable
private fun WeekView(
    weekMeals: List<MealPlanBloc.DayGroup>,
    onMealClicked: (MealPlanItem) -> Unit,
    onDeleteClicked: (MealPlanItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (weekMeals.isEmpty() || weekMeals.all { it.meals.isEmpty() }) {
        EmptyMealsMessage(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        weekMeals.forEach { dayGroup ->
            if (dayGroup.meals.isNotEmpty()) {
                stickyHeader(key = "week_${dayGroup.dateLabel}") {
                    MealSectionHeader(dayGroup.dateLabel.localized())
                }
                items(dayGroup.meals, key = { it.id }) { meal ->
                    WeekMealItem(
                        meal = meal,
                        onClick = { onMealClicked(meal) },
                        onDeleteClick = { onDeleteClicked(meal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MealSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                ),
    )
}

@Composable
private fun MealItemCard(
    meal: MealPlanItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = meal.recipeTitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.meal_plan_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun WeekMealItem(
    meal: MealPlanItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = ChefMateTheme.dimens.paddingNormal),
            horizontalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = meal.mealType.displayName(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = meal.recipeTitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.meal_plan_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun EmptyMealsMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.meal_plan_no_meals),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MealType.displayName(): String =
    when (this) {
        MealType.BREAKFAST -> stringResource(Res.string.meal_plan_breakfast)
        MealType.LUNCH -> stringResource(Res.string.meal_plan_lunch)
        MealType.DINNER -> stringResource(Res.string.meal_plan_dinner)
        MealType.SNACKS -> stringResource(Res.string.meal_plan_snacks)
    }
