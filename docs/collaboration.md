# Collaboration Architecture

This document describes the architecture for grocery list and recipe collaboration in Chef Mate.

## Overview

Collaboration allows users to share grocery lists and recipes with others via email invitations. Each shared resource has role-based access control (Owner, Editor, Viewer) enforced at both the UI and database levels via Supabase Row Level Security (RLS).

```mermaid
flowchart TB
    subgraph Client["Client (KMP)"]
        UI["UI Layer\nGroceryListScreen\nRecipe Screens"]
        BLoC["BLoC / ViewModel\nGroceryListBloc\nGroceryListViewModel"]
        Repo["Repository\nGroceryRepositoryImpl\nRecipeRepositoryImpl"]
        Local["SQLDelight\nGroceryList, GroceryListMember\nRecipe"]
        Remote["Remote Data Source\nSupabaseGroceryRemoteDataSource\nSupabaseRecipeRemoteDataSource"]
    end

    subgraph Supabase["Supabase"]
        Auth["Auth\n(user identity)"]
        DB["PostgreSQL\ngrocery_lists\ngrocery_items\ngrocery_list_members\nrecipes\nrecipe_shares"]
        RLS["RLS Policies\n(access control)"]
        RT["Realtime\n(live updates)"]
    end

    UI --> BLoC --> Repo
    Repo --> Local
    Repo --> Remote
    Remote --> DB
    DB --> RLS
    DB --> RT
    Auth --> RLS
    RT -.-> Remote
```

## Roles

| Role | Grocery Lists | Recipes |
|------|--------------|---------|
| **Owner** | Full control: rename, delete list, manage items, invite/remove collaborators | Full control: edit, delete, share |
| **Editor** | Add, check, delete items. Cannot rename or delete the list | Edit recipe. Cannot delete |
| **Viewer** | Read-only. Cannot add, check, or delete items | Read-only. Can fork (copy) to own account |

## Data Model

### Supabase Tables

```mermaid
erDiagram
    grocery_lists {
        uuid id PK
        text name
        uuid owner_id FK
        timestamptz created_at
        timestamptz updated_at
    }

    grocery_items {
        uuid id PK
        uuid list_id FK
        text name
        boolean is_checked
        text client_id
        timestamptz created_at
        timestamptz updated_at
    }

    grocery_list_members {
        uuid id PK
        uuid list_id FK
        uuid user_id FK
        text invited_email
        text role "owner | editor | viewer"
        text status "pending | accepted | rejected"
        uuid invited_by FK
        timestamptz created_at
    }

    recipes {
        uuid id PK
        uuid owner_id FK
        text title
        uuid forked_from FK "nullable, self-reference"
    }

    recipe_shares {
        uuid id PK
        uuid recipe_id FK
        uuid user_id FK
        text invited_email
        text role "owner | editor | viewer"
        text status "pending | accepted | rejected"
        uuid invited_by FK
        timestamptz created_at
    }

    grocery_lists ||--o{ grocery_items : contains
    grocery_lists ||--o{ grocery_list_members : has
    recipes ||--o{ recipe_shares : has
    recipes ||--o| recipes : "forked from"
```

### Local SQLDelight Schema

The local database mirrors the remote collaboration state with these additions to existing tables:

**GroceryList** (new columns):
- `ownerId` - remote user ID of the list owner
- `role` - current user's role (`owner`, `editor`, `viewer`)
- `isShared` - whether this list was shared by another user

**GroceryListMember** (new table): local cache of collaborators per list.

**Recipe** (new columns):
- `forkedFromRemoteId` - remote ID of the original recipe (if forked)
- `forkedFromTitle` - title of the original recipe
- `role` / `isShared` - same as grocery lists

## Invitation Flow

```mermaid
sequenceDiagram
    participant Owner as Owner (User A)
    participant Supabase
    participant Invitee as Invitee (User B)

    Owner->>Supabase: INSERT into grocery_list_members<br/>(email, role=editor, status=pending)
    Note over Supabase: RLS: only list owner can insert

    Invitee->>Supabase: Sync (SELECT grocery_lists)
    Note over Supabase: RLS returns lists where<br/>user is owner OR accepted member
    Supabase-->>Invitee: Shared list appears after accept

    Invitee->>Supabase: UPDATE grocery_list_members<br/>SET status = 'accepted'
    Note over Supabase: RLS: invited user can update own row

    Invitee->>Supabase: Add/edit grocery items
    Note over Supabase: RLS: editor role allows<br/>INSERT/UPDATE/DELETE on items
```

For users who don't have an account yet, the `invited_email` is stored with `user_id = NULL`. A database trigger on `auth.users` INSERT automatically migrates pending invitations when the user signs up.

## Sync Strategy

The existing offline-first sync is extended for collaboration:

1. **Push phase** (owned lists only):
   - Push unsynced lists where `role = 'owner'`
   - Push dirty lists where `role = 'owner'`
   - Shared lists are never pushed as new creations

2. **Pull phase** (all accessible lists):
   - `fetchAccessibleGroceryLists()` uses a filter-less SELECT — RLS returns owned + shared lists
   - Each pulled list is tagged locally with `ownerId`, `role`, and `isShared`
   - Members are fetched and cached in `GroceryListMember` table

3. **Item sync** (unchanged):
   - Items sync per-list as before
   - RLS handles authorization for shared list items

## Recipe Forking

When a viewer or editor copies a shared recipe:

1. A new recipe is created in the user's account with identical content
2. `forked_from` is set to the original recipe's remote ID
3. The local record stores `forkedFromRemoteId` and `forkedFromTitle`
4. The UI displays "Forked from: [original title]" on the recipe detail screen

The fork is a full copy — changes to the original do not propagate.

## RLS Policy Summary

All access control is enforced server-side via Supabase RLS:

| Table | SELECT | INSERT | UPDATE | DELETE |
|-------|--------|--------|--------|--------|
| `grocery_lists` | owner OR accepted member | owner only | owner OR editor member | owner only |
| `grocery_items` | user has list access | owner OR editor | owner OR editor | owner OR editor |
| `grocery_list_members` | list members + owner | list owner only | invited user OR owner | owner OR self (leave) |
| `recipes` | owner OR accepted share | owner only | owner OR editor share | owner only |
| `recipe_shares` | recipe members + owner | recipe owner only | invited user OR owner | owner OR self |

## UI Components

### Grocery List Screen

- **Share button** (top app bar): visible only to list owners. Opens the collaborator management sheet.
- **Collaborator sheet**: shows current members with role/status, email invite input field.
- **List selector**: split into "My Lists" and "Shared with me" sections. Shared lists show a badge.
- **Permission gating**: viewers don't see the add-item input or delete buttons.

### Key Files

| Concern | Path |
|---------|------|
| Supabase migration | `supabase/migrations/20260425_add_collaboration.sql` |
| SQLDelight migration | `client/database/src/commonMain/sqldelight/migrations/4.sqm` |
| Member queries | `client/database/src/commonMain/sqldelight/.../GroceryListMember.sq` |
| Collaboration models | `client/grocery/data/public/.../CollaborationModels.kt` |
| Remote member model | `client/grocery/data/impl/.../remote/RemoteGroceryListMember.kt` |
| Repository (grocery) | `client/grocery/data/impl/.../GroceryRepositoryImpl.kt` |
| Repository (recipe) | `client/recipe/data/impl/.../RecipeRepositoryImpl.kt` |
| BLoC interface | `client/grocery/core/public/.../list/GroceryListBloc.kt` |
| ViewModel | `client/grocery/core/impl/.../list/GroceryListViewModel.kt` |
| UI screen | `client/grocery/core/public/.../list/GroceryListScreen.kt` |
| DI bindings | `client/database/.../di/DatabaseComponent.kt` |
