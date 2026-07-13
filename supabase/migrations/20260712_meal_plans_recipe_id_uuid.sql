-- Meal plans referenced recipes by the client's LOCAL SQLDelight autoincrement
-- id (an integer), which is meaningless on any other device — each device
-- numbers its own recipes independently. As a result a meal synced from one
-- device pointed at the wrong recipe (or none) on another, so meals never
-- appeared cross-device.
--
-- The fix stores the recipe's UUID (recipes.id) in meal_plans.recipe_id, the
-- same cross-device identity recipes/categories/books already sync on. This
-- migration switches the column type accordingly.
--
-- DESTRUCTIVE: existing recipe_id values are un-mappable local integers, so the
-- existing rows are cleared. Clients re-push their meal plans (now carrying
-- recipe UUIDs) on the next sync, so no user data is lost locally.
--
-- NOTE: production's schema is hand-managed and its migration history is not
-- reconciled with the CLI, so review the current meal_plans definition before
-- applying this in prod (column may already differ from this assumption).

BEGIN;

-- Existing rows carry local integer recipe ids that cannot be cast to a UUID.
DELETE FROM public.meal_plans;

ALTER TABLE public.meal_plans
    DROP CONSTRAINT IF EXISTS meal_plans_recipe_id_fkey;

ALTER TABLE public.meal_plans
    ALTER COLUMN recipe_id TYPE UUID USING NULL;

ALTER TABLE public.meal_plans
    ALTER COLUMN recipe_id SET NOT NULL;

ALTER TABLE public.meal_plans
    ADD CONSTRAINT meal_plans_recipe_id_fkey
    FOREIGN KEY (recipe_id) REFERENCES public.recipes(id) ON DELETE CASCADE;

COMMIT;
