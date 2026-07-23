package com.plusmobileapps.chefmate.recipebook.data.testing

import com.plusmobileapps.chefmate.recipebook.data.RecipeBookCollaborationRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookInvite
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMemberStatus
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole

class FakeRecipeBookCollaborationRepository(
    var owner: Boolean = true,
    var members: MutableList<RecipeBookMember> = mutableListOf(),
    var invites: MutableList<RecipeBookInvite> = mutableListOf(),
) : RecipeBookCollaborationRepository {

    val invited: MutableList<Triple<Long, String, RecipeBookRole>> = mutableListOf()
    val removed: MutableList<String> = mutableListOf()
    val accepted: MutableList<String> = mutableListOf()
    val left: MutableList<Long> = mutableListOf()
    val declined: MutableList<String> = mutableListOf()

    override suspend fun isOwner(bookId: Long): Boolean = owner

    override suspend fun getMembers(bookId: Long): List<RecipeBookMember> = members

    override suspend fun invite(bookId: Long, email: String, role: RecipeBookRole) {
        invited += Triple(bookId, email, role)
        members +=
            RecipeBookMember(
                id = "m-${members.size}",
                email = email,
                role = role,
                status = RecipeBookMemberStatus.PENDING,
            )
    }

    override suspend fun removeMember(memberId: String) {
        removed += memberId
        members.removeAll { it.id == memberId }
    }

    override suspend fun leaveBook(bookId: Long) {
        if (owner) error("Owners can't leave their own book")
        left += bookId
    }

    override suspend fun pendingInvites(): List<RecipeBookInvite> = invites

    override suspend fun acceptInvite(memberId: String) {
        accepted += memberId
        invites.removeAll { it.memberId == memberId }
    }

    override suspend fun declineInvite(memberId: String) {
        declined += memberId
        invites.removeAll { it.memberId == memberId }
    }
}
