package com.plusmobileapps.chefmate.devsettings

data class TestUser(val index: Int, val email: String, val password: String) {
    override fun toString(): String = "TestUser(index=$index, email=$email, password=***)"
}
