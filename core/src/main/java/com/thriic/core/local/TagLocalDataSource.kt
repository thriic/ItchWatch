package com.thriic.core.local

import com.thriic.core.model.LocalInfo
import com.thriic.core.model.SearchTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagLocalDataSource @Inject constructor(private val tagDao: TagDao) {
    suspend fun getAllSearchTag() = withContext(Dispatchers.IO) {
        tagDao.getAll()
    }

    suspend fun getSearchTag(tagName: String) =
        withContext(Dispatchers.IO) {
            tagDao.queryTag(tagName)
        }

    suspend fun updateSearchTagIfNew(vararg tags: SearchTag) =
        withContext(Dispatchers.IO) {
            val size = tagDao.count()
            if (size < tags.size) {
                tagDao.insertAll(*tags)
            }
            tags.size - size
        }


    suspend fun empty() =
        withContext(Dispatchers.IO) {
            tagDao.count() == 0
        }

    suspend fun count() = withContext(Dispatchers.IO) {
        tagDao.count()
    }

}