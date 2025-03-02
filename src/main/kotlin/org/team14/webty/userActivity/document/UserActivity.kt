package org.team14.webty.userActivity.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate

@Document(indexName = "useractivity")
class UserActivity (
    @Field(type = FieldType.Keyword)
    val webtoonId: String,

    @Field(type = FieldType.Keyword)
    val userId: String,

    @Field(type = FieldType.Text)
    val webtoonName: String,

    @Id
    var id: String? = null, // Elasticsearch에서는 ID가 nullable 가능?

    @Field(type = FieldType.Date) //?
    var date: LocalDate? = null
)
