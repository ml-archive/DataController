package com.fuzz.datacontroller

import com.fuzz.datacontroller.annotations.*
import com.fuzz.datacontroller.sharedpreferences.PreferenceDelegate
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import retrofit2.http.GET
import retrofit2.http.Path

@Table(database = TestDatabase::class)
data class ShoppingList(@PrimaryKey var id: String = "", @PrimaryKey var storeId: String = "")

@DataDefinition
interface ShoppingListApi {

    @DB
    @Memory
    @Network
    @DataControllerRef
    fun shoppingListDataController(): DataController<ShoppingList>


    @Reuse("shoppingListDataController")
    @GET("/shoppinglists/{listId}/{storeId}")
    fun getShoppingList(@Path("listId") @DQuery("id") shoppingListId: String,
                        @Path("storeId") @DQuery("storeId") storeId: String,
                        dataControllerCallback: DataController.DataControllerCallback<ShoppingList>)
            : DataControllerRequest<ShoppingList>

    @Targets
    @Reuse("shoppingListDataController")
    @Memory
    fun getShoppingListFromMemory(dataControllerCallback: DataController.DataControllerCallback<ShoppingList>)
            : DataControllerRequest<ShoppingList>

    @Reuse("shoppingListDataController")
    fun getShoppingListFromStorage(): ShoppingList?

    @SharedPreferences(preferenceDelegate = PrefDelegate::class)
    fun getShoppingListSharedPreferences(dataControllerCallback: DataController.DataControllerCallback<ShoppingList>)
            : DataControllerRequest<ShoppingList>
}

class PrefDelegate : PreferenceDelegate<ShoppingList> {
    override fun setValue(editor: android.content.SharedPreferences.Editor, value: DataControllerResponse<ShoppingList>) {

    }

    override fun getValue(sharedPreferences: android.content.SharedPreferences): ShoppingList? {
        return null
    }
}