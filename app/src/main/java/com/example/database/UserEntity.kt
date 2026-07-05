package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["id"])]
)
data class UserEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val avatar: String, // avatar string representation or avatarId
    val level: Int,
    val xp: Int,
    val coins: Int,
    val currentStreak: Int,
    val createdAt: Long, // UTC timestamp
    val updatedAt: Long, // UTC timestamp
    
    // Additional fields for complete userStats state persistence
    val selectedTitle: String,
    val unlockedTitlesJson: String, // comma-separated or serialized
    val unlockedBadgesJson: String, // comma-separated or serialized
    val totalXp: Int,
    val todayXpEarned: Int = 65,
    val todayCoinsEarned: Int = 18
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String, // UUID (usually single row)
    val soundEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val hapticFeedback: Boolean,
    val createdAt: Long, // UTC
    val updatedAt: Long  // UTC
)
