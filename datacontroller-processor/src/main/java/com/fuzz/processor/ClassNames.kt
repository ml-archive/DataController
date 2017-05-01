package com.fuzz.processor

import com.squareup.javapoet.ClassName

private val package_name = "com.fuzz.datacontroller"
private var retrofit_package = "retrofit2"
private val retrofit_package_http = "$retrofit_package.http"

val DATACONTROLLER_REQUEST = ClassName.get(package_name, "DataControllerRequest")

val DATA_DEFINITION = ClassName.get("$package_name.manager", "DataDefinition")
val DATASOURCE_PROVIDER = ClassName.get(package_name, "DataSourceProvider")

val DATACONTROLLER_REQUEST_BUILDER = ClassName.get(package_name, "DataControllerRequest.Builder")

val DATACONTROLLER_RESPONSE = ClassName.get(package_name, "DataControllerResponse")

val DATACONTROLLER = ClassName.get(package_name, "DataController")

val DATACONTROLLER_CALLBACK = ClassName.get(package_name, "DataController", "DataControllerCallback")

val ERROR_FILTER = ClassName.get(package_name, "DataControllerRequest", "ErrorFilter")

val MEMORY_SOURCE = ClassName.get("$package_name.source", "MemorySource")

val DBFLOW_SINGLE_SOURCE = ClassName.get("$package_name.dbflow", "DBFlowSingleSource")
val DBFLOW_LIST_SOURCE = ClassName.get("$package_name.dbflow", "DBFlowListSource")

val SHARED_PREFERENCES_SOURCE = ClassName.get("$package_name.sharedpreferences", "SharedPreferencesSource")
val SHARED_PREFERENCES = ClassName.get("android.content", "SharedPreferences")

val DATA_SOURCE_PARAMS = ClassName.get("$package_name.source", "DataSourceContainer", "DataSourceParams")

val SOURCE_TYPE = ClassName.get("$package_name.source", "DataSource", "SourceType")

val DBFLOW_PARAMS = ClassName.get("$package_name.dbflow", "BaseDBFlowSource", "DBFlowParams")

val SQLITE = ClassName.get("com.raizlabs.android.dbflow.sql.language", "SQLite")

val GET = ClassName.get(retrofit_package_http, "GET")
val OPTIONS = ClassName.get(retrofit_package_http, "OPTIONS")
val PUT = ClassName.get(retrofit_package_http, "PUT")
val DELETE = ClassName.get(retrofit_package_http, "DELETE")
val POST = ClassName.get(retrofit_package_http, "POST")

val retrofitMethodSet = setOf(GET, OPTIONS, PUT, DELETE, POST)

val CALL = ClassName.get(retrofit_package, "Call")
val RETROFIT = ClassName.get(retrofit_package, "Retrofit")
val RETROFIT_SOURCE = ClassName.get("$package_name.retrofit", "RetrofitSource")
val RETROFIT_SOURCE_PARAMS = ClassName.get("$package_name.retrofit", "RetrofitSource", "RetrofitSourceParams")



