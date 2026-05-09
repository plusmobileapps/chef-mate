package com.plusmobileapps.chefmate.devsettings.impl

import com.plusmobileapps.chefmate.devsettings.TestUser
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUserProviderTest {

    @Test
    fun `parse returns empty list for blank input`() {
        assertEquals(emptyList(), TestUserProvider.parse(""))
        assertEquals(emptyList(), TestUserProvider.parse("   "))
    }

    @Test
    fun `parse handles single user`() {
        val users = TestUserProvider.parse("alice@chefmate.test|hunter2")
        assertEquals(listOf(TestUser(1, "alice@chefmate.test", "hunter2")), users)
    }

    @Test
    fun `parse handles multiple users with sequential indexes`() {
        val users =
            TestUserProvider.parse(
                "alice@chefmate.test|hunter2;bob@chefmate.test|hunter3;carol@chefmate.test|hunter4"
            )
        assertEquals(
            listOf(
                TestUser(1, "alice@chefmate.test", "hunter2"),
                TestUser(2, "bob@chefmate.test", "hunter3"),
                TestUser(3, "carol@chefmate.test", "hunter4"),
            ),
            users,
        )
    }

    @Test
    fun `parse skips malformed entries`() {
        val users = TestUserProvider.parse("alice@chefmate.test|hunter2;malformed_no_pipe")
        // Malformed entry filtered out; the only valid entry keeps its original positional index.
        assertEquals(listOf(TestUser(1, "alice@chefmate.test", "hunter2")), users)
    }

    @Test
    fun `parse skips empty trailing segment`() {
        val users = TestUserProvider.parse("alice@chefmate.test|hunter2;")
        assertEquals(listOf(TestUser(1, "alice@chefmate.test", "hunter2")), users)
    }
}
