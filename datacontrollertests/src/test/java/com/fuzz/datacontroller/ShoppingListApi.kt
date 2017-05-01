package com.fuzz.datacontroller

import com.fuzz.datacontroller.annotations.*
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
    @GET("/shoppinglists/{listId}/{storeId}")
    fun getShoppingList(@Path("listId") @DQuery("id") shoppingListId: String,
                        @Path("storeId") @DQuery("storeId") storeId: String,
                        dataControllerCallback: DataController.DataControllerCallback<ShoppingList>)
            : DataControllerRequest<ShoppingList>

    @Reuse("getShoppingList")
    fun getShoppingListWithError(@DQuery("id") shoppingListId: String,
                                 @DQuery("storeId") storeId: String,
                                 dataControllerCallback: DataController.DataControllerCallback<ShoppingList>,
                                 errorFilter: DataControllerRequest.ErrorFilter)
            : DataControllerRequest<ShoppingList>

    @Targets
    @Reuse("getShoppingList")
    @Memory
    fun getShoppingListFromMemory(dataControllerCallback: DataController.DataControllerCallback<ShoppingList>)
            : DataControllerRequest<ShoppingList>

}
