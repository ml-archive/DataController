package com.fuzz.datacontroller

import com.fuzz.datacontroller.annotations.*
import com.fuzz.datacontroller.retrofit.RetrofitSource
import com.fuzz.datacontroller.sharedpreferences.PreferenceDelegate
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.strategy.OneShotRefreshStrategy
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

@Table(database = TestDatabase::class)
data class ShoppingList(@PrimaryKey var id: String = "", @PrimaryKey var storeId: String = "")

@DataDefinition
interface ShoppingListApi {

    @DB
    @Memory(refreshStrategy = OneShotRefreshStrategy::class)
    @Network(responseHandler = CustomResponseHandler::class, errorConverter = CustomErrorConverter::class)
    @DataControllerRef
    fun shoppingListDataController(): DataController<ShoppingList>

    @Reuse("shoppingListDataController")
    @GET("/shoppinglists/{listId}/{storeId}")
    fun getShoppingList(@Path("listId") @DQuery("id") shoppingListId: String,
                        @Path("storeId") @DQuery("storeId") storeId: String,
                        sourceParams: DataSource.SourceParams,
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

class CustomResponseHandler : RetrofitSource.ResponseHandler<ShoppingList> {
    override fun handleResponse(retrofitSource: RetrofitSource<ShoppingList>?, sourceParams: DataSource.SourceParams?,
                                call: Call<ShoppingList>?, response: Response<ShoppingList>?,
                                success: DataController.Success<ShoppingList>?, error: DataController.Error?, currentRetryCount: Int) {

    }

    override fun handleFailure(retrofitSource: RetrofitSource<ShoppingList>?, sourceParams: DataSource.SourceParams?,
                               success: DataController.Success<ShoppingList>?, error: DataController.Error?,
                               dataResponseError: DataResponseError?, currentRetryCount: Int) {

    }
}

class CustomErrorConverter<T> : RetrofitSource.ResponseErrorConverter<T> {
    override fun convertFromResponse(response: Response<T>): DataResponseError {
        return DataResponseError.networkErrorOf(response.message(), response.code())
                .userFacingMessage(response.message()).build()
    }
}