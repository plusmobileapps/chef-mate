package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookScreen
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookCollaboratorsBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookCreateBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookDeleteConfirmBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookEditBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookErrorBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookLeaveConfirmBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookLeaveErrorBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookMemberViewBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookRemoveConfirmBloc
import com.plusmobileapps.chefmate.recipebook.edit.previewEditRecipeBookSavingBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun EditRecipeBookCreateScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookCreateBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun EditRecipeBookEditScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookEditBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun EditRecipeBookSavingScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookSavingBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700)
@Composable
fun EditRecipeBookErrorScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookErrorBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditRecipeBookCreateDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { EditRecipeBookScreen(bloc = previewEditRecipeBookCreateBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookCollaboratorsScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookCollaboratorsBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookMemberViewScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookMemberViewBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookRemoveConfirmScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookRemoveConfirmBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookDeleteConfirmScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookDeleteConfirmBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookLeaveConfirmScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookLeaveConfirmBloc) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 900)
@Composable
fun EditRecipeBookLeaveErrorScreenshot() {
    ChefMateTheme { EditRecipeBookScreen(bloc = previewEditRecipeBookLeaveErrorBloc) }
}
