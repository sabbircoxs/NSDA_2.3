package com.example.nsda_23

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "book_table")
data class Book(
   @PrimaryKey(autoGenerate = true) val id: Long = 0,
   val title: String,
   val author: String,
   val pages: Int,
   var isRead: Boolean = false
)
