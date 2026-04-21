@file:OptIn(ExperimentalFoundationApi::class)

package com.plusmobileapps.chefmate.recipe.core.addmeal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_breakfast
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_choose_date
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_dinner
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_lunch
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_next
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_no_meals
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_snacks
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChooseDateScreen(bloc: ChooseDateBloc, showAsChild: Boolean, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val headerData: PlusHeaderData =
        if (showAsChild) {
            PlusHeaderData.Child(
                title = stringResource(Res.string.add_meal_plan_choose_date).asTextData(),
                onBackClick = bloc::onBackClicked,
            )
        } else {
            PlusHeaderData.Modal(
                title = stringResource(Res.string.add_meal_plan_choose_date).asTextData(),
                onCloseClick = bloc::onBackClicked,
            )
        }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize(),
        data = headerData,
        scrollEnabled = false,
        floatingActionButton = {
            if (state.selectedDate != null) {
                ExtendedFloatingActionButton(onClick = bloc::onNextClicked) {
                    Text(stringResource(Res.string.add_meal_plan_next))
                }
            }
        },
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = ChefMateTheme.dimens.paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = bloc::onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }
            Text(text = state.monthLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = bloc::onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        MonthCalendar(
            firstDayOfMonth = state.firstDayOfMonth,
            selectedDate = state.selectedDate,
            daysWithMeals = state.daysWithMeals,
            onDaySelected = bloc::onDaySelected,
            modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
        )

        if (state.selectedDate != null) {
            SelectedDayMeals(
                meals = state.selectedDayMeals,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}

@Composable
private fun SelectedDayMeals(meals: List<MealPlanItem>, modifier: Modifier = Modifier) {
    if (meals.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.add_meal_plan_no_meals),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        val breakfast = meals.filter { it.mealType == MealType.BREAKFAST }
        val lunch = meals.filter { it.mealType == MealType.LUNCH }
        val dinner = meals.filter { it.mealType == MealType.DINNER }
        val snacks = meals.filter { it.mealType == MealType.SNACKS }

        LazyColumn(
            modifier = modifier.padding(horizontal = ChefMateTheme.dimens.paddingNormal),
            verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingSmall),
        ) {
            if (breakfast.isNotEmpty()) {
                stickyHeader(key = "breakfast") {
                    MealSectionHeader(stringResource(Res.string.add_meal_plan_breakfast))
                }
                items(breakfast, key = { it.id }) { meal -> MealRow(meal) }
            }
            if (lunch.isNotEmpty()) {
                stickyHeader(key = "lunch") {
                    MealSectionHeader(stringResource(Res.string.add_meal_plan_lunch))
                }
                items(lunch, key = { it.id }) { meal -> MealRow(meal) }
            }
            if (dinner.isNotEmpty()) {
                stickyHeader(key = "dinner") {
                    MealSectionHeader(stringResource(Res.string.add_meal_plan_dinner))
                }
                items(dinner, key = { it.id }) { meal -> MealRow(meal) }
            }
            if (snacks.isNotEmpty()) {
                stickyHeader(key = "snacks") {
                    MealSectionHeader(stringResource(Res.string.add_meal_plan_snacks))
                }
                items(snacks, key = { it.id }) { meal -> MealRow(meal) }
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
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = ChefMateTheme.dimens.paddingSmall),
    )
}

@Composable
private fun MealRow(meal: MealPlanItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = ChefMateTheme.dimens.paddingSmall),
        horizontalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            imageUrl = meal.recipeImageUrl,
            contentDescription = meal.recipeTitle,
            modifier = Modifier.size(40.dp),
        )
        Text(text = meal.recipeTitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MonthCalendar(
    firstDayOfMonth: LocalDate,
    selectedDate: LocalDate?,
    daysWithMeals: Set<String>,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysInMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).plus(-1, DateTimeUnit.DAY).day
    val firstDayOffset =
        when (firstDayOfMonth.dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier, verticalArrangement = spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayLabel in listOf("S", "M", "T", "W", "T", "F", "S")) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOffset + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = LocalDate(firstDayOfMonth.year, firstDayOfMonth.month, dayNumber)
                        DayCell(
                            day = dayNumber,
                            isSelected = date == selectedDate,
                            hasMeals = date.toString() in daysWithMeals,
                            onClick = { onDaySelected(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    hasMeals: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(32.dp)
                    .background(
                        color = if (isSelected) primaryColor else Color.Transparent,
                        shape = CircleShape,
                    ),
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) onPrimaryColor else onSurfaceColor,
                textAlign = TextAlign.Center,
            )
        }
        Box(modifier = Modifier.height(6.dp), contentAlignment = Alignment.Center) {
            if (hasMeals) {
                Box(
                    modifier =
                        Modifier.size(4.dp)
                            .background(
                                color = if (isSelected) onPrimaryColor else primaryColor,
                                shape = CircleShape,
                            )
                )
            }
        }
    }
}
