package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CertificationItem
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.ProjectItem
import com.example.data.model.SkillCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromExperienceList(list: List<ExperienceItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ExperienceItem::class.java)
        return moshi.adapter<List<ExperienceItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toExperienceList(json: String?): List<ExperienceItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ExperienceItem::class.java)
        return moshi.adapter<List<ExperienceItem>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromEducationList(list: List<EducationItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, EducationItem::class.java)
        return moshi.adapter<List<EducationItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toEducationList(json: String?): List<EducationItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, EducationItem::class.java)
        return moshi.adapter<List<EducationItem>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromSkillCategoryList(list: List<SkillCategory>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, SkillCategory::class.java)
        return moshi.adapter<List<SkillCategory>>(type).toJson(list)
    }

    @TypeConverter
    fun toSkillCategoryList(json: String?): List<SkillCategory> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, SkillCategory::class.java)
        return moshi.adapter<List<SkillCategory>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromProjectList(list: List<ProjectItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ProjectItem::class.java)
        return moshi.adapter<List<ProjectItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toProjectList(json: String?): List<ProjectItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ProjectItem::class.java)
        return moshi.adapter<List<ProjectItem>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromCertificationList(list: List<CertificationItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, CertificationItem::class.java)
        return moshi.adapter<List<CertificationItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toCertificationList(json: String?): List<CertificationItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, CertificationItem::class.java)
        return moshi.adapter<List<CertificationItem>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
    }
}
