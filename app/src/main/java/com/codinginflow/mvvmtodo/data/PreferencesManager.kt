package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"
enum class SortOrder
{
    BY_NAME,BY_DATE
}
data class FilterPreferences(val sortOrder: SortOrder,val hideCompleted:Boolean)
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context:Context) {
    private val dataStore=context.createDataStore("user_preferences")

    val preferencesFlow=dataStore.data
            .catch { exception ->
                if(exception is IOException)
                {
                    Log.e(TAG, ": error reading preferences",exception)
                    emit(emptyPreferences())
                }
                else
                {
                    throw exception
                }

            }
            .map { preferences->

                val sortOrder=SortOrder.valueOf(
                        preferences[preferenceKeys.SORT_ORDER]?:SortOrder.BY_DATE.name
                )
                val hideCompleted=preferences[preferenceKeys.HIDE_COMPLETED] ?:false

                FilterPreferences(sortOrder,hideCompleted)

            }
    suspend fun updateSortOrder(sortOrder: SortOrder)
    {
        dataStore.edit { preferences->
            preferences[preferenceKeys.SORT_ORDER]=sortOrder.name

        }
    }
    suspend fun updateHideCompleted(hideCompleted: Boolean)
    {
        dataStore.edit { preferences->
            preferences[preferenceKeys.HIDE_COMPLETED]=hideCompleted
        }
    }
    private object preferenceKeys{
        val SORT_ORDER = preferencesKey<String> ( "sort_Order" )
        val HIDE_COMPLETED = preferencesKey<Boolean>("Hide_completed")
    }
}