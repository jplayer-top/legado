package io.legado.app.model

import io.legado.app.data.api.IHttpGetApi
import io.legado.app.data.api.IHttpPostApi
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.SearchBook
import io.legado.app.help.http.HttpHelper
import io.legado.app.model.analyzeRule.AnalyzeUrl
import io.legado.app.model.webbook.BookList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WebBook(val bookSource: BookSource) : CoroutineScope by MainScope() {

    fun searchBook(key: String, page: Int?, success: (() -> Unit), error: (() -> Unit)) {
        launch(IO) {
            bookSource.getSearchRule().searchUrl?.let { searchUrl ->
                val analyzeUrl = AnalyzeUrl(searchUrl)
                val response = when {
                    analyzeUrl.method == AnalyzeUrl.Method.POST -> HttpHelper.getApiService<IHttpPostApi>(
                        analyzeUrl.baseUrl
                    ).postBody(
                        analyzeUrl.url,
                        analyzeUrl.body,
                        analyzeUrl.headerMap
                    ).await()
                    analyzeUrl.fieldMap.isEmpty() -> HttpHelper.getApiService<IHttpGetApi>(
                        analyzeUrl.baseUrl
                    )[analyzeUrl.url, analyzeUrl.headerMap].await()
                    else -> HttpHelper.getApiService<IHttpGetApi>(analyzeUrl.baseUrl)
                        .getMap(analyzeUrl.url, analyzeUrl.fieldMap, analyzeUrl.headerMap).await()
                }
                val bookList = BookList().analyzeBookList(response, bookSource)
            }

        }
    }


}