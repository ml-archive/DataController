package com.grosner.processor

import com.squareup.javapoet.ClassName

private val package_name = "com.fuzz.datacontroller"

val DATACONTROLLER_REQUEST = ClassName.get(package_name, "DataControllerRequest")

val DATACONTROLLER_REQUEST_BUILDER = ClassName.get(package_name, "DataControllerRequest.Builder")


val DATACONTROLLER = ClassName.get(package_name, "DataController")

val DATACONTROLLER_CALLBACK = ClassName.get(package_name, "DataController.DataControllerCallback")


val MEMORY_SOURCE = ClassName.get("$package_name.source", "MemorySource")

val DBFLOW_SINGLE_SOURCE = ClassName.get("$package_name.dbflow", "DBFlowSingleSource")
val DBFLOW_LIST_SOURCE = ClassName.get("$package_name.dbflow", "DBFlowListSource")

val DATA_SOURCE_PARAMS = ClassName.get("$package_name.source", "DataSourceContainer", "DataSourceParams")

val SOURCE_TYPE = ClassName.get("$package_name.source", "DataSource", "SourceType")

val DBFLOW_PARAMS = ClassName.get("$package_name.dbflow", "BaseDBFlowSource", "DBFlowParams")

val SQLITE = ClassName.get("com.raizlabs.android.dbflow.sql.language", "SQLite")