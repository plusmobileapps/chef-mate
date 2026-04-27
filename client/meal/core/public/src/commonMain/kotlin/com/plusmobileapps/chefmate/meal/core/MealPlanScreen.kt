@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.meal.core

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.meal.core.public.generated.resources.Res
import chefmate.client.meal.core.public.generated.resources.meal_plan_add_meal
import chefmate.client.meal.core.public.generated.resources.meal_plan_breakfast
import chefmate.client.meal.core.public.generated.resources.meal_plan_day
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_cancel
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_confirm
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_message
import chefmate.client.meal.core.public.generated.resources.meal_plan_delete_title
import chefmate.client.meal.core.public.generated.resources.meal_plan_dinner
import chefmate.client.meal.core.public.generated.resources.meal_plan_lunch
import chefmate.client.meal.core.public.generated.resources.meal_plan_month
import chefmate.client.meal.core.public.generated.resources.meal_plan_no_meals
import chefmate.client.meal.core.public.generated.resources.meal_plan_snacks
import chefmate.client.meal.core.public.generated.resources.meal_plan_sync_not_synced
import chefmate.client.meal.core.public.generated.resources.meal_plan_sync_synced
import chefmate.client.meal.core.public.generated.resources.meal_plan_sync_syncing
import chefmate.client.meal.core.public.generated.resources.meal_plan_title
import chefmate.client.meal.core.public.generated.resources.meal_plan_week
import com.plusmobileapps.chefmate.meal.data.MealPlanItem
import com.plusmobileapps.chefmate.meal.data.MealType
import com.plusmobileapps.chefmate.meal.data.SyncStatus
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
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

    Box(modifier = modifier.fillMaxSize()) {
        PlusNavContainer(
            data = PlusHeaderData.Parent(title = Res.string.meal_plan_title.asTextData()),
            scrollEnabled = false,
            content = {
                Column(modifier = Modifier.fillMaxSize()) {
                    ViewModeSegmentedControl(
                        selectedMode = state.viewMode,
                        onModeSelected = bloc::onViewModeSelected,
                        modifier = Modifier.padding(horizontal = ChefMateTheme.dimens.paddingNormal),
                    )

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
                            MealPlanBloc.ViewMode.MONTH ->
                                MonthView(
                                    monthModel = state.monthModel,
                                    onDaySelected = bloc::onMonthDaySelected,
                                    onMealClicked = bloc::onMealClicked,
                                    onDeleteClicked = bloc::onDeleteMealClicked,
                                    modifier = Modifier.weight(1f),
                                )
                        }
                    }
                }
            },
        )

        ExtendedFloatingActionButton(
            onClick = bloc::onAddMealClicked,
            modifier =
                Modifier.align(Alignment.BottomEnd).padding(ChefMateTheme.dimens.paddingNormal),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(stringResource(Res.string.meal_plan_add_meal))
        }
    }
}

@Composable
private fun ViewModeSegmentedControl(
    selectedMode: MealPlanBloc.ViewMode,
    onModeSelected: (MealPlanBloc.ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = MealPlanBloc.ViewMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
            ) {
                Text(
                    text =
                        when (mode) {
                            MealPlanBloc.ViewMode.DAY -> stringResource(Res.string.meal_plan_day)
                            MealPlanBloc.ViewMode.WEEK -> stringResource(Res.string.meal_plan_week)
                            MealPlanBloc.ViewMode.MONTH ->
                                stringResource(Res.string.meal_plan_month)
                        }
                )
            }
        }
    }
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
private fun MonthView(
    monthModel: MealPlanBloc.MonthModel?,
    onDaySelected: (LocalDate) -> Unit,
    onMealClicked: (MealPlanItem) -> Unit,
    onDeleteClicked: (MealPlanItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (monthModel == null) {
        EmptyMealsMessage(modifier)
        return
    }

    val dayMeals = monthModel.selectedDayMeals
    val allEmpty =
        dayMeals == null ||
            (dayMeals.breakfast.isEmpty() &&
                dayMeals.lunch.isEmpty() &&
                dayMeals.dinner.isEmpty() &&
                dayMeals.snacks.isEmpty())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingSmall),
        contentPadding = PaddingValues(bottom = ChefMateTheme.dimens.fabClearance),
    ) {
        item(key = "month_calendar") {
            MonthCalendar(
                firstDayOfMonth = monthModel.firstDayOfMonth,
                selectedDay = monthModel.selectedDay,
                daysWithMeals = monthModel.daysWithMeals,
                onDaySelected = onDaySelected,
                modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
            )
        }
        if (allEmpty) {
            item(key = "month_empty") {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.meal_plan_no_meals),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (dayMeals != null) {
            if (dayMeals.breakfast.isNotEmpty()) {
                stickyHeader(key = "month_breakfast") {
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
                stickyHeader(key = "month_lunch") {
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
                stickyHeader(key = "month_dinner") {
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
                stickyHeader(key = "month_snacks") {
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
}

@Composable
private fun MonthCalendar(
    firstDayOfMonth: LocalDate,
    selectedDay: LocalDate,
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
                            isSelected = date == selectedDay,
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
                        color =
                            if (isSelected) primaryColor
                            else androidx.compose.ui.graphics.Color.Transparent,
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
        contentPadding = PaddingValues(bottom = ChefMateTheme.dimens.fabClearance),
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
        contentPadding = PaddingValues(bottom = ChefMateTheme.dimens.fabClearance),
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
                .background(MaterialTheme.colorScheme.background)
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
            horizontalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecipeImage(
                imageUrl = meal.recipeImageUrl,
                contentDescription = meal.recipeTitle,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = meal.recipeTitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            SyncStatusIcon(meal.syncStatus)
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
            RecipeImage(
                imageUrl = meal.recipeImageUrl,
                contentDescription = meal.recipeTitle,
                modifier = Modifier.size(40.dp),
            )
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
            SyncStatusIcon(meal.syncStatus)
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
private fun SyncStatusIcon(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val syncingDescription = stringResource(Res.string.meal_plan_sync_syncing)
    when (syncStatus) {
        SyncStatus.NOT_SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = stringResource(Res.string.meal_plan_sync_not_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(20.dp),
            )
        SyncStatus.SYNCING ->
            CircularProgressIndicator(
                modifier =
                    modifier.size(16.dp).semantics { contentDescription = syncingDescription },
                strokeWidth = 2.dp,
            )
        SyncStatus.SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = stringResource(Res.string.meal_plan_sync_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(20.dp),
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
