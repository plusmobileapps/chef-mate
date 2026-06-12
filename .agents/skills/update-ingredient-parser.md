# update-ingredient-parser

Add a new ingredient classification to the IngredientParser and write a unit test for it.

## Required Input

The user must provide:
1. **Ingredient text** — the raw ingredient string (e.g., "2 cups tofu")
2. **Expected category** — the `GroceryCategory` it should be classified as (e.g., `PRODUCE`, `DAIRY`, `MEAT`, etc.)

If the user does not provide both, ask before proceeding.

## Instructions

### Step 1: Update the category map

Open `client/grocery/data/public/src/commonMain/kotlin/com/plusmobileapps/chefmate/grocery/data/IngredientParser.kt`.

Find the `CATEGORY_MAP` and add the new keyword(s) to the appropriate `GroceryCategory` entry. Choose the shortest keyword that uniquely identifies the ingredient without causing false positives against other categories. Be mindful that keywords are matched longest-first, so a more specific keyword will take priority over a shorter one.

If the ingredient is already matched by an existing keyword but to the **wrong** category, you may need to add a longer, more specific keyword to the correct category so it matches first.

### Step 2: Write a unit test

Open `client/grocery/data/public/src/commonTest/kotlin/com/plusmobileapps/chefmate/grocery/data/IngredientParserTest.kt`.

Add a new test method following the existing naming convention `parse_<description>`. Use the full ingredient text the user provided as input to `IngredientParser.parse()` and assert:
- `name` — the expected ingredient name (after quantity extraction)
- `quantity` — the expected quantity string, or `assertNull` if none
- `category` — the expected `GroceryCategory`

Follow the exact assertion style used in the existing tests.

### Step 3: Run the tests

Run:
```sh
./gradlew :client:grocery:data:public:test
```

If the test fails, investigate and fix the issue. Do not move on until the new test passes along with all existing tests.
